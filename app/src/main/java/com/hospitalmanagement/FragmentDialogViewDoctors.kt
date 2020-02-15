package com.hospitalmanagement

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_dialog_view_doctors.*


class FragmentDialogViewDoctors: DialogFragment() {
    var listener: FragmentDialogMyAppsInteractionListener? = null
    lateinit var thisContext: Activity

    private var docList = mutableListOf<UserClassBinder>()
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var ADAPTER : DocsAdapter



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_view_doctors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisContext = activity!!

        closeDialogFrag.setOnClickListener {
            dialog!!.dismiss()
        }


        linearLayoutManager = LinearLayoutManager(thisContext)
        ADAPTER = DocsAdapter(docList,thisContext)
        doctors_recyclerview.layoutManager = linearLayoutManager
        doctors_recyclerview.itemAnimator = DefaultItemAnimator()
        doctors_recyclerview.adapter = ADAPTER


    }

    override fun onStart() {
        super.onStart()
        refreshList()
    }

    private fun refreshList(){
        docList.clear()
        ADAPTER.addItems(docList)
        ADAPTER.notifyDataSetChanged()
        loadDoctors()
    }

    private fun loadDoctors(){
        val dataArray = ClassUtilities().getDoctors(thisContext).distinct()

        ADAPTER.addItems(dataArray.toMutableList())
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


    //Fragment communication with the Home Activity Starts
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentDialogMyAppsInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface FragmentDialogMyAppsInteractionListener {
        fun onAddAppointment()
    }
    //Fragment communication with the Home Activity Stops
}



