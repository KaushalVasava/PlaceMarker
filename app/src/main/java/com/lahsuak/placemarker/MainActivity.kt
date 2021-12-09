package com.lahsuak.placemarker

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lahsuak.placemarker.adapter.DisplayMapsActivity
import com.lahsuak.placemarker.adapter.MapsAdapter
import com.lahsuak.placemarker.adapter.MapsListener
import com.lahsuak.placemarker.models.Place
import com.lahsuak.placemarker.models.UserMap
import java.io.*

private const val FILENAME = "PlaceMarker.data"
class MainActivity : AppCompatActivity() {
    //private lateinit var navController: NavController
    private var mapsList = mutableListOf<UserMap>()
    private lateinit var adapter: MapsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recyclerview= findViewById<RecyclerView>(R.id.maps_recyclerview)
        val addMarker = findViewById<FloatingActionButton>(R.id.add_marker)
        recyclerview.layoutManager = LinearLayoutManager(this)

        mapsList = getUserMaps(this).toMutableList()
        adapter = MapsAdapter(this,mapsList,object: MapsListener {
            override fun onItemClick(position: Int) {
                val intent= Intent(this@MainActivity,DisplayMapsActivity::class.java)
                intent.putExtra("mapsData",mapsList[position])
                startActivity(intent)
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right)
            }
        })
        recyclerview.adapter = adapter

        addMarker.setOnClickListener {
            showDialog()
        }

    }

    //Save user maps data into local file
    private fun getDataFile(context: Context): File {
        return File(context.filesDir,FILENAME)
    }
    //Serialize user maps data because we create data class serializable
    private fun saveUserMaps(context: Context,userMaps: List<UserMap>){
        ObjectOutputStream(FileOutputStream(getDataFile(context))).use {
            it.writeObject(userMaps)
        }
    }
    //Deserialize user maps data
    private fun getUserMaps(context: Context):List<UserMap>{
        val dataFile=getDataFile(context)
        if(!dataFile.exists()){
            return emptyList()
        }
        ObjectInputStream(FileInputStream(dataFile)).use {
           return it.readObject() as List<UserMap>
        }
    }

    private fun showDialog() {
        val mapLayout = LayoutInflater.from(this).inflate(R.layout.add_map_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Map Title")
            .setView(mapLayout)
            .setPositiveButton("OK") { dialog, _ ->
                val markerTitle = mapLayout.findViewById<EditText>(R.id.map_title)
                if (markerTitle.text.isNotEmpty()) {
                    val intent = Intent(this,CreateMapActivity::class.java)
                    intent.putExtra("Extra_map",markerTitle.text.toString())
                    startActivityForResult(intent,40)
                } else {
                    Toast.makeText(
                        this,
                        "Please enter title!",
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==40 && resultCode == RESULT_OK){
            val userMap = data?.getSerializableExtra("mapsData") as UserMap
            mapsList.add(userMap)
            Log.d("MAP", "data ${userMap.places.last().latitude} and ${userMap.places.last().longitude}")
            adapter.notifyItemInserted(mapsList.size-1)
            saveUserMaps(this,mapsList)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun generateSampleData(): List<UserMap> {
        return listOf(
            UserMap(
                "Memories from University",
                listOf(
                    Place("Branner Hall", "Best dorm at Stanford", 37.426, -122.163),
                    Place("Gates CS building", "Many long nights in this basement", 37.430, -122.173),
                    Place("Pinkberry", "First date with my wife", 37.444, -122.170)
                )
            ),
            UserMap("January vacation planning!",
                listOf(
                    Place("Tokyo", "Overnight layover", 35.67, 139.65),
                    Place("Ranchi", "Family visit + wedding!", 23.34, 85.31),
                    Place("Singapore", "Inspired by \"Crazy Rich Asians\"", 1.35, 103.82)
                )),
            UserMap("Singapore travel itinerary",
                listOf(
                    Place("Gardens by the Bay", "Amazing urban nature park", 1.282, 103.864),
                    Place("Jurong Bird Park", "Family-friendly park with many varieties of birds", 1.319, 103.706),
                    Place("Sentosa", "Island resort with panoramic views", 1.249, 103.830),
                    Place("Botanic Gardens", "One of the world's greatest tropical gardens", 1.3138, 103.8159)
                )
            ),
            UserMap("My favorite places in the Midwest",
                listOf(
                    Place("Chicago", "Urban center of the midwest, the \"Windy City\"", 41.878, -87.630),
                    Place("Rochester, Michigan", "The best of Detroit suburbia", 42.681, -83.134),
                    Place("Mackinaw City", "The entrance into the Upper Peninsula", 45.777, -84.727),
                    Place("Michigan State University", "Home to the Spartans", 42.701, -84.482),
                    Place("University of Michigan", "Home to the Wolverines", 42.278, -83.738)
                )
            ),
            UserMap("Restaurants to try",
                listOf(
                    Place("Champ's Diner", "Retro diner in Brooklyn", 40.709, -73.941),
                    Place("Althea", "Chicago upscale dining with an amazing view", 41.895, -87.625),
                    Place("Shizen", "Elegant sushi in San Francisco", 37.768, -122.422),
                    Place("Citizen Eatery", "Bright cafe in Austin with a pink rabbit", 30.322, -97.739),
                    Place("Kati Thai", "Authentic Portland Thai food, served with love", 45.505, -122.635)
                )
            )
        )
    }
}






//        //set navigation graph
//        val navHostFragment = supportFragmentManager
//           .findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
//
//        //set controller for navigation between fragments
//        navController = navHostFragment.navController
//        setupActionBarWithNavController(navController)//,appBarConfiguration)
//    }
//    override fun onSupportNavigateUp(): Boolean {
//        //Pass argument appBarConfiguration in navigateUp() method
//        // for hamburger icon respond to click events
//        //navConfiguration
//        return navController.navigateUp() || super.onSupportNavigateUp()
//    }