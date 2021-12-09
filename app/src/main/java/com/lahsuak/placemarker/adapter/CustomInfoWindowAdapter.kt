package com.lahsuak.placemarker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.collection.arrayMapOf
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.lahsuak.placemarker.R

class CustomInfoWindowAdapter(context: Context) : GoogleMap.InfoWindowAdapter {
    private lateinit var mWindow: View
    private var mContext: Context = context

    init {
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)
    }

    private fun readWindowText(marker: Marker, view: View) {
        val title = marker.title
        val snippet = marker.snippet

        val tvTitle: TextView = view.findViewById(R.id.title)
        val tvSnippet: TextView = view.findViewById(R.id.detail_text)

        if (!title.isNullOrEmpty()) {
            tvTitle.text = title
        }
        if (!snippet.isNullOrEmpty()) {
            tvSnippet.text = snippet
        }
    }

    override fun getInfoWindow(marker: Marker?): View {
        readWindowText(marker!!, mWindow)
        return mWindow
    }

    override fun getInfoContents(marker: Marker?): View {
        readWindowText(marker!!, mWindow)
        return mWindow
    }
}