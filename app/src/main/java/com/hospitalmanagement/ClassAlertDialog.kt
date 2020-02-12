package com.hospitalmanagement

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import android.app.AlertDialog
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View


class ClassAlertDialog(var context:Context) {

    init {
        val alertDialog:AlertDialog?=null
    }

    fun alertMessage(displayMsg:String,title:String=""){
        if(title=="")
            AlertDialog.Builder(context)
                    .setMessage(displayMsg)
                    .setPositiveButton("Ok"
                    ) { _, id ->
                    }.setCancelable(false)
                    .show()
        else
            AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(displayMsg)
                    .setPositiveButton("Ok"
                    ) { _, id ->
                    }.setCancelable(false)
                    .show()
    }
    fun snackBarMsg(view:View,msg :String= "No Internet Connection"){
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }

    fun toast(msg:String){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

}