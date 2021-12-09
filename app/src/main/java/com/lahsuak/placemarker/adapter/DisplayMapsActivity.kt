package com.lahsuak.placemarker.adapter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.lahsuak.placemarker.R
import com.lahsuak.placemarker.databinding.ActivityDisplayMapsBinding
import com.lahsuak.placemarker.models.UserMap

class DisplayMapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var userMap: UserMap
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDisplayMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDisplayMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        userMap = intent.getSerializableExtra("mapsData") as UserMap
        Log.d("MAP", "data ${userMap.places.last().latitude} and ${userMap.places.last().longitude}")
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val boundBuilder = LatLngBounds.Builder()
        for(place in userMap.places){
            val latLang = LatLng(place.latitude,place.longitude)
            boundBuilder.include(latLang)
            mMap.addMarker(MarkerOptions().position(latLang).title(place.title).snippet("${place.latitude} , ${place.longitude} "))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLang,10F))
        }
            //this is for zoomable region ex. 10000 x 10000 with 0 padding
        //mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(),10000,10000,0))
    }
}