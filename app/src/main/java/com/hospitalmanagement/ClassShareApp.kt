package com.hospitalmanagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.util.ArrayList
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity


class ClassShareApp(val context: Context) {
//    private lateinit var getNewsRow:NewsListClassBinder

    private val appPackageName: String? = context.packageName // getPackageName() from Context or Activity object
    val intent: Intent = Intent()
    private val shareAppMsg: String =
            "Get \"${context.getString(R.string.app_name)}\" on Google Play Store via \n" +
                    "https://play.google.com/store/apps/details?id=$appPackageName"

    init {
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
    }

    fun shareApp() {
        intent.putExtra(Intent.EXTRA_TEXT, shareAppMsg)
        startActivity(context, Intent.createChooser(intent, "Share to: "), Bundle())
    }

}