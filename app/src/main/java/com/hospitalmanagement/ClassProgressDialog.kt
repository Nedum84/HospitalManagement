package com.hospitalmanagement

import android.app.AlertDialog
import android.content.Context
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View

class ClassProgressDialog(var context: Context?, var text:String?="Please Wait...", var cancelable:Boolean = false) {
    private var alertDialog:AlertDialog? = null
    var builder:AlertDialog.Builder = AlertDialog.Builder(context)
    var dialogView:View = LayoutInflater.from(context).inflate(R.layout.progress_dialog,null)
    var message:TextView

    init {
        //Alert Dialog declaration starts
        message = dialogView.findViewById(R.id.message)
        message.text = text
        builder.setView(dialogView)
        builder.setCancelable(cancelable)
        try {
            alertDialog = builder.create()
        } catch (e: Exception) {
        }
    }

    fun createDialog(){
//        ClassUtilities().lockScreen(context)
        alertDialog?.show()
    }
    fun dismissDialog(){
//        try {
//            ClassUtilities().unlockScreen(context)
//        } catch (e: Exception) { }

        try {
            alertDialog?.dismiss()
        } catch (e: Exception) {
        }
    }
    fun updateMsg(msg:String){
        message.text = msg
    }

}