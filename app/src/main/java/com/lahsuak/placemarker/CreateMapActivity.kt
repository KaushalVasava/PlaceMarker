package com.lahsuak.placemarker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import com.lahsuak.placemarker.adapter.CustomInfoWindowAdapter
import com.lahsuak.placemarker.databinding.ActivityCreateMapBinding
import com.lahsuak.placemarker.models.Place
import com.lahsuak.placemarker.models.UserMap
import java.io.IOException
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList

class CreateMapActivity : AppCompatActivity(), OnMapReadyCallback, SearchView.OnQueryTextListener {
    private val TAG = "CreateMapActivity"
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCreateMapBinding
    private var markerList: MutableList<Marker> = mutableListOf()
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mLocationPermissionsGranted = false

    private val FINE_LOCATION: String = Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION: String = Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // val btnSave = findViewById<ImageView>(R.id.btn_save)

        //supportActionBar?.hide()
        supportActionBar?.title = intent.getStringExtra("Extra_map")
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getLocationPermission()
        mapFragment.view?.let {
            Snackbar.make(it, "Long press to add a marker", LENGTH_INDEFINITE)
                .setAction("OK") {}
                .show()
        }
        
        val mInfo = findViewById<ImageView>(R.id.info_window)
        mInfo.setOnClickListener {
            Log.d(TAG, "onCreate: info")
            try {
                if(markerList[0].isInfoWindowShown){
                    markerList[0].hideInfoWindow()
                }
                else{
                    markerList[0].showInfoWindow()
                }
            }catch (e: NullPointerException){
                Log.d(TAG, "onCreate: NullPointer EXception ${e.message}")
            }
        }
    }

//    private fun init() {
//        val search = findViewById<EditText>(R.id.search_map)
//        search.setOnEditorActionListener { v, actionId, event ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH
//                || actionId == EditorInfo.IME_ACTION_DONE
//                || event.action == KeyEvent.ACTION_DOWN
//                || event.action == KeyEvent.KEYCODE_ENTER
//            ) {
//                //execute our method for searching
//                getLocate(search)
//            }
//            return@setOnEditorActionListener false
//        }
//    }

//    private fun getLocate(search: EditText) {
//        Log.d(TAG, "getLocate: locating")
//        val searchString = search.text.toString()
//        val geocoder = Geocoder(this)
//        var list = ArrayList<Address>()
//        try {
//            list = geocoder.getFromLocationName(searchString, 1) as ArrayList<Address>
//        } catch (e: IOException) {
//            Log.d(TAG, "getLocate: IOException ${e.message}")
//        }
//        if (list.size > 0) {
//            val address = list[0]
//            Log.d(TAG, "getLocate: found location $address")
//        }
//    }

    private fun initMap() {
        Log.d(TAG, "initMap: ")
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnInfoWindowClickListener { deletedMarker ->
            markerList.remove(deletedMarker)
            deletedMarker.remove()
        }
        mMap.setOnMapLongClickListener { latLng ->
            showDialog(latLng)
        }
        if (mLocationPermissionsGranted) {
            getDeviceLocation()
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mMap.isMyLocationEnabled = true
            //mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this))
        }
    }

    private fun save() {
        if (markerList.isEmpty()) {
            Toast.makeText(
                this,
                "There must be at least one marker on the map",
                Toast.LENGTH_SHORT
            ).show()
        }
        val places = markerList.map { marker ->
            Place(
                marker.title,
                marker.snippet,
                marker.position.latitude,
                marker.position.longitude
            )
        }
        val userMap = UserMap(intent.getStringExtra("Extra_map")!!, places)
        val data = Intent()
        data.putExtra("mapsData", userMap)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the device current location")
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            if (mLocationPermissionsGranted) {
                val location = mFusedLocationProviderClient.lastLocation
                location.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "getDeviceLocation: found location")
                        try {
                            val currentLocation = task.result as Location

                            Log.d(TAG, "getDeviceLocation: $currentLocation")

                            val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
                        } catch (e: Exception) {
                            Toast.makeText(
                                applicationContext,
                                "Please turn on your GPS",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.d(TAG, "getDeviceLocation: current location is null")
                        Toast.makeText(this, "Not found your current location", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.d(TAG, "getDeviceLocation: Security Exception :${e.message}")
        }
    }

    private fun getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions")
        val permissions = arrayOf<String>(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    this.applicationContext,
                    COURSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "getLocationPermission: granted")
                //  initMap()
                mLocationPermissionsGranted = true
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            Log.d(TAG, "getLocationPermission: not granted")
            ActivityCompat.requestPermissions(
                this,
                permissions,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: called.")
        mLocationPermissionsGranted = false
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.size > 0) {
                    var i = 0
                    while (i < grantResults.size) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false
                            Log.d(TAG, "onRequestPermissionsResult: permission failed")
                            return
                        }
                        i++
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted")
                    mLocationPermissionsGranted = true
                    //initialize our map
                    initMap()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save_menu, menu)
        val searchItem = menu?.findItem(R.id.search_action)

        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.queryHint = "Search Street, City, State, Country"
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save_action) {
            if (markerList.isEmpty()) {
                Toast.makeText(
                    this,
                    "There must be at least one marker on the map",
                    Toast.LENGTH_SHORT
                ).show()
                return true
            }
            val places = markerList.map { marker ->
                Place(
                    marker.title,
                    marker.snippet,
                    marker.position.latitude,
                    marker.position.longitude
                )
            }
            val userMap = UserMap(intent.getStringExtra("Extra_map")!!, places)
            val data = Intent()
            data.putExtra("mapsData", userMap)
            setResult(Activity.RESULT_OK, data)
            finish()
            return true
        } else if (item.itemId == R.id.search_action) {

            return true
        } else
            return super.onOptionsItemSelected(item)
    }

    private fun showDialog(latLng: LatLng) {
        val markerLayout = LayoutInflater.from(this).inflate(R.layout.marker_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Create a marker")
            .setView(markerLayout)
            .setPositiveButton("OK") { dialog, _ ->
                val markerTitle = markerLayout.findViewById<EditText>(R.id.txt_title)
                val markerDesc = markerLayout.findViewById<EditText>(R.id.txt_desc)
                if (markerTitle.text.isNotEmpty() && markerDesc.text.isNotEmpty()) {
                    mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this))
                    val marker = mMap.addMarker(
                        MarkerOptions().position(latLng).title(markerTitle.text.toString()).snippet(
                            markerDesc.text.toString()
                        )
                    )
                    val snippet = "Address: "+mMap.cameraPosition
                    markerList.add(marker)
                } else {
                    Toast.makeText(
                        this,
                        "Please enter both title and description!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    //draw a circle on a map
    fun drawCircle(bharuch: LatLng) {
        val circleOption = CircleOptions()
            .center(bharuch)
            .radius(10000.0)
        val circle = mMap.addCircle(circleOption)
        circle.strokeColor = Color.RED
    }

    //draw a polygon on a map
    fun drawPolygon() {
        val ml1 = LatLng(22.9, 76.9)
        val ml2 = LatLng(24.9, 78.9)
        val ml3 = LatLng(25.9, 50.9)
        val ml4 = LatLng(28.9, 90.9)
        val polygonOption = PolygonOptions()
            .add(ml1)
            .add(ml2)
            .add(ml3)
            .add(ml4)
            .fillColor(R.color.cardview_light_background)
            .strokeColor(Color.BLUE)
        val polygon = mMap.addPolygon(polygonOption)
    }

    //draw a poly lines on a map
    fun drawPolyline() {
        val ml1 = LatLng(22.9, 76.9)
        val ml2 = LatLng(24.9, 78.9)
        val ml3 = LatLng(25.9, 50.9)
        val ml4 = LatLng(28.9, 90.9)
        val strokePattern = listOf<PatternItem>(
            Dot(), Gap(10F), Dash(50F)
        )

        val polylineOption = PolylineOptions()
            .add(ml1)
            .add(ml2)
            .add(ml3)
            .add(ml4)
            //.width(2.0F)
            .color(Color.MAGENTA)
        val polyline = mMap.addPolyline(polylineOption)
        polyline.pattern = strokePattern
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val searchString = newText!!.lowercase(Locale.getDefault())
        Log.d(TAG, "getLocate: locating")
        val geocoder = Geocoder(this)
        var list = ArrayList<Address>()
        try {
            list = geocoder.getFromLocationName(searchString, 1) as ArrayList<Address>
        } catch (e: IOException) {
            Log.d(TAG, "getLocate: IOException ${e.message}")
        }
        if (list.size > 0) {
            val address = list[0]
            Log.d(TAG, "getLocate: found location $address")
            val location = LatLng(address.latitude,address.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,10f))
            mMap.addMarker(MarkerOptions().position(location).title(address.getAddressLine(0)))
        }
        return true
    }
}