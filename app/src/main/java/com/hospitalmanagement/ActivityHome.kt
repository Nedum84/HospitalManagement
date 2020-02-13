package com.hospitalmanagement

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.GravityCompat
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_activity_home.*

class ActivityHome : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FragmentDialogViewApps.FragmentDialogMyAppsInteractionListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        refreshPage.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
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
        menuInflater.inflate(R.menu.activity_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onAddAppointment() {
        addAppDialogShow()
    }

    //student clearance
    private val dialogFragmentAddApp = FragmentDialogMakeApp()
    private fun addAppDialogShow(){
        if(dialogFragmentAddApp.isAdded)return

        val ft = supportFragmentManager.beginTransaction()
//        val prev = supportFragmentManager.findFragmentByTag("dialog")
        val prev = supportFragmentManager.findFragmentByTag(FragmentDialogMakeApp::class.java.name)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        dialogFragmentAddApp.show(ft, FragmentDialogMakeApp::class.java.name)
    }



//    private fun flagQuestionDialog(){
//        val inflater = LayoutInflater.from(this).inflate(R.layout.alert_dialog_inflate_flag_answer, null)
//        val builder = AlertDialog.Builder(this)
//        builder.setView(inflater)
//        flagQuestionDialog = builder.create()
//        flagQuestionDialog.show()
//
//
//        //close dialog
//        inflater.close_dialog.setOnClickListener {_->
//            flagQuestionDialog.hide()
//        }
//        //send dialog
//        inflater.add_flag_comment.setOnClickListener {_->
//            val comment_content = inflater.flag_comment_content.text.toString().trim()
//            if(comment_content == ""){
//                ClassAlertDialog(this).toast("Enter your comment")
//            }else{
//                addFlagQuestion(comment_content)
//            }
//        }
//    }
}
