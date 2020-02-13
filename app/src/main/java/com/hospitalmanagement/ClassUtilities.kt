package com.hospitalmanagement

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson


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

    fun getDoctors(context: Context?,speality_id:String=""):MutableList<UserClassBinder>{
        val allUsers = (Gson().fromJson(ClassSharedPreferences(context).getAllUsersJSONDetails(), Array<UserClassBinder>::class.java).asList()).toMutableList()
        val selUsers = mutableListOf<UserClassBinder>()
        for (u in allUsers){
            if (u.user_level !="3")
                continue

            if (speality_id.isNotEmpty()){
                if (u.speciality_id !=speality_id)
                    continue
            }

            selUsers.add(u)
        }

        return selUsers.distinct().toMutableList()
    }


}