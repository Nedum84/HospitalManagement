package com.hospitalmanagement

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import android.app.Activity
import java.lang.ref.WeakReference
import android.app.Application
import android.os.Build
import androidx.fragment.app.FragmentActivity



class LoadImg (context: Context, imageView: ImageView): AsyncTask<String, Void, String>() {
    private val activityReference: WeakReference<Context> = WeakReference(context)
    private val imageViewReference: WeakReference<ImageView> = WeakReference(imageView)
    var imgUrl:String = ""

    override fun doInBackground(vararg params: String?): String? {
        imgUrl = params[0]!!

        return imgUrl
    }


    override fun onPostExecute(filePath: String?) {
        val thisContext = activityReference.get() ?: return//returns if thisContext is null
        val thisImg = imageViewReference.get() ?: return//returns if thisContext is null

        if(thisContext.isAvailable()){
            Glide.with(thisContext.applicationContext)//instead of thisContext
                    .load(imgUrl)
                    .apply(RequestOptions()
//                            .placeholder(R.drawable.ic_tune_black_24dp)//default image on loading
                            .error(R.drawable.user)//without n/w, this img shows
                            .dontAnimate()
                            .fitCenter()
                    )
                    .thumbnail(.1f)
                    .into(thisImg)
        }
    }

    private fun Context?.isAvailable(): Boolean {//to avoid error
        if (this == null) {
            return false
        } else if (this !is Application) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (this is FragmentActivity) {
                    return !this.isDestroyed
                } else if (this is Activity) {
                    return !this.isDestroyed
                }
            }
        }
        return true
    }
}