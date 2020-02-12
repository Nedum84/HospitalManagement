package com.hospitalmanagement

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import kotlinx.android.synthetic.main.adapter_apps.view.*
import org.json.JSONException
import org.json.JSONObject


class AppsAdapter(val list:MutableList<AppClassBinder>, val context: Context): RecyclerView.Adapter<AppsAdapter.ViewHolder>(){
    private var adapterCallbackInterface: AppAdapterCallbackInterface? = null



    init {
        try {
            adapterCallbackInterface = context as AppAdapterCallbackInterface
        } catch (e: ClassCastException) {
            throw RuntimeException(context.toString() + "Activity must implement AppAdapterCallbackInterface.", e)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(
                R.layout.adapter_apps,
                parent,
                false
        ))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun addItems(items: MutableList<AppClassBinder>) {
        val lastPos = list.size - 1
        list.addAll(items)
        notifyItemRangeInserted(lastPos, items.size)
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listDetails = list[position]

        holder.appPatName.text = "Patient Name: ${listDetails.pat_name}"
        holder.appDate.text     = ClassDateAndTime().getDateTime(listDetails.app_date!!.toLong())
        holder.appDocName.text     = "With ${listDetails.doc_name}"
        holder.appCancelBtn.visibility = View.GONE
        holder.appCancelledBtn.visibility = View.GONE
        holder.appDoneBtn.visibility = View.GONE
        holder.appViewPrescriptionBtn.visibility = View.GONE
        holder.appPrescribeBtn.visibility = View.GONE

        if(listDetails.app_status=="0"){//pending
            holder.appCancelBtn.visibility = View.VISIBLE
        }else if(listDetails.app_status=="1"){//cancellded
            holder.appCancelledBtn.visibility = View.VISIBLE
        }else if(listDetails.app_status=="2"){//done or completed or with prescription already  :)
            holder.appViewPrescriptionBtn.visibility = View.VISIBLE
            holder.appDoneBtn.visibility = View.VISIBLE
        }
        if(ClassSharedPreferences(context).getUserLevel()=="2"&&listDetails.app_status=="0"){//visible to doctors only
            holder.appPrescribeBtn.visibility = View.VISIBLE
        }






        holder.appCancelBtn.setOnClickListener {
            adapterCallbackInterface?.onCancelApp(listDetails)
        }
        holder.appSymptomsBtn.setOnClickListener {
            ClassAlertDialog(context).alertMessage("${listDetails.pat_symptoms}","symptoms")
        }

        holder.appViewPrescriptionBtn.setOnClickListener {
            adapterCallbackInterface?.onViewPrescription(listDetails)


//            val drugs = Gson().fromJson("", Array<DrugClassBinder>::class.java).asList()
//
//            val drugNames = drugs.map { it.drug_name }.toTypedArray()
//
//            val builder = AlertDialog.Builder(context)
//            builder.setTitle("Drug Prescriptions")
//
//            builder.setItems(drugNames,null)
//            builder.setNegativeButton("CLOSE"){ _, _ ->}
//
//            val dialog = builder.create()
//            dialog.show()
        }
        holder.appPrescribeBtn.setOnClickListener {
            getDrugs(listDetails)
        }


    }
    fun getDrugs(appsList: AppClassBinder){
        //creating volley string request
        val pDialog = ClassProgressDialog(context)
        pDialog.createDialog()
        val stringRequest = object : StringRequest(Request.Method.POST, UrlHolder.URL_GET_DRUGS,
                Response.Listener<String> { response ->
                    pDialog.dismissDialog()

                    try {
                        val obj = JSONObject(response)
                        if (!obj.getBoolean("error")) {
                            val jsonResponse = obj.getJSONArray("drugsz_arraysz")

                            if ((jsonResponse.length()!=0)){

                                val qDataArray = mutableListOf<DrugClassBinder>()
                                for (i in 0 until jsonResponse.length()) {
                                    val jsonObj = jsonResponse.getJSONObject(i)
                                    val subject = DrugClassBinder(
                                            jsonObj.getString("drug_id"),
                                            jsonObj.getString("drug_name"),
                                            jsonObj.getString("drug_desc"),
                                            jsonObj.getString("drug_price")
                                    )
                                    qDataArray.add(subject)
                                }
                                showDrugsAlertDialog(appsList, qDataArray)

                            }else{
                                ClassAlertDialog(context).toast("No drug found...")
                            }
                        } else {
                            ClassAlertDialog(context).toast("An error occurred, try again...")
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { _ ->
                    pDialog.dismissDialog()
                    ClassAlertDialog(context).toast("No Network Connection...")
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String?> {
                val params = HashMap<String, String?>()
                params["request_type"] = "get_drugs"
                params["search_query"] = ""
                return params
            }
        }
        VolleySingleton.instance?.addToRequestQueue(stringRequest)//adding request to queue
        //volley interactions end

    }


    fun showDrugsAlertDialog(appsList: AppClassBinder, drugList:MutableList<DrugClassBinder>){

        val drugName = arrayListOf<String>()
        val drugId = arrayListOf<String>()
        val checkedItems = arrayListOf<Boolean>()
        for (element in drugList) {
            drugName.add(element.drug_name)
            drugId.add(element.drug_id!!)

            checkedItems.add(false)
        }

        // setup the alert builder
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Drugs")
        builder.setMultiChoiceItems(drugName.toTypedArray(), checkedItems.toBooleanArray())
        { dialog, which, isChecked ->
            checkedItems[which] = isChecked
        }
        // add OK and Cancel buttons
        builder.setPositiveButton("OK") { dialog, which ->
            val selDrugList = mutableListOf<DrugClassBinder>()
            for(i in 0 until drugList.size){
                val drugDet = drugList[i]
                val checked = checkedItems[i] //true or false
                val clickedDrugId = drugId[i]

                if (!checked){
//                        selDrugIds.remove(clickedDrugId)
                }else{
                    selDrugList.add(drugDet)
                }
            }


            if (selDrugList.size==0){
                ClassAlertDialog(context).toast("No drug selected...")
            }else{
                adapterCallbackInterface!!.onPrescribe(appsList, selDrugList)


//                Gson().toJson(mutableListOf(selDrugList))

            }
        }
        builder.setNegativeButton("Cancel", null)
        // create and show the alert progressDialog
        val dialog = builder.create()
        dialog.show()
    }


    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v){
        val appsWrapper = v.appsWrapper!!
        val appPatName = v.appPatName!!
        val appDate = v.appDate!!
        val appDocName = v.appDocName!!
        val appCancelBtn = v.appCancelBtn!!
        val appSymptomsBtn = v.appSymptomsBtn!!
        val appDoneBtn = v.appDoneBtn!!
        val appCancelledBtn = v.appCancelledBtn!!
        val appViewPrescriptionBtn = v.appViewPrescriptionBtn!!
        val appPrescribeBtn = v.appPrescribeBtn!!
    }



    //interface declaration
    interface AppAdapterCallbackInterface {
        fun onCancelApp(appsList: AppClassBinder)
        fun onPrescribe(appsList: AppClassBinder, selDrugIds:MutableList<DrugClassBinder>)
        fun onViewPrescription(appsList: AppClassBinder)
    }
}
