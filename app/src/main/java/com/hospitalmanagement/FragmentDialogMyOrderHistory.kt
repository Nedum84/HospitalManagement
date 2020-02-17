package com.hospitalmanagement


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.fragment_dialog_my_order_history.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class FragmentDialogMyOrderHistory: DialogFragment() {
    var listener: FragmentDialogOrdersHistoryInteractionListener? = null
    lateinit var thisContext: Activity

    private var myOrderHistoryList = mutableListOf<MyOrderHistoryClassBinder>()
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var ADAPTER : MyOrdersHistoryAdapter



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_my_order_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisContext = activity!!

        closeDialogFrag.setOnClickListener {
            dialog!!.dismiss()
        }


        linearLayoutManager = LinearLayoutManager(thisContext)
        ADAPTER = MyOrdersHistoryAdapter(myOrderHistoryList,thisContext)
        order_history_recyclerview.layoutManager = linearLayoutManager
        order_history_recyclerview.itemAnimator = DefaultItemAnimator()
        order_history_recyclerview.adapter = ADAPTER

        orderDrugsBtn.setOnClickListener {
            dialog!!.dismiss()
            listener!!.onAddOrders()
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
        myOrderHistoryList.clear()
        ADAPTER.addItems(myOrderHistoryList)
        ADAPTER.notifyDataSetChanged()
        loadApps()
    }

    private fun loadApps(){
        //creating volley string request
        loadingProgressbar?.visibility = View.VISIBLE
        no_network_tag?.visibility = View.GONE
        no_data_tag.visibility = View.GONE
        val stringRequest = object : StringRequest(Method.POST, UrlHolder.URL_ORDER_HISTORY,
                Response.Listener<String> { response ->
                    loadingProgressbar?.visibility = View.GONE

                    try {
                        val obj = JSONObject(response)
                        if (!obj.getBoolean("error")) {
                            val jsonResponse = obj.getJSONArray("orders_arraysz")

                            if ((jsonResponse.length()!=0)){
                                val qDataArray = mutableListOf<MyOrderHistoryClassBinder>()
                                for (i in 0 until jsonResponse.length()) {
                                    val jsonObj = jsonResponse.getJSONObject(i)
                                    val data = MyOrderHistoryClassBinder(
                                            jsonObj.getString("order_id"),
                                            jsonObj.getString("order_no"),
                                            jsonObj.getString("order_price"),
                                            jsonObj.getString("order_drugs"),
                                            jsonObj.getString("pat_id")
                                    )
                                    if (data !in myOrderHistoryList)qDataArray.add(data)
                                }
                                ADAPTER.addItems(qDataArray)

                            }else{
                                no_data_tag?.visibility = View.VISIBLE
                                orderDrugsBtn.visibility = View.VISIBLE
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
                params["request_type"] = "get_orders"
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
        if (context is FragmentDialogOrdersHistoryInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface FragmentDialogOrdersHistoryInteractionListener {
        fun onAddOrders()
    }
    //Fragment communication with the Home Activity Stops
}


