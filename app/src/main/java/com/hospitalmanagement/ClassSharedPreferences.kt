package com.hospitalmanagement

import android.content.Context
import java.util.HashSet

class  ClassSharedPreferences(val context: Context?){

    private val PREFERENCE_NAME = "hospital_management_preference"
//    private val PREFERENCE_CURRENT_CLEARANCE_ID = "current_clearance_id"



    private val PREFERENCE_USER_JSON_DETAILS = "user_json_details"
    private val PREFERENCE_USER_LEVEL = "user_level"
    private val PREFERENCE_USER_ID = "user_id"

    private val preference = context?.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)!!

    //set setCurrentClearanceId
    fun setCurrentClearanceDetail(data: String){
        val editor = preference.edit()
        editor.putString(PREFERENCE_CURRENT_CLEARANCE_ID,data)
        editor.apply()
    }
    fun getCurrentClearanceDetail():String{
        return  preference.getString(PREFERENCE_CURRENT_CLEARANCE_ID,"")!!
    }




    //USER DETAILS

    //set user details arrays in JSON
    fun setUserJSONDetails(data:String){
        val editor = preference.edit()
        editor.putString(PREFERENCE_USER_JSON_DETAILS,data)
        editor.apply()
    }
    fun getUserJSONDetails():String?{
        return  preference.getString(PREFERENCE_USER_JSON_DETAILS,"")
    }
    //set set Id
    fun setUserId(data:String?){
        val editor = preference.edit()
        editor.putString(PREFERENCE_USER_ID,data)
        editor.apply()
    }
    fun getUserId():String?{
        return  preference.getString(PREFERENCE_USER_ID,"")
    }

    //set setUserLevel
    fun setUserLevel(data:String?){
        val editor = preference.edit()
        editor.putString(PREFERENCE_USER_LEVEL,data)
        editor.apply()
    }
    fun getUserLevel():String?{
        return  preference.getString(PREFERENCE_USER_LEVEL,"")
    }
    fun isLoggedIn():Boolean{
        return getUserJSONDetails()!=""
    }

}