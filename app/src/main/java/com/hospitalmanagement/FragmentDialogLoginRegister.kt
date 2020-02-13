package com.hospitalmanagement

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import android.content.Context
import android.widget.*
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Window
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_dialog_login_register.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*


class FragmentDialogLoginRegister: DialogFragment(){
    lateinit var thisContext: Activity
    private var listener: FragmentStRegisterInteractionListener? = null

    var regGender: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_login_register, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisContext = activity!!

        closeDialogFrag.setOnClickListener {
            dialog!!.dismiss()
        }

        registerBtn.setOnClickListener {
            processRegistration()
        }
        loginBtn.setOnClickListener {
            processLogin()
        }
        alreadyRegisteredBtn.setOnClickListener {
            log_reg_title.text = "Patient Login"
            regWrapper.visibility = View.GONE
            logWrapper.visibility = View.VISIBLE
        }
        notRegisteredBtn.setOnClickListener {
            log_reg_title.text = "Patient Registration"
            regWrapper.visibility = View.VISIBLE
            logWrapper.visibility = View.GONE
        }


        initializeData()
    }
    private fun toString(editText: EditText):String{
        return editText.text.toString().trim()
    }
    private fun checkEmpty(string: String):Boolean{
        return string.isEmpty()
    }


    private fun processRegistration() {
        val regName = toString(reg_name)
        val regPhone = toString(reg_phone)
        val regAge = toString(reg_age)
        val regAddr = toString(reg_addr)
        val regPassword = toString(reg_password)

        if(checkEmpty(regName)){
            ClassAlertDialog(thisContext).toast("Your name is required")
        }else if(checkEmpty(regPhone)){
            ClassAlertDialog(thisContext).toast("Enter your phone number")
        }else if(checkEmpty(regAge)){
            ClassAlertDialog(thisContext).toast("Enter your age")
        }else if(checkEmpty(regAddr)){
            ClassAlertDialog(thisContext).toast("Your address is required")
        }else if(regGender == "Gender"){
            ClassAlertDialog(thisContext).toast("Select your gender...")
        }else if(regPassword.length<4){
            ClassAlertDialog(thisContext).toast("Password too short...")
        }else{

            //creating volley string request
            val dialog= ClassProgressDialog(context)
            dialog.createDialog()
            val stringRequest = object : StringRequest(Request.Method.POST, UrlHolder.URL_LOGIN_REGISTER,
                    Response.Listener<String> { response ->
                        dialog.dismissDialog()

                        try {

                            val obj = JSONObject(response)
                            val regStatus = obj.getString("reg_status");
                            if (regStatus == "ok") {
                                ClassAlertDialog(thisContext).toast("Registration successful...")

                                val userDetails = obj.getJSONArray("userDetails").getJSONObject(0)
                                val allUserDetails = obj.getJSONArray("all_usersz_details")
                                saveUserDetails(userDetails)
                                saveAllUsersDetails(allUserDetails)
                            } else {
                                ClassAlertDialog(thisContext).toast(regStatus)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { volleyError ->
                        dialog.dismissDialog()
                        ClassAlertDialog(thisContext).toast("ERROR IN NETWORK CONNECTION!")
                    }) {
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["request_type"] = "pat_register"
                    params["name"] = regName
                    params["phone"] = regPhone
                    params["gender"] = regGender
                    params["age"] = regAge
                    params["address"] = regAddr
                    params["password"] = regPassword
                    return params
                }
            }

            //adding request to queue
            VolleySingleton.instance?.addToRequestQueue(stringRequest)
            //volley interactions end
        }
    }

    private fun processLogin() {
        val logUsernamePhone = toString(log_username_phone)
        val logPassword = toString(log_password)

        if(checkEmpty(logUsernamePhone)||checkEmpty(logPassword)){
            ClassAlertDialog(thisContext).toast("Both fields are required...")
        }else{

            //creating volley string request
            val dialog= ClassProgressDialog(context)
            dialog.createDialog()
            val stringRequest = object : StringRequest(Request.Method.POST, UrlHolder.URL_LOGIN_REGISTER,
                    Response.Listener<String> { response ->
                        dialog.dismissDialog()

                        try {

                            val obj = JSONObject(response)
                            val regStatus = obj.getString("log_status");
                            if (regStatus == "ok") {
                                ClassAlertDialog(thisContext).toast("Login successful...")

                                val userDetails = obj.getJSONArray("userDetails").getJSONObject(0)
                                val allUserDetails = obj.getJSONArray("all_usersz_details")
                                saveUserDetails(userDetails)
                                saveAllUsersDetails(allUserDetails)
                            } else {
                                ClassAlertDialog(thisContext).toast(regStatus)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { volleyError ->
                        dialog.dismissDialog()
                        ClassAlertDialog(thisContext).toast("ERROR IN NETWORK CONNECTION!")
                    }) {
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["request_type"] = "login"
                    params["log_username"] = logUsernamePhone
                    params["log_password"] = logPassword
                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            //adding request to queue
            VolleySingleton.instance?.addToRequestQueue(stringRequest)
            //volley interactions end
        }
    }


    private fun initializeData(){
        genderSpinner()
    }


    private fun genderSpinner(){
        val spinnerArray = arrayOf("Gender", "Male", "Female")


        val spinnerArrayAdapter = ArrayAdapter<String>(thisContext, android.R.layout.simple_spinner_dropdown_item, spinnerArray)
        //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reg_gender_spinner?.adapter = spinnerArrayAdapter

        reg_gender_spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                regGender = spinnerArray[position]
            }

        }
    }
    private fun saveAllUsersDetails(allUserDetails: JSONArray?) {
        val preference = ClassSharedPreferences(thisContext)

        val jsonResponse = allUserDetails!!

        if ((jsonResponse.length()!=0)){
            val qDataArray = mutableListOf<UserClassBinder>()
            for (i in 0 until jsonResponse.length()) {
                val jsonObj = jsonResponse.getJSONObject(i)
                val eachData = UserClassBinder(
                        jsonObj?.getString("user_id"),
                        jsonObj?.getString("user_card_no"),
                        jsonObj?.getString("name"),
                        jsonObj?.getString("username"),
                        jsonObj?.getString("phone"),
                        jsonObj?.getString("gender"),
                        jsonObj?.getString("age"),
                        jsonObj?.getString("address"),
                        jsonObj?.getString("pat_med_report"),
                        jsonObj?.getString("speciality"),
                        jsonObj?.getString("speciality_id"),
                        jsonObj?.getString("user_level"),
                        jsonObj?.getString("reg_date"),
                        jsonObj?.getString("photo_url")
                )
                qDataArray.add(eachData)
            }
            preference.setAllUsersJSONDetails(Gson().toJson(mutableListOf(qDataArray)))
        }
    }

    private fun saveUserDetails(userDetails: JSONObject?) {
        val preference = ClassSharedPreferences(thisContext)

        val userDetailsArr = UserClassBinder(
                userDetails?.getString("user_id"),
                userDetails?.getString("user_card_no"),
                userDetails?.getString("name"),
                userDetails?.getString("username"),
                userDetails?.getString("phone"),
                userDetails?.getString("gender"),
                userDetails?.getString("age"),
                userDetails?.getString("address"),
                userDetails?.getString("pat_med_report"),
                userDetails?.getString("speciality"),
                userDetails?.getString("speciality_id"),
                userDetails?.getString("user_level"),
                userDetails?.getString("reg_date"),
                userDetails?.getString("photo_url")
        )
        preference.setUserJSONDetails(Gson().toJson(mutableListOf(userDetailsArr)))
        preference.setUserId(userDetailsArr.user_id)
        preference.setUserLevel(userDetailsArr.user_level)


        dialog!!.dismiss()
        listener?.onRegSuccessful()
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        } else {
            setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_NoActionBar)
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isCancelable = false
        return dialog
    }


    //Fragment communication with the Home Activity Starts
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentStRegisterInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement FragmentStRegisterInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface FragmentStRegisterInteractionListener {
        fun onRegSuccessful()
    }
    //Fragment communication with the Home Activity Stops
}

