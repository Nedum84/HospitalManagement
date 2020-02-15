package com.hospitalmanagement


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.fragment_dialog_search_drugs.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class FragmentDialogSearchDrugs: DialogFragment() {
    var listener: FragmentDialogSearchDrugInteractionListener? = null
    lateinit var thisContext: Activity

    var isLoadingDataFromServer = false  //for checking when data fetching is going on

    private var dList = mutableListOf<DrugClassBinder>()
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var ADAPTER : DrugsAdapter



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_search_drugs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisContext = activity!!

        closeDialogFrag.setOnClickListener {
            dialog!!.dismiss()
            listener!!.onCloseSearchDrugDialog()
        }


        linearLayoutManager = LinearLayoutManager(thisContext)
        ADAPTER = DrugsAdapter(dList,thisContext)
        searchDRecyclerview.layoutManager = linearLayoutManager
        searchDRecyclerview.itemAnimator = DefaultItemAnimator()
        searchDRecyclerview.adapter = ADAPTER

        loadSearchedDrug("")
        searchListeners()
    }
    private fun searchListeners(){
        questionSearchView.queryHint = "Search for a drug..."
        questionSearchView.requestFocus()
        questionSearchView.setIconifiedByDefault(false)

        val searchBg = questionSearchView.findViewById(androidx.appcompat.R.id.search_plate) as View
        searchBg.background = null

        questionSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(!isLoadingDataFromServer){
                    ClassUtilities().hideKeyboard(questionSearchView, thisContext)
                    isLoadingDataFromServer = true
                    loadSearchedDrug(query!!)
                }

                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                isLoadingDataFromServer = true
                loadSearchedDrug(query.trim())

                return true
            }
        })
    }

    private fun loadSearchedDrug(search_query:String) {
        //creating volley string request
        no_question_search_tag?.visibility = View.VISIBLE
        no_question_search_tag.text = "Searching..."
        val stringRequest = object : StringRequest(Request.Method.POST, UrlHolder.URL_GET_DRUGS,
                com.android.volley.Response.Listener<String> { response ->
                    isLoadingDataFromServer = false
                    refreshAdapter()

                    try {
                        val obj = JSONObject(response)
                        if (!obj.getBoolean("error")) {
                            val jsonResponse = obj.getJSONArray("drugsz_arraysz")

                            if ((jsonResponse.length()!=0)){
                                no_question_search_tag?.visibility = View.GONE

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
                                ADAPTER.addItems(qDataArray)

                            }else{
                                no_question_search_tag?.visibility = View.VISIBLE
                                no_question_search_tag?.text = ClassHtmlFormater().fromHtml("No result found for <b><i>\"$search_query\"</i></b> ")
                            }
                        } else{
                            ClassAlertDialog(thisContext).toast("An error occurred, try again...")
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                com.android.volley.Response.ErrorListener { vError ->
                    vError.printStackTrace()
                    isLoadingDataFromServer = false

                    if(dList.size == 0){
                        no_question_search_tag?.visibility = View.VISIBLE
                        no_question_search_tag?.text = "NO NETWORK! "
                    }else{
                        ClassAlertDialog(thisContext).toast("No internet connection...")
                        no_question_search_tag?.visibility = View.GONE
                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String?> {
                val params = HashMap<String, String?>()
                params.put("request_type", "get_drugs")
                params.put("search_query", search_query)
                return params
            }
        }
        VolleySingleton.instance?.addToRequestQueue(stringRequest)//adding request to queue
        //volley interactions end
    }

    private fun loadSearchedDrug2(search_query:String) {
        no_question_search_tag?.visibility = View.VISIBLE
        no_question_search_tag.text = "Searching..."
        refreshAdapter()

        val allDrugs = ClassUtilities().getAllDrugs()
        val dataArray = mutableListOf<DrugClassBinder>()
        for (d in allDrugs){
            if (search_query.isNotEmpty()){
                if(search_query !in d.drug_name){
                    continue
                }
            }
            dataArray.add(d)
        }

        if ((dataArray.size!=0)){
            no_question_search_tag?.visibility = View.GONE
            ADAPTER.addItems(dataArray)

        }else{
            no_question_search_tag?.visibility = View.VISIBLE
            no_question_search_tag?.text = ClassHtmlFormater().fromHtml("No drug found for <b><i>\"$search_query\"</i></b> ")
        }
    }

    fun refreshAdapter(){
        dList.clear()
        ADAPTER.addItems(dList)
        ADAPTER.notifyDataSetChanged()
    }






    override fun onResume() {
        super.onResume()
        //lock screen
        ClassUtilities().lockScreen(thisContext)//Lock Screen rotation...
    }

    override fun onPause() {
        super.onPause()
        ClassUtilities().unlockScreen(thisContext)//un Lock Screen rotation...
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        } else {
            setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_NoActionBar)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        isCancelable = false
        return dialog
    }


    //Fragment communication with the Home Activity Starts
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentDialogSearchDrugInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface FragmentDialogSearchDrugInteractionListener {
        fun onCloseSearchDrugDialog()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener!!.onCloseSearchDrugDialog()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener!!.onCloseSearchDrugDialog()
    }
    //Fragment communication with the Home Activity Stops
}

