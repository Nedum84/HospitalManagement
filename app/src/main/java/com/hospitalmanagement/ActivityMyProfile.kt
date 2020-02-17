package com.hospitalmanagement

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.google.gson.Gson
import com.r0adkll.slidr.Slidr
import kotlinx.android.synthetic.main.activity_my_profile.*

class ActivityMyProfile : AppCompatActivity() {
    lateinit var thisContext: Activity
    lateinit var curUserDetail:UserClassBinder
    lateinit var prefs: ClassSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        Slidr.attach(this)//for slidr swipe lib

        thisContext = this
        prefs = ClassSharedPreferences(thisContext)



        val stDetails = ClassSharedPreferences(thisContext).getUserJSONDetails()
        try {
            curUserDetail = Gson().fromJson(stDetails, Array<UserClassBinder>::class.java).asList()[0]

            loadStData()
        } catch (e: Exception) {
            super.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadStData() {
        name.text = "${curUserDetail.name}"
        phone.text = curUserDetail.phone
        address.text = curUserDetail.address
        LoadImg(thisContext,profilePhoto).execute(curUserDetail.photo_url)


        cardNo.text = "#${curUserDetail.user_card_no}"
        username.text = curUserDetail.username
        gender.text = curUserDetail.gender
        age.text = "${curUserDetail.age} years"
        regDate.text = ClassDateAndTime().getDateTime2(curUserDetail.reg_date!!.toLong())

        if (curUserDetail.user_level!="1"){
            cardNoWrapper.visibility = View.GONE
            genderWrapper.visibility = View.GONE

            address.text = curUserDetail.speciality
            ageTitle.text = "Years of Experience: "
        }else{

            ageTitle.text = "Age: "
        }
    }


    private fun backPressed(){
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                backPressed()
            }
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }


}

