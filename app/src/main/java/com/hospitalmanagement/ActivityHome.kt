package com.hospitalmanagement

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.core.view.GravityCompat
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.alert_dialog_login.view.*
import kotlinx.android.synthetic.main.app_bar_activity_home.*
import kotlinx.android.synthetic.main.content_activity_home.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class ActivityHome : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FragmentDialogViewApps.FragmentDialogMyAppsInteractionListener
        , FragmentDialogMyOrderHistory.FragmentDialogOrdersHistoryInteractionListener, DocsAdapter.DocsAdapterCallbackInterface
        , FragmentDialogLoginRegister.FragmentStRegisterInteractionListener, FragmentDialogSearchDrugs.FragmentDialogSearchDrugInteractionListener {

    lateinit var prefs: ClassSharedPreferences
    lateinit var thisContext: Activity
    lateinit var curUserDetail:UserClassBinder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)
        thisContext = this
        prefs = ClassSharedPreferences(thisContext)


        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        val header = navigationView.getHeaderView(0)
        val navUserName = header.findViewById(R.id.nav_user_name) as TextView
        val navUserEmail = header.findViewById(R.id.nav_user_email) as TextView
        val navImageView = header.findViewById(R.id.nav_image_view) as ImageView

        if (!prefs.isLoggedIn()){
            navUserName.text = getString(R.string.app_name)
            navUserEmail.text = getString(R.string.nav_header_subtitle)

        }else{
            val stDetails = ClassSharedPreferences(thisContext).getUserJSONDetails()
            try {
                curUserDetail = Gson().fromJson(stDetails, Array<UserClassBinder>::class.java).asList()[0]

                navUserName.text = "${curUserDetail.name?.toUpperCase()}"
                navUserEmail.text = curUserDetail.phone?.toUpperCase()
            } catch (e: Exception) {
            }
        }
        nav_view.setCheckedItem(R.id.nav_home)//For nav view indicator


        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        clickEvents()
    }

    private fun clickEvents() {
        orderDrugBtn.setOnClickListener {
            searchDrugDialogShow()
        }
        search_drug.setOnClickListener {
            searchDrugDialogShow()
        }
        order_drugs.setOnClickListener {
            startActivity(Intent(this, ActivityMyOrders::class.java))
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        }

        view_doctors.setOnClickListener {
            if (!prefs.isLoggedIn()){
                AlertDialog.Builder(this)
                        .setMessage("Login is required before you check our doctors")
                        .setPositiveButton("Login"
                        ) { _, _ ->
                            logRegDialogShow()

                        }.setNegativeButton("Cancel"
                        ) { _, _ ->}
                        .show()

            }else{
                viewDocsDialogShow()
            }
        }
        make_appointment.setOnClickListener {
            if (!prefs.isLoggedIn()){
                AlertDialog.Builder(this)
                        .setMessage("Login is required before you make appointment wih our professional Doctors")
                        .setPositiveButton("Login"
                        ) { _, _ ->
                            logRegDialogShow()

                        }.setNegativeButton("Cancel"
                        ) { _, _ ->}
                        .show()
            }else{
                addAppDialogShow()
            }
        }
        my_appointments.setOnClickListener {
            if (!prefs.isLoggedIn()) {
                AlertDialog.Builder(this)
                        .setMessage("Login to view your appointments")
                        .setPositiveButton("Login"
                        ) { _, _ ->
                            logRegDialogShow()

                        }.setNegativeButton("Cancel"
                        ) { _, _ -> }
                        .show()
            }else{
                myAppsDialogShow()
            }
        }
        order_history.setOnClickListener {
            if (!prefs.isLoggedIn()) {
                AlertDialog.Builder(this)
                        .setMessage("Login to view your Order History")
                        .setPositiveButton("Login"
                        ) { _, _ ->
                            logRegDialogShow()

                        }.setNegativeButton("Cancel"
                        ) { _, _ -> }
                        .show()
            }else{
                myOrderHistoryDialogShow()
            }
        }

        check_my_med_report.setOnClickListener {
            if (!prefs.isLoggedIn()) {
                AlertDialog.Builder(this)
                        .setMessage("Login to view your medical report")
                        .setPositiveButton("Login"
                        ) { _, _ ->
                            logRegDialogShow()

                        }.setNegativeButton("Cancel"
                        ) { _, _ -> }
                        .show()
            }else{
                ClassAlertDialog(thisContext).alertMessage("${curUserDetail.pat_med_report}","Symptoms")
            }
        }


        //click image 5 times to login to admin panel...
        hLogo.setOnTouchListener(object : View.OnTouchListener {
            var handler = Handler()

            var numberOfTaps = 0
            var lastTapTimeMs: Long = 0
            var touchDownMs: Long = 0

            override fun onTouch(v: View, event: MotionEvent): Boolean {

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> touchDownMs = System.currentTimeMillis()
                    MotionEvent.ACTION_UP -> {
                        handler.removeCallbacksAndMessages(null)

                        if (System.currentTimeMillis() - touchDownMs > ViewConfiguration.getTapTimeout()) {
                            //it was not a tap
                            numberOfTaps = 0
                            lastTapTimeMs = 0
                        }

                        if (numberOfTaps > 0 && System.currentTimeMillis() - lastTapTimeMs < ViewConfiguration.getDoubleTapTimeout()) {
                            numberOfTaps += 1
                        } else {
                            numberOfTaps = 1
                        }

                        lastTapTimeMs = System.currentTimeMillis()

                        if (numberOfTaps == 5) {
                            if (prefs.getUserLevel() == "4"&&prefs.isLoggedIn()){
                                startActivity(Intent(thisContext, ActivityDoctorMgt::class.java), Bundle())
                            }else{
                                ClassAlertDialog(thisContext).toast("Unauthorized request")
                            }
                        }
                        v.performClick()
                    }
                }

                return true
            }
        })
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_activity_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_menu_search_drug->{
                searchDrugDialogShow()
            }
            R.id.action_menu_order_drug->{
                startActivity(Intent(this, ActivityMyOrders::class.java))
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
            }
            R.id.action_menu_order_drug2->{
                startActivity(Intent(this, ActivityMyOrders::class.java))
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
            }
            R.id.action_profile -> {
                if(!prefs.isLoggedIn()){
                    ClassAlertDialog(thisContext).toast("You have to register/login to view your profile...")
                }else{
                    startActivity(Intent(this, ActivityMyProfile::class.java))
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
                }
            }
            R.id.action_logout->{
                if(!prefs.isLoggedIn()){
                    ClassAlertDialog(thisContext).toast("Not logged IN...")
                }else{
                    prefs.setUserJSONDetails("")
                    this.onRegSuccessful()//reload the activity
                    ClassAlertDialog(thisContext).toast("You have logged out successfully...")
                }
            }
            R.id.action_about_us->{
                ClassAlertDialog(thisContext).alertMessage(ClassSharedPreferences(thisContext).getAllUsersJSONDetails()!!)
//                aboutUsDialog()
            }
            R.id.menu_login_pat->{
                loginChooser("1")
            }
            R.id.menu_login_doc->{
                loginChooser("2")
            }
            R.id.menu_login_phm->{
                loginChooser("3")
            }
            R.id.menu_login_admin->{
                loginChooser("4")
            }
            R.id.action_update_details->{
                if(!prefs.isLoggedIn()){
                    ClassAlertDialog(thisContext).toast("You are not logged IN...")
                }else{
                    updateUserDetails()
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }

        return super.onOptionsItemSelected(item)
    }
    private fun loginChooser(access_level:String) {
        if(prefs.isLoggedIn()){
            ClassAlertDialog(thisContext).toast("Already Logged IN")
        }else{
            if (access_level=="1"){
                logRegDialogShow()
            }else{
                adminLoginDialog()
            }
        }
    }
    private fun toString(editText: EditText):String{
        return editText.text.toString().trim()
    }
    private fun checkEmpty(string: String):Boolean{
        return string.isEmpty()
    }
    var genLogAlertDialog: AlertDialog? = null
    private fun adminLoginDialog() {
        val inflater = LayoutInflater.from(thisContext).inflate(R.layout.alert_dialog_login, null)
        val builder = AlertDialog.Builder(thisContext)

        builder.setView(inflater)
        genLogAlertDialog = builder.create()
        genLogAlertDialog?.show()

        inflater.genLogBtn.setOnClickListener {

            val genLogUsername = toString(inflater.genLogUsername)
            val genLogPassword = toString(inflater.genLogPassword)

            if (checkEmpty(genLogUsername)) {
                ClassAlertDialog(thisContext).toast("Enter your username")
            } else if (checkEmpty(genLogPassword)) {
                ClassAlertDialog(thisContext).toast("Enter your password")
            } else {

                //creating volley string request
                val dialog = ClassProgressDialog(thisContext)
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

                                    genLogAlertDialog?.dismiss()
                                    onRegSuccessful()
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
                        params["log_username"] = genLogUsername
                        params["log_password"] = genLogPassword
                        return params
                    }
                }

                //adding request to queue
                VolleySingleton.instance?.addToRequestQueue(stringRequest)
                //volley interactions end
            }
        }
    }

    private fun updateUserDetails() {

        //creating volley string request
        val dialog = ClassProgressDialog(thisContext)
        dialog.createDialog()
        val stringRequest = object : StringRequest(Request.Method.POST, UrlHolder.URL_LOGIN_REGISTER,
                Response.Listener<String> { response ->
                    dialog.dismissDialog()

                    try {

                        val obj = JSONObject(response)
                        val regStatus = obj.getString("log_status");
                        if (regStatus == "ok") {
                            ClassAlertDialog(thisContext).toast("Details Updated...")

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
                params["request_type"] = "update_details"
                params["user_id"] = prefs.getUserId()!!
                return params
            }
        }

        //adding request to queue
        VolleySingleton.instance?.addToRequestQueue(stringRequest)
        //volley interactions end
    }



    private fun aboutUsDialog() {
        ClassAlertDialog(thisContext).alertMessage("ADSU Hospital and Management system where you can order for drugs, book appointment and view our doctors profile....." +
                "\n\nDeveloped by Android/Web enthusiast. We develop robust, dynamic & responsible applications ","About Us")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the home action
            }
            R.id.nav_my_profile -> {
                if(!prefs.isLoggedIn()){
                    AlertDialog.Builder(this)
                            .setMessage("Login is required before you view our doctors")
                            .setPositiveButton("Login"
                            ) { _, _ ->
                                logRegDialogShow()

                            }.setNegativeButton("Cancel"
                            ) { _, _ ->}
                            .show()
                }else{
                    startActivity(Intent(this, ActivityMyProfile::class.java))
                }
            }
            R.id.nav_search_drug -> {
                searchDrugDialogShow()
            }
            R.id.nav_share -> {
                ClassShareApp(thisContext).shareApp()
            }
            R.id.nav_about_us->{
//                ClassAlertDialog(thisContext).alertMessage(prefs.getAllUsersJSONDetails()!!)
                aboutUsDialog()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    //from my appointment fragment dialog class
    override fun onAddAppointment() {
        addAppDialogShow()
    }
    //from my order history dialog fragment
    override fun onAddOrders() {
        searchDrugDialogShow()
    }
    //from docs adapter class
    override fun onMakeApp() {
        addAppDialogShow()
    }
    //for successful login
    override fun onRegSuccessful() {
        finish()
        startActivity(intent)
    }
    //search drug frag dialog
    override fun onCloseSearchDrugDialog() {

        val curDrugOrders = (Gson().fromJson(prefs.getOrderDetails(), Array<DrugClassBinder>::class.java).asList())
        if (curDrugOrders.isNotEmpty()){
            startActivity(Intent(this, ActivityMyOrders::class.java))
//            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

    }





    //add appointment dialog
    private val dialogFragmentAddApp = FragmentDialogMakeApp()
    private fun addAppDialogShow(){
        if(dialogFragmentAddApp.isAdded)return

        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag(FragmentDialogMakeApp::class.java.name)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        dialogFragmentAddApp.show(ft, FragmentDialogMakeApp::class.java.name)
    }

    //student search drug or add orders to cart
    private val dialogFragmentSearchDrug = FragmentDialogSearchDrugs()
    private fun searchDrugDialogShow(){
        if(dialogFragmentSearchDrug.isAdded)return

        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag(FragmentDialogSearchDrugs::class.java.name)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        dialogFragmentSearchDrug.show(ft, FragmentDialogSearchDrugs::class.java.name)
    }


    //doctors frag dialog class
    private val dialogFragmentDoctors = FragmentDialogViewDoctors()
    private fun viewDocsDialogShow(){
        if(dialogFragmentDoctors.isAdded)return

        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag(FragmentDialogViewDoctors::class.java.name)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        dialogFragmentDoctors.show(ft, FragmentDialogViewDoctors::class.java.name)
    }

    //login/register frag dialog class
    private val dialogFragmentLogRegister = FragmentDialogLoginRegister()
    private fun logRegDialogShow(){
        if(dialogFragmentLogRegister.isAdded)return

        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag(FragmentDialogLoginRegister::class.java.name)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        dialogFragmentLogRegister.show(ft, FragmentDialogLoginRegister::class.java.name)
    }

    //my appointments frag dialog class
    private val dialogFragmentMyApps = FragmentDialogViewApps()
    private fun myAppsDialogShow(){
        if(dialogFragmentMyApps.isAdded)return

        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag(FragmentDialogViewApps::class.java.name)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        dialogFragmentMyApps.show(ft, FragmentDialogViewApps::class.java.name)
    }


    //my order history frag dialog class
    private val dialogFragmentMyOrderHistory = FragmentDialogMyOrderHistory()
    private fun myOrderHistoryDialogShow(){
        if(dialogFragmentMyOrderHistory.isAdded)return

        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag(FragmentDialogMyOrderHistory::class.java.name)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        dialogFragmentMyOrderHistory.show(ft, FragmentDialogMyOrderHistory::class.java.name)
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
            preference.setAllUsersJSONDetails(Gson().toJson(qDataArray))
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
        preference.setUserJSONDetails(Gson().toJson(userDetailsArr))
        preference.setUserId(userDetailsArr.user_id)
        preference.setUserLevel(userDetailsArr.user_level)
    }


}
