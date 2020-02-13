package com.hospitalmanagement


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_make_app.*


class FragmentDialogMakeApp : DialogFragment() {
    var listener: FragmentDialogMyAppsInteractionListener? = null
    lateinit var thisContext: Activity

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
    }


    fun topicSpinnerInitialize(subject_id:Int){
        val topicList = sqLiteDBHelper.getTopics(-1,subject_id)
        val topicNameArray = arrayListOf<String>()
        val topicIdArray = arrayListOf<String>()

        topicNameArray.add("Select Topic...")
        topicIdArray.add("-1")
        topicNameArray.add("All")
        topicIdArray.add("-1")
        for (element in topicList) {
            topicNameArray.add(element.topic_name!!)
            topicIdArray.add(element.topic_id!!)
        }
        val topicSpinnerArrayAdapter = ArrayAdapter<String>(thisContext, android.R.layout.simple_spinner_dropdown_item, topicNameArray)
        //selected item will look like a spinner set from XML
        topicSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        addAppDocSpeciality?.adapter = topicSpinnerArrayAdapter

        addAppDocSpeciality?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTopicId = topicIdArray[position]
                selectedTopicName = topicNameArray[position]
//                showError(selectedTopicId+" - "+topicNameArray[position])
            }

        }


        if(draftDetails.size !=0){
            val topicIdPos = topicIdArray.indexOf(draftDetails[0].draft_topic_id)
            topic_spin?.setSelection(topicIdPos)
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
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        } else {
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_NoActionBar)
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
