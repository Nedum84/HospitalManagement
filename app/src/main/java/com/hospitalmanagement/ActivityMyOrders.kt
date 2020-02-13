package com.hospitalmanagement

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_my_orders.*

class ActivityMyOrders : AppCompatActivity(), MyOrdersAdapter.OrdersAdapterCallbackInterface, FragmentDialogSearchDrugs.FragmentDialogSearchDrugInteractionListener {

    private var drugList = mutableListOf<DrugClassBinder>()

    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var ADAPTER : MyOrdersAdapter
    lateinit var thisContext:Activity



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)
        thisContext = this


        linearLayoutManager = LinearLayoutManager(thisContext)
        ADAPTER = MyOrdersAdapter(drugList,thisContext)
        my_orders_recyclerview.layoutManager = linearLayoutManager
        my_orders_recyclerview.itemAnimator = DefaultItemAnimator()
        my_orders_recyclerview.adapter = ADAPTER
        loadDrugs()

        addToCartBtn.setOnClickListener {
            //sow cart fragment
            searchDrugDialogShow()
        }
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

    private fun updateTotalPrice(curDrugOrders: MutableList<DrugClassBinder>) {
        var totalPrice = 0
        for (p in curDrugOrders){
            totalPrice += p.drug_price.toInt()*p.drug_qty
        }
        totalPriceShow.text     = "Total: ₦$totalPrice.00"
    }

    override fun onOrderUpdate(drugList: MutableList<DrugClassBinder>) {
        ClassSharedPreferences(thisContext).setOrderDetails(Gson().toJson(drugList))
    }

    override fun onOrderRemove(drugList: MutableList<DrugClassBinder>) {
        ClassSharedPreferences(thisContext).setOrderDetails(Gson().toJson(drugList))
    }
    override fun onCloseSearchDrugDialog() {
        refreshList()
    }


    //student clearance
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
}
