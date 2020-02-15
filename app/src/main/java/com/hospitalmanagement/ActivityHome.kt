package com.hospitalmanagement

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.GravityCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)
        thisContext = this
        prefs = ClassSharedPreferences(thisContext)

        clickEvents()

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
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
            }
        }
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
                aboutUsDialog()
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
                            .setMessage("Login is required before you check our doctors")
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
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
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
    }


}
