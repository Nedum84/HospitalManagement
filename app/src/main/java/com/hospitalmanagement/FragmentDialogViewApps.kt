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
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.fragment_dialog_view_apps.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class FragmentDialogViewApps: DialogFragment() {
    var listener: FragmentDialogMyAppsInteractionListener? = null
    lateinit var thisContext: Activity

    private var appList = mutableListOf<AppClassBinder>()
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var ADAPTER : AppsAdapter



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_view_apps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisContext = activity!!

        closeDialogFrag.setOnClickListener {
            dialog!!.dismiss()
        }


        linearLayoutManager = LinearLayoutManager(thisContext)
        ADAPTER = AppsAdapter(appList,thisContext)
        apps_recyclerview.layoutManager = linearLayoutManager
        apps_recyclerview.itemAnimator = DefaultItemAnimator()
        apps_recyclerview.adapter = ADAPTER

        addAppointmentBtn.setOnClickListener {
            dialog!!.dismiss()
            listener!!.onAddAppointment()
        }
        tapToRetry.setOnClickListener {
            refreshList()
        }
    }

    override fun onStart() {
        super.onStart()
        refreshList()
    }

    private fun refreshList(){
        appList.clear()
        ADAPTER.addItems(appList)
        ADAPTER.notifyDataSetChanged()
        loadApps()
    }

    private fun loadApps(){
        //creating volley string request
        loadingProgressbar?.visibility = View.VISIBLE
        no_network_tag?.visibility = View.GONE
        no_data_tag.visibility = View.GONE
        val stringRequest = object : StringRequest(Method.POST, UrlHolder.URL_GET_APPS,
                Response.Listener<String> { response ->
                    loadingProgressbar?.visibility = View.GONE

                    try {
                        val obj = JSONObject(response)
                        if (!obj.getBoolean("error")) {
                            val jsonResponse = obj.getJSONArray("appsz_arraysz")

                            if ((jsonResponse.length()!=0)){
                                val qDataArray = mutableListOf<AppClassBinder>()
                                for (i in 0 until jsonResponse.length()) {
                                    val jsonObj = jsonResponse.getJSONObject(i)
                                    val subject = AppClassBinder(
                                            jsonObj.getInt("app_id"),
                                            jsonObj.getString("app_date"),
                                            jsonObj.getString("pat_id"),
                                            jsonObj.getString("pat_name"),
                                            jsonObj.getString("pat_symptoms"),
                                            jsonObj.getString("doc_prescription"),
                                            jsonObj.getString("doc_id"),
                                            jsonObj.getString("doc_name"),
                                            jsonObj.getString("app_status")
                                    )
                                    if (subject !in appList)qDataArray.add(subject)
                                }
                                ADAPTER.addItems(qDataArray)

                            }else{
//                                ClassAlertDialog(thisContext).toast("No data found...")
                                no_data_tag?.visibility = View.VISIBLE
                                if (ClassSharedPreferences(thisContext).getUserLevel()=="1"||ClassSharedPreferences(thisContext).getUserLevel()=="4"){
                                    addAppointmentBtn.visibility = View.VISIBLE
                                }else{
                                    addAppointmentBtn.visibility = View.GONE
                                }
                            }
                        } else {
                            ClassAlertDialog(thisContext).toast("An error occurred, try again...")
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { _ ->
                    loadingProgressbar?.visibility = View.GONE
                    no_network_tag?.visibility = View.VISIBLE
                    ClassAlertDialog(thisContext).toast("No Network Connection...")
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String?> {
                val params = HashMap<String, String?>()
                params["request_type"] = "get_apps"
                params["user_id"] = ClassSharedPreferences(thisContext).getUserId()
                return params
            }
        }
        VolleySingleton.instance?.addToRequestQueue(stringRequest)//adding request to queue
        //volley interactions end

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
        dialog.window!!.attributes.windowAnimations = R.style.Animation_WindowSlideUpDown
//        isCancelable = false
        return dialog
    }


    //Fragment communication with the Home Activity Starts
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentDialogMyAppsInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface FragmentDialogMyAppsInteractionListener {
        fun onAddAppointment()
    }
    //Fragment communication with the Home Activity Stops
}

