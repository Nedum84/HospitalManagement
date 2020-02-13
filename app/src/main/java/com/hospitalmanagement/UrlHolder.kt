package com.hospitalmanagement

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat


object  UrlHolder{
        private const val URL_ROOT = "http://192.168.44.236/test/"
//    private const val URL_ROOT = "https://ven10.co/assessment/"
//    private const val URL_ROOT = "http://unnelection.com.ng/test/"



    val URL_GET_DRUGS: String?  = URL_ROOT + "get_drugs.php"
    val URL_CANCEL_APP: String?  = URL_ROOT + "cancel_app.php"
    val URL_GET_APPS: String?  = URL_ROOT + "get_apps.php"
    val URL_ADD_PRESCRIPTION: String?  = URL_ROOT + "add_prescription.php"
    val URL_ADD_APP: String?  = URL_ROOT + "add_app.php"
    val URL_LOGIN_REGISTER: String?  = URL_ROOT + "reg_log.php"
}