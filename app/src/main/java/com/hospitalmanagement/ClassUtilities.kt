package com.hospitalmanagement

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject


class ClassUtilities() {

    fun lockScreen(context: Context?){
        //lock screen
        val orientation = context!!.resources.configuration.orientation//activity!!.requestedOrientation(to get 10 more values)
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // code for portrait mode
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            // code for landscape mode
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        //lock screen
//        OR
//        activity!!.requestedOrientation = orientation
        //OR
//        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }
    fun unlockScreen(context: Context?){
        try {
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } catch (e: Exception) {}
    }

    fun hideKeyboard(view: View?,activity: Activity){
//        val view = currentFocus
        try {
            if(view != null){
                val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
            activity.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        } catch (e: Exception) {}
    }

    fun getDoctors(context: Context?, speciality_id:String=""):MutableList<UserClassBinder>{
        val allUsers = (Gson().fromJson(ClassSharedPreferences(context).getAllUsersJSONDetails(), Array<UserClassBinder>::class.java).asList())
        val selUsers = mutableListOf<UserClassBinder>()
        for (u in allUsers){
            if (u.user_level !="2")
                continue

            if (speciality_id.isNotEmpty()){
                if (u.speciality_id !=speciality_id)
                    continue
            }

            selUsers.add(u)
        }

        return selUsers.distinct().toMutableList()
    }
    fun getAllDrugs():MutableList<DrugClassBinder>{
        val return_ = mutableListOf<DrugClassBinder>()
        try {
            val obj = JSONObject(UrlHolder.allDrugs)
            if (!obj.getBoolean("error")) {
                val jsonResponse = obj.getJSONArray("drugsz_arraysz")

                if ((jsonResponse.length()!=0)){

                    for (i in 0 until jsonResponse.length()) {
                        val jsonObj = jsonResponse.getJSONObject(i)
                        val subject = DrugClassBinder(
                                jsonObj.getString("drug_id"),
                                jsonObj.getString("drug_name"),
                                jsonObj.getString("drug_desc"),
                                jsonObj.getString("drug_price")
                        )
                        return_.add(subject)
                    }
                }
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return return_.distinct().toMutableList()
    }



}