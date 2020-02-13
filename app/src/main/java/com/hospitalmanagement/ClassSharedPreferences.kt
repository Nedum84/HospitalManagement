package com.hospitalmanagement

import android.content.Context
import com.google.gson.Gson
import java.util.HashSet

class  ClassSharedPreferences(val context: Context?){

    private val PREFERENCE_NAME = "hospital_management_preference"
    private val PREFERENCE_CURRENT_ORDER_DETAILS = "current_order_details"



    private val PREFERENCE_USER_JSON_DETAILS = "user_json_details"
    private val PREFERENCE_ALL_USERS_JSON_DETAILS = "all_users_json_details"
    private val PREFERENCE_USER_LEVEL = "user_level"
    private val PREFERENCE_USER_ID = "user_id"

    private val preference = context?.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)!!

    //set Orders Cart Details
    fun setOrderDetails(data: String){
        val editor = preference.edit()
        editor.putString(PREFERENCE_CURRENT_ORDER_DETAILS,data)
        editor.apply()
    }
    fun getOrderDetails():String{
        return  preference.getString(PREFERENCE_CURRENT_ORDER_DETAILS, Gson().toJson(mutableListOf<DrugClassBinder>()))!!
    }




    //USER DETAILS

    //set user details arrays in JSON
    fun setUserJSONDetails(data:String){
        val editor = preference.edit()
        editor.putString(PREFERENCE_USER_JSON_DETAILS,data)
        editor.apply()
    }
    private fun getUserJSONDetails():String?{
        return  preference.getString(PREFERENCE_USER_JSON_DETAILS, "")//Gson().toJson(mutableListOf<UserClassBinder>())
    }
    //set all the users details arrays in JSON
    fun setAllUsersJSONDetails(data:String){
        val editor = preference.edit()
        editor.putString(PREFERENCE_ALL_USERS_JSON_DETAILS,data)
        editor.apply()
    }
    fun getAllUsersJSONDetails():String?{
        return  preference.getString(PREFERENCE_ALL_USERS_JSON_DETAILS, Gson().toJson(mutableListOf<UserClassBinder>()))
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