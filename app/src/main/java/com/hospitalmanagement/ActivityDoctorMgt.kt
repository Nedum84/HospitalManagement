package com.hospitalmanagement

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_doctor_mgt.*
import kotlinx.android.synthetic.main.alert_dialog_add_doc.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap


class ActivityDoctorMgt: AppCompatActivity(), DocsAdapter2.DocsAdapterCallbackInterface2 {

    lateinit var thisContext: Activity

    private var docList = mutableListOf<UserClassBinder>()
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var ADAPTER: DocsAdapter2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_mgt)
        thisContext = this



        linearLayoutManager = LinearLayoutManager(thisContext)
        ADAPTER = DocsAdapter2(docList, thisContext)
        doctors_recyclerview.layoutManager = linearLayoutManager
        doctors_recyclerview.itemAnimator = DefaultItemAnimator()
        doctors_recyclerview.adapter = ADAPTER
    }


    override fun onStart() {
        super.onStart()
        refreshList()
    }

    private fun refreshList() {
        docList.clear()
        ADAPTER.addItems(docList)
        ADAPTER.notifyDataSetChanged()
        loadDoctors()
    }

    private fun loadDoctors() {
        val dataArray = ClassUtilities().getDoctors(thisContext).distinct()

        ADAPTER.addItems(dataArray.toMutableList())
    }


    override fun onRemoveDoc(user_details: UserClassBinder) {

        //creating volley string request
        val dialog = ClassProgressDialog(thisContext)
        dialog.createDialog()
        val stringRequest = object : StringRequest(Request.Method.POST, UrlHolder.URL_REMOVE_DOCTOR,
                Response.Listener<String> { response ->
                    dialog.dismissDialog()

                    try {

                        val obj = JSONObject(response)
                        val resStatus = obj.getString("message");
                        if (resStatus == "ok") {
                            ClassAlertDialog(thisContext).toast("Registration successful...")


                            val allUserDetails = obj.getJSONArray("all_usersz_details")
                            saveAllUsersDetails(allUserDetails)
                        } else {
                            ClassAlertDialog(thisContext).toast(resStatus)
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
                params["request_type"] = "remove_doc"
                params["doc_id"] = ClassSharedPreferences(thisContext).getUserId()!!
                return params
            }
        }

        //adding request to queue
        VolleySingleton.instance?.addToRequestQueue(stringRequest)
        //volley interactions end
    }


    private fun saveAllUsersDetails(allUserDetails: JSONArray?) {
        val preference = ClassSharedPreferences(thisContext)

        val jsonResponse = allUserDetails!!

        if ((jsonResponse.length() != 0)) {
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

        refreshList()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_activity_doc_mgt, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_menu_add_doc -> {
                addDocDialog()
            }
            R.id.action_menu_go_back -> {
                finish()
            }
            else -> return super.onOptionsItemSelected(item)
        }

        return super.onOptionsItemSelected(item)
    }


    var specialityId = ""
    var addDocAlertDialog: AlertDialog? = null
    private fun addDocDialog() {
        val inflater = LayoutInflater.from(thisContext).inflate(R.layout.alert_dialog_add_doc, null)
        val builder = AlertDialog.Builder(thisContext)

        builder.setView(inflater)
        addDocAlertDialog = builder.create()
        addDocAlertDialog?.show()

        docSpecialitySpinnerInitialize(inflater.regDocSpecialitySpinner)
        inflater.regDocBtn.setOnClickListener {

            val regDocName = toString(inflater.regDocName)
            val regDocPhone = toString(inflater.regDocPhone)
            val regDocPhotoUrl = toString(inflater.regDocPhotoUrl)
            val regDocYearsOfExp = toString(inflater.regDocYearsOfExp)
            val regDocPassword = toString(inflater.regDocPassword)

            if (checkEmpty(regDocName)) {
                ClassAlertDialog(thisContext).toast("name is required")
            } else if (checkEmpty(regDocPhone)) {
                ClassAlertDialog(thisContext).toast("Enter  phone number")
            } else if (checkEmpty(regDocYearsOfExp)) {
                ClassAlertDialog(thisContext).toast("Enter doctor's years of experience")
            } else if (specialityId == "") {
                ClassAlertDialog(thisContext).toast("Select your gender...")
            } else if (regDocPassword.length < 4) {
                ClassAlertDialog(thisContext).toast("Password too short...")
            } else {

                //creating volley string request
                val dialog = ClassProgressDialog(thisContext)
                dialog.createDialog()
                val stringRequest = object : StringRequest(Request.Method.POST, UrlHolder.URL_MAKE_DOC_PHM,
                        Response.Listener<String> { response ->
                            dialog.dismissDialog()

                            try {

                                val obj = JSONObject(response)
                                val regStatus = obj.getString("reg_status");
                                if (regStatus == "ok") {
                                    ClassAlertDialog(thisContext).toast("Registration successful...")

                                    val allUserDetails = obj.getJSONArray("all_usersz_details")
                                    saveAllUsersDetails(allUserDetails)
                                    addDocAlertDialog?.dismiss()
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
                        params["request_type"] = "reg_doc"
                        params["name"] = regDocName
                        params["phone"] = regDocPhone
                        params["speciality_id"] = specialityId
                        params["years_of_experience"] = regDocYearsOfExp
                        params["photo_url"] = regDocPhotoUrl
                        params["password"] = regDocPassword
                        return params
                    }
                }

                //adding request to queue
                VolleySingleton.instance?.addToRequestQueue(stringRequest)
                //volley interactions end
            }
        }
    }


    private fun toString(editText: EditText): String {
        return editText.text.toString().trim()
    }

    private fun checkEmpty(string: String): Boolean {
        return string.isEmpty()
    }

    private fun docSpecialitySpinnerInitialize(regDocSpecialitySpinner:Spinner) {
        val docList = ClassUtilities().getDoctors(thisContext).distinctBy { it.speciality_id }

        val docSpecialityNameArray = arrayListOf<String>()
        val docSpecialityIdArray = arrayListOf<String>()

        docSpecialityNameArray.add("Select Speciality...")
        docSpecialityIdArray.add("")
        for (element in docList) {
            if(element.speciality!!.isEmpty())continue

            docSpecialityNameArray.add(element.speciality)
            docSpecialityIdArray.add(element.speciality_id!!)
        }
        val spinnerArrayAdapter = ArrayAdapter<String>(thisContext, android.R.layout.simple_spinner_dropdown_item, docSpecialityNameArray)
        //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        regDocSpecialitySpinner?.adapter = spinnerArrayAdapter

        regDocSpecialitySpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                specialityId = docSpecialityIdArray[position]
            }

        }


    }
}



