package com.hospitalmanagement


import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.fragment_dialog_make_app.*
import org.json.JSONException
import org.json.JSONObject
import android.widget.TimePicker
import android.widget.DatePicker
import java.util.*


class FragmentDialogMakeApp : DialogFragment() {
    lateinit var thisContext: Activity

    var specialityId = ""
    var doctorId = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_make_app, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisContext = activity!!

        closeDialogFrag.setOnClickListener {
            dialog!!.dismiss()
        }
        addAppointmentBtn.setOnClickListener {
            processAppointment()
        }
        addAppDate.setOnClickListener {
            dateTimeDialog()
        }

        docSpecialitySpinnerInitialize()
    }

    var appDateTime:Long = 0
    private fun dateTimeDialog() {
        val dialogView = View.inflate(activity, R.layout.date_time_picker, null)
        val alertDialog = AlertDialog.Builder(activity).create()

        dialogView.findViewById<View>(R.id.date_time_set).setOnClickListener {
            val datePicker = dialogView.findViewById<View>(R.id.date_picker) as DatePicker
            val timePicker = dialogView.findViewById<View>(R.id.time_picker) as TimePicker

            val calendar = GregorianCalendar(datePicker.year,
                    datePicker.month,
                    datePicker.dayOfMonth,
                    timePicker.currentHour,
                    timePicker.currentMinute)

            appDateTime = calendar.timeInMillis
            addAppDate.text = ClassDateAndTime().getDateTime(appDateTime)
            alertDialog.dismiss()
        }
        alertDialog.setView(dialogView)
        alertDialog.show()
    }


    private fun toString(editText: EditText):String{
        return editText.text.toString().trim()
    }
    private fun checkEmpty(string: String):Boolean{
        return string.isEmpty()
    }
    private fun processAppointment() {
//        val addAppDate = addAppDate.text.toString().trim()
        val addAppSymptoms = toString(addAppSymptoms)



        if(checkEmpty(specialityId)){
            ClassAlertDialog(thisContext).toast("Select speciality...")
        }else if(checkEmpty(doctorId)){
            ClassAlertDialog(thisContext).toast("Select doctor...")
        }else if(appDateTime.toInt()==0){
            ClassAlertDialog(thisContext).toast("Enter the date...")
        }else if(checkEmpty(addAppSymptoms)){
            ClassAlertDialog(thisContext).toast("Enter the symptoms...")
        }else{

            //creating volley string request
            val pDialog= ClassProgressDialog(context)
            pDialog.createDialog()
            val stringRequest = object : StringRequest(Request.Method.POST, UrlHolder.URL_ADD_APP,
                    Response.Listener<String> { response ->
                        pDialog.dismissDialog()

                        try {

                            val obj = JSONObject(response)
                            val regStatus = obj.getString("app_status")
                            if (regStatus == "ok") {
                                ClassAlertDialog(thisContext).toast("Appointment submitted successfully...")

                                dialog!!.dismiss()
                            } else {
                                ClassAlertDialog(thisContext).toast(regStatus)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { volleyError ->
                        pDialog.dismissDialog()
                        ClassAlertDialog(thisContext).toast("ERROR IN NETWORK CONNECTION!")
                    }) {
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["request_type"] = "add_app"
                    params["app_date"] = "$appDateTime"
                    params["pat_id"] = ClassSharedPreferences(thisContext).getUserId()!!
                    params["pat_symptoms"] = addAppSymptoms
                    params["doc_id"] = doctorId
                    return params
                }
            }

            //adding request to queue
            VolleySingleton.instance?.addToRequestQueue(stringRequest)
            //volley interactions end
        }
    }



    private fun docSpecialitySpinnerInitialize() {
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
        addAppDocSpeciality?.adapter = spinnerArrayAdapter

        addAppDocSpeciality?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                specialityId = docSpecialityIdArray[position]
                doctorsSpinnerInitialize()
            }

        }


    }
    fun doctorsSpinnerInitialize() {
        val docList = ClassUtilities().getDoctors(thisContext, specialityId)

        val docNameArray = arrayListOf<String>()
        val docIdArray = arrayListOf<String>()

        docNameArray.add("Select doctor...")
        docIdArray.add("")
        for (element in docList) {
            if(element.speciality!!.isEmpty())continue

            docNameArray.add(element.name!!)
            docIdArray.add(element.user_id!!)
        }
        val spinnerArrayAdapter = ArrayAdapter<String>(thisContext, android.R.layout.simple_spinner_dropdown_item, docNameArray)
        //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        addAppDocName?.adapter = spinnerArrayAdapter

        addAppDocName?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                doctorId = docIdArray[position]
            }

        }


    }



    override fun onResume() {
        super.onResume()
        //lock screen
        ClassUtilities().lockScreen(thisContext)//Lock Screen rotation...
    }

    override fun onPause() {
        super.onPause()
        ClassUtilities().unlockScreen(thisContext)//un Lock Screen rotation...
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
        dialog.window!!.attributes.windowAnimations = R.style.Animation_WindowSlideUpDown
//        isCancelable = false
        return dialog
    }
    //Fragment communication with the Home Activity Stops
}
