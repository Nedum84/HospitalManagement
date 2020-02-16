package com.hospitalmanagement

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import kotlinx.android.synthetic.main.adapter_apps.view.*
import kotlinx.android.synthetic.main.adapter_doctors_profile.view.*
import kotlinx.android.synthetic.main.adapter_doctors_profile2.view.*
import kotlinx.android.synthetic.main.adapter_my_orders.view.*
import kotlinx.android.synthetic.main.adapter_order_history.view.*
import kotlinx.android.synthetic.main.adapter_search_drugs.view.*
import org.json.JSONException
import org.json.JSONObject


class AppsAdapter(val list:MutableList<AppClassBinder>, val context: Context): RecyclerView.Adapter<AppsAdapter.ViewHolder>(){
    private val prefs = ClassSharedPreferences(context)

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
        holder.appDate.text     = "Date: ${ClassDateAndTime().getDateTime(listDetails.app_date!!.toLong())}"
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
        if((prefs.getUserLevel()=="2")&&(listDetails.app_status=="0")){//visible to doctors only
            holder.appPrescribeBtn.visibility = View.VISIBLE
        }






        holder.appCancelBtn.setOnClickListener {
            AlertDialog.Builder(context)
                    .setMessage("Cancel this appointment?")
                    .setPositiveButton("Yes"
                    ) { _, _ ->
                        cancelApp(listDetails)
                    }.setNegativeButton("Close"
                    ) { _, _ -> }
                    .show()
        }
        holder.appSymptomsBtn.setOnClickListener {
            ClassAlertDialog(context).alertMessage("${listDetails.pat_symptoms}","Symptoms")
        }

        holder.appViewPrescriptionBtn.setOnClickListener {
            val drugs = Gson().fromJson(listDetails.doc_prescription, Array<DrugClassBinder>::class.java).asList()

            val drugNames = drugs.map { it.drug_name }.toTypedArray()

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Drug Prescriptions")

            builder.setItems(drugNames,null)
            builder.setNegativeButton("Close"){ _, _ ->}

            val dialog = builder.create()
            dialog.show()
        }
        holder.appPrescribeBtn.setOnClickListener {
            getDrugs(listDetails)
        }


    }

    private fun getDrugs(appsList: AppClassBinder) {
        val allDrugs = ClassUtilities().getAllDrugs()
        showDrugsAlertDialog(appsList, allDrugs)
    }

    private fun getDrugs2(appsList: AppClassBinder){
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
            var selDrugList = mutableListOf<DrugClassBinder>()
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


            selDrugList = selDrugList.distinctBy { it.drug_id }.toMutableList()
            if (selDrugList.size==0){
                ClassAlertDialog(context).toast("No drug selected...")
            }else{
//                adapterCallbackInterface!!.onPrescribe(appsList, selDrugList)//not used now

                prescribeDrug(appsList, selDrugList)
            }
        }
        builder.setNegativeButton("Cancel", null)
        // create and show the alert progressDialog
        val dialog = builder.create()
        dialog.show()
    }

    private fun  cancelApp(appsList: AppClassBinder){
        //creating volley string request
        val progressDialog = ClassProgressDialog(context,"Cancelling Appointment, Please Wait...")
        progressDialog.createDialog()
        val stringRequest =  object : StringRequest(Request.Method.POST, UrlHolder.URL_CANCEL_APP,
                Response.Listener<String> { response ->
                    progressDialog.dismissDialog()

                    try {
                        val obj = JSONObject(response)
                        val responseMsg = obj.getString("message")

                        if (responseMsg=="ok") {
                            appsList.app_status = "1"

                            notifyDataSetChanged()
                        }else{
                            ClassAlertDialog(context).toast(responseMsg)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(context, e.printStackTrace().toString() + " Error", Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener { volleyError ->
                    progressDialog.dismissDialog()
                    Toast.makeText(context, volleyError.message, Toast.LENGTH_LONG).show()
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String?> {
                val params = java.util.HashMap<String, String?>()
                params["request_type"] = "cancel_app"
                params["app_id"] = appsList.app_id.toString()
                return params
            }
        }
        //adding request to queue
        VolleySingleton.instance?.addToRequestQueue(stringRequest)
        //volley interactions end
    }
    private fun  prescribeDrug(appsList: AppClassBinder, selDrugIds:MutableList<DrugClassBinder>){
        //creating volley string request
        val progressDialog = ClassProgressDialog(context)
        progressDialog.createDialog()
        val stringRequest =  object : StringRequest(Request.Method.POST, UrlHolder.URL_ADD_PRESCRIPTION,
                Response.Listener<String> { response ->
                    progressDialog.dismissDialog()

                    try {
                        val obj = JSONObject(response)
                        val responseMsg = obj.getString("message")

                        if (responseMsg=="ok") {
                            appsList.app_status = "2"

                            notifyDataSetChanged()
                        }else{
                            ClassAlertDialog(context).toast(responseMsg)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(context, e.printStackTrace().toString() + " Error", Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener { volleyError ->
                    progressDialog.dismissDialog()
                    Toast.makeText(context, volleyError.message, Toast.LENGTH_LONG).show()
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String?> {
                val params = java.util.HashMap<String, String?>()
                params["request_type"] = "add_prescription"
                params["app_id"] = appsList.app_id.toString()
                params["doc_prescription"] = Gson().toJson(mutableListOf(selDrugIds))
                return params
            }
        }
        //adding request to queue
        VolleySingleton.instance?.addToRequestQueue(stringRequest)
        //volley interactions end
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
//    interface AppAdapterCallbackInterface {
//        fun onPrescribe(appsList: AppClassBinder, selDrugIds:MutableList<DrugClassBinder>)
//    }
}
class DocsAdapter(val list:MutableList<UserClassBinder>, val context: Context): RecyclerView.Adapter<DocsAdapter.ViewHolder>(){
    private var adapterCallbackInterface: DocsAdapterCallbackInterface? = null


    init {
        try {
            adapterCallbackInterface = context as DocsAdapterCallbackInterface
        } catch (e: ClassCastException) {
            throw RuntimeException(context.toString() + "Activity must implement DocsAdapterCallbackInterface.", e)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(
                R.layout.adapter_doctors_profile,
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

    fun addItems(items: MutableList<UserClassBinder>) {
        val lastPos = list.size - 1
        list.addAll(items)
        notifyItemRangeInserted(lastPos, items.size)
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listDetails = list[position]

        holder.docName.text = "${listDetails.name}"
        holder.docProfile.text     = "A ${listDetails.speciality} with ${listDetails.age} years experience and handles different cases"
        LoadImg(context,holder.docPhoto).execute(listDetails.photo_url)


        holder.docSetAppBtn.setOnClickListener {
            adapterCallbackInterface?.onMakeApp()
        }

    }
    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v){
        val docWrapper = v.docWrapper!!
        val docPhoto = v.docPhoto!!
        val docProfile = v.docProfile!!
        val docSetAppBtn = v.docSetAppBtn!!
        val docName = v.docName!!
    }



    //interface declaration
    interface DocsAdapterCallbackInterface {
        fun onMakeApp()
    }
}
class MyOrdersAdapter(val list:MutableList<DrugClassBinder>, val context: Context): RecyclerView.Adapter<MyOrdersAdapter.ViewHolder>(){
    private var adapterCallbackInterface: OrdersAdapterCallbackInterface? = null


    init {
        try {
            adapterCallbackInterface = context as OrdersAdapterCallbackInterface
        } catch (e: ClassCastException) {
            throw RuntimeException(context.toString() + "Activity must implement OrdersAdapterCallbackInterface.", e)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(
                R.layout.adapter_my_orders,
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

    fun addItems(items: MutableList<DrugClassBinder>) {
        val lastPos = list.size - 1
        list.addAll(items)
        notifyItemRangeInserted(lastPos, items.size)
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listDetails = list[position]

        holder.orderDrugName.text = "Drug Name: ${listDetails.drug_name}"
        holder.orderQty.text     = "${listDetails.drug_qty}"
        holder.orderDrugUnitPrice.text = "Unit Price: ₦${listDetails.drug_price}"
        holder.orderTotal.text     = "Total: ₦${listDetails.drug_price.toInt()*listDetails.drug_qty}"


        holder.orderQtyMinus.setOnClickListener {
            if (listDetails.drug_qty>1){
                listDetails.drug_qty = listDetails.drug_qty-1

                //Updating...
                notifyDataSetChanged()

                adapterCallbackInterface?.onOrderUpdate(list)
            }
        }
        holder.orderQtyAdd.setOnClickListener {
            listDetails.drug_qty = listDetails.drug_qty+1

            //Updating...
            notifyDataSetChanged()

            adapterCallbackInterface?.onOrderUpdate(list)
        }
        holder.orderRemoveFromCartBtn.setOnClickListener {
            try {list.removeAt(position)} catch (e: Exception) {e.printStackTrace()}
            notifyItemRemoved(position)


            adapterCallbackInterface?.onOrderRemove(list)
        }

    }
    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v){
        val orderWrapper = v.orderWrapper!!
        val orderDrugName = v.orderDrugName!!
        val orderQtyMinus = v.orderQtyMinus!!
        val orderQty = v.orderQty!!
        val orderQtyAdd = v.orderQtyAdd!!
        val orderDrugUnitPrice = v.orderDrugUnitPrice!!
        val orderTotal = v.orderTotal!!
        val orderRemoveFromCartBtn = v.orderRemoveFromCartBtn!!
    }



    //interface declaration
    interface OrdersAdapterCallbackInterface {
        fun onOrderUpdate(drugList: MutableList<DrugClassBinder>)
        fun onOrderRemove(drugList: MutableList<DrugClassBinder>)
    }
}
class DrugsAdapter(val list:MutableList<DrugClassBinder>, val context: Context): RecyclerView.Adapter<DrugsAdapter.ViewHolder>(){



    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(
                R.layout.adapter_search_drugs,
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

    fun addItems(items: MutableList<DrugClassBinder>) {
        val lastPos = list.size - 1
        list.addAll(items)
        notifyItemRangeInserted(lastPos, items.size)
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listDetails = list[position]

        holder.drugName.text = listDetails.drug_name
        holder.drugDesc.text     = listDetails.drug_desc
        holder.drugPrice.text = "₦${listDetails.drug_price}.00"


        val curDrugOrders = (Gson().fromJson(ClassSharedPreferences(context).getOrderDetails(), Array<DrugClassBinder>::class.java).asList()).toMutableList()
        if (listDetails in curDrugOrders){
            holder.drugAddToCartBtn.visibility =View.GONE
            holder.drugRemoveFromCartBtn.visibility =View.VISIBLE
        }else{
            holder.drugAddToCartBtn.visibility =View.VISIBLE
            holder.drugRemoveFromCartBtn.visibility =View.GONE
        }

        holder.drugRemoveFromCartBtn.setOnClickListener {
            if (listDetails in curDrugOrders){
                curDrugOrders.remove(listDetails)
            }
            ClassSharedPreferences(context).setOrderDetails(Gson().toJson(curDrugOrders))

            //Updating...
            notifyDataSetChanged()
        }

        holder.drugAddToCartBtn.setOnClickListener {
            if (listDetails !in curDrugOrders){
                curDrugOrders.add(listDetails)
            }
            ClassSharedPreferences(context).setOrderDetails(Gson().toJson(curDrugOrders))

            //Updating...
            notifyDataSetChanged()
        }
        holder.drugWrapper.setOnClickListener {
            ClassUtilities().hideKeyboard(it, (context as Activity))
            ClassAlertDialog(context).alertMessage("${listDetails.drug_desc}","${listDetails.drug_name}")
        }

    }
    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v){
        val drugWrapper = v.drugWrapper!!
        val drugName = v.drugName!!
        val drugDesc = v.drugDesc!!
        val drugPrice = v.drugPrice!!
        val drugRemoveFromCartBtn = v.drugRemoveFromCartBtn!!
        val drugAddToCartBtn = v.drugAddToCartBtn!!
    }

}

class MyOrdersHistoryAdapter(val list:MutableList<MyOrderHistoryClassBinder>, val context: Context): RecyclerView.Adapter<MyOrdersHistoryAdapter.ViewHolder>(){



    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(
                R.layout.adapter_order_history,
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

    fun addItems(items: MutableList<MyOrderHistoryClassBinder>) {
        val lastPos = list.size - 1
        list.addAll(items)
        notifyItemRangeInserted(lastPos, items.size)
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listDetails = list[position]

        val drugOrders = (Gson().fromJson(listDetails.order_drugs, Array<DrugClassBinder>::class.java).asList()).toMutableList()

        holder.orderHistoryOrderNo.text = "Order No: #${listDetails.order_no}"
        holder.orderHistoryPrice.text = "₦${listDetails.order_price}.00"
        holder.orderHistoryNoOfDrugs.text = "${drugOrders.size} Products Ordered"

        holder.orderHistoryWrapper.setOnClickListener {

            val dOrders = drugOrders.map { it.drug_name +" - ${it.drug_qty}(₦${it.drug_price})"}.toTypedArray()

            val builder = AlertDialog.Builder(context)
            builder.setTitle("#${listDetails.order_no} Details")

            builder.setItems(dOrders,null)
            builder.setNegativeButton("Close"){ _, _ ->}

            val dialog = builder.create()
            dialog.show()
        }

    }
    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v){
        val orderHistoryWrapper = v.orderHistoryWrapper!!
        val orderHistoryOrderNo = v.orderHistoryOrderNo!!
        val orderHistoryNoOfDrugs = v.orderHistoryNoOfDrugs!!
        val orderHistoryPrice = v.orderHistoryPrice!!
        val orderHistoryDetailsBtn = v.orderHistoryDetailsBtn!!
    }

}

class DocsAdapter2(val list:MutableList<UserClassBinder>, val context: Context): RecyclerView.Adapter<DocsAdapter2.ViewHolder>(){
    private var adapterCallbackInterface: DocsAdapterCallbackInterface2? = null


    init {
        try {
            adapterCallbackInterface = context as DocsAdapterCallbackInterface2
        } catch (e: ClassCastException) {
            throw RuntimeException(context.toString() + "Activity must implement DocsAdapterCallbackInterface2.", e)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(
                R.layout.adapter_doctors_profile2,
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

    fun addItems(items: MutableList<UserClassBinder>) {
        val lastPos = list.size - 1
        list.addAll(items)
        notifyItemRangeInserted(lastPos, items.size)
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listDetails = list[position]

        holder.doc2Name.text = "${listDetails.name}"
        holder.doc2AreaOfSpeciality.text     = "Speciality: ${listDetails.speciality}"
        holder.doc2YrOfExp.text     = "Speciality: ${listDetails.age}"
        LoadImg(context,holder.doc2Photo).execute(listDetails.photo_url)


        holder.doc2RemDocBtn.setOnClickListener {
            AlertDialog.Builder(context)
                    .setMessage("Remove this Doctor?")
                    .setPositiveButton("Remove"
                    ) { _, _ ->
                        adapterCallbackInterface?.onRemoveDoc(listDetails)

                    }.setNegativeButton("Cancel"
                    ) { _, _ -> }
                    .show()
        }

    }
    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v){
        val doc2Wrapper = v.doc2Wrapper!!
        val doc2Photo = v.doc2Photo!!
        val doc2Name = v.doc2Name!!
        val doc2AreaOfSpeciality = v.doc2AreaOfSpeciality!!
        val doc2YrOfExp = v.doc2YrOfExp!!
        val doc2RemDocBtn = v.doc2RemDocBtn!!
    }



    //interface declaration
    interface DocsAdapterCallbackInterface2 {
        fun onRemoveDoc(user_details:UserClassBinder)
    }
}