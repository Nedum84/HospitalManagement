package com.hospitalmanagement

import android.app.Activity
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.r0adkll.slidr.Slidr
import kotlinx.android.synthetic.main.activity_my_orders.*
import kotlinx.android.synthetic.main.alert_dialog_payment_with_atm.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class ActivityMyOrders : AppCompatActivity(), MyOrdersAdapter.OrdersAdapterCallbackInterface
        , FragmentDialogSearchDrugs.FragmentDialogSearchDrugInteractionListener, FragmentDialogLoginRegister.FragmentStRegisterInteractionListener {

    private var drugList = mutableListOf<DrugClassBinder>()

    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var ADAPTER : MyOrdersAdapter
    lateinit var thisContext:Activity



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        thisContext = this
        Slidr.attach(this)//for slidr swipe lib


        linearLayoutManager = LinearLayoutManager(thisContext)
        ADAPTER = MyOrdersAdapter(drugList,thisContext)
        my_orders_recyclerview.layoutManager = linearLayoutManager
        my_orders_recyclerview.itemAnimator = DefaultItemAnimator()
        my_orders_recyclerview.adapter = ADAPTER

        addToCartBtn.setOnClickListener {
            //sow cart fragment
            searchDrugDialogShow()
        }
        checkoutBtn.setOnClickListener {
            if (totalPrice==0){
                ClassAlertDialog(thisContext).toast("No order in your cart...")
            }else{
                if (!ClassSharedPreferences(thisContext).isLoggedIn()) {
                    AlertDialog.Builder(this)
                            .setTitle("Login Required!")
                            .setMessage("You need to login to complete your order")
                            .setPositiveButton("Login"
                            ) { _, _ ->
                                logRegDialogShow()

                            }.setNegativeButton("Cancel"
                            ) { _, _ -> }
                            .show()
                }else{
                    paymentDialog()
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        loadDrugs()
    }

    var paymentAlertDialog: AlertDialog? = null
    private fun paymentDialog(){
        val inflater = LayoutInflater.from(thisContext).inflate(R.layout.alert_dialog_payment_with_atm, null)
        val builder = AlertDialog.Builder(thisContext)

        builder.setView(inflater)
        paymentAlertDialog = builder.create()
        paymentAlertDialog?.show()

        inflater.payment_total_amount.text = "NGN $totalPrice.00"
        inflater.payment_total_amount2.text = "Pay NGN $totalPrice"
        inflater.button_perform_local_transaction.setOnClickListener {
            if (toString(inflater.edit_card_number).isEmpty()){
                ClassAlertDialog(thisContext).toast("Enter the card number...")
            }else{
                makePayment()
            }
        }
    }
    private fun makePayment(){
        //creating volley string request
        val pDialog= ClassProgressDialog(thisContext)
        pDialog.createDialog()
        val stringRequest = object : StringRequest(Request.Method.POST, UrlHolder.URL_MAKE_ORDER_PAYMENT,
                Response.Listener<String> { response ->
                    pDialog.dismissDialog()

                    try {

                        val obj = JSONObject(response)
                        val clrStatus = obj.getString("order_status");
                        if (clrStatus == "ok") {
                            ClassAlertDialog(thisContext).toast("Order placed successfully...")

                            paymentAlertDialog?.dismiss()
                            ClassSharedPreferences(thisContext).setOrderDetails(Gson().toJson(mutableListOf<DrugClassBinder>()))
                            refreshList()
                        } else {
                            ClassAlertDialog(thisContext).toast(clrStatus)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { volleyError ->
                    pDialog.dismissDialog()
                    ClassAlertDialog(thisContext).toast("ERROR IN NETWORK CONNECTION!")
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["request_type"] = "add_order"
                params["order_price"] = "$totalPrice"
                params["order_drugs"] = ClassSharedPreferences(thisContext).getOrderDetails()
                params["pat_id"] = ClassSharedPreferences(thisContext).getUserId()!!
                return params
            }
        }

        //adding request to queue
        VolleySingleton.instance?.addToRequestQueue(stringRequest)
        //volley interactions end
    }


    private fun toString(editText: EditText):String{
        return editText.text.toString().trim()
    }

    private fun refreshList(){
        drugList.clear()
        ADAPTER.addItems(drugList)
        ADAPTER.notifyDataSetChanged()
        loadDrugs()
    }


    private fun loadDrugs(){
        val curDrugOrders = (Gson().fromJson(ClassSharedPreferences(thisContext).getOrderDetails(), Array<DrugClassBinder>::class.java).asList()).toMutableList()

        if (curDrugOrders.size!=0){
            ADAPTER.addItems(curDrugOrders)
            updateTotalPrice(curDrugOrders)


            no_data_tag.visibility = View.GONE
            order_footer.visibility = View.VISIBLE
        }else{
            no_data_tag.visibility = View.VISIBLE
            order_footer.visibility = View.GONE
        }
    }

    var totalPrice = 0
    private fun updateTotalPrice(curDrugOrders: MutableList<DrugClassBinder>) {
        totalPrice = 0
        for (p in curDrugOrders){
            totalPrice += p.drug_price.toInt()*p.drug_qty
        }
        totalPriceShow.text     = "Total: ₦$totalPrice.00"


        //for empty list
        if (curDrugOrders.size==0){
            no_data_tag.visibility = View.VISIBLE
            order_footer.visibility = View.GONE
        }
    }


    //from drug
    override fun onOrderUpdate(drugList: MutableList<DrugClassBinder>) {
        ClassSharedPreferences(thisContext).setOrderDetails(Gson().toJson(drugList))
        updateTotalPrice(drugList)//updating data...
    }

    override fun onOrderRemove(drugList: MutableList<DrugClassBinder>) {
        ClassSharedPreferences(thisContext).setOrderDetails(Gson().toJson(drugList))
        updateTotalPrice(drugList)//
    }
    //from drug order adapter
    override fun onCloseSearchDrugDialog() {
        refreshList()
    }
    //for successful login
    override fun onRegSuccessful() {
        finish()
        startActivity(intent)
    }



    //search drug frag dialog
    private val dialogFragmentSearchDrug = FragmentDialogSearchDrugs()
    private fun searchDrugDialogShow(){
        if(dialogFragmentSearchDrug.isAdded)return

        val ft = supportFragmentManager.beginTransaction()
//        val prev = supportFragmentManager.findFragmentByTag("dialog")
        val prev = supportFragmentManager.findFragmentByTag(FragmentDialogSearchDrugs::class.java.name)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        dialogFragmentSearchDrug.show(ft, FragmentDialogSearchDrugs::class.java.name)
    }


    //login/register frag dialog class
    private val dialogFragmentLogRegister = FragmentDialogLoginRegister()
    private fun logRegDialogShow(){
        if(dialogFragmentLogRegister.isAdded)return

        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag(FragmentDialogLoginRegister::class.java.name)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        dialogFragmentLogRegister.show(ft, FragmentDialogLoginRegister::class.java.name)
    }


    override fun onBackPressed() {
        backPressed()
    }
    private fun backPressed(){
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_activity_my_order, menu)
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                backPressed()
            }
            R.id.action_menu_go_back ->{
                backPressed()
            }
            R.id.action_menu_search_drug ->{
                searchDrugDialogShow()
            }
            R.id.action_menu_search_drug2 ->{
                searchDrugDialogShow()
            }
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }


}
