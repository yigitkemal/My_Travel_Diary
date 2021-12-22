package com.example.mytraveldiary.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MainThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.mytraveldiary.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mytraveldiary.databinding.ActivityMapsBinding
import com.example.mytraveldiary.model.Place
import com.example.mytraveldiary.room.PlaceDao
import com.example.mytraveldiary.room.PlaceDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean: Boolean? = null
    private var selectedLatitute: Double? = null
    private var selectedLongitute: Double? = null
    private lateinit var db: PlaceDatabase
    private lateinit var placeDao: PlaceDao

    val compositeDisposable = CompositeDisposable()

    var placeFromMain: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()

        sharedPreferences = this.getSharedPreferences("com.example.mytraveldiary", MODE_PRIVATE)
        trackBoolean = false
        selectedLatitute = 0.0
        selectedLongitute = 0.0

        db = Room.databaseBuilder(applicationContext, PlaceDatabase::class.java, "Places").build()

        binding.mapsSaveButton.isEnabled = false

        placeDao = db.placeDao()
    }


    //altınoluk
    //39.5678031, 26.802981
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        val intent = intent
        val info = intent.getStringExtra("info")

        if(info == "new"){
            binding.mapsSaveButton.visibility = View.VISIBLE
            binding.mapsDeleteButton.visibility= View.GONE

            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

            locationListener = object : LocationListener {
                override fun onLocationChanged(p0: Location) {
                    trackBoolean = sharedPreferences.getBoolean("trackBoolean", false)
                    if (trackBoolean == false) {
                        val userLocation = LatLng(p0.latitude, p0.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                        sharedPreferences.edit().putBoolean("trackBoolean", true).apply()
                    }
                }
            }

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // permission denied
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    Snackbar.make(
                        binding.root,
                        "Permission needed for location",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Give Permission") {
                        // request permission
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()
                } else {
                    // request permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                // permission granted
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    100f,
                    locationListener
                )
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
                }
                mMap.isMyLocationEnabled = true
            }

        }else{
            mMap.clear()
            placeFromMain = intent.getSerializableExtra("selectedPlace") as? Place

            placeFromMain?.let {
                val latlng = LatLng(it.latitute,it.longitute)
                mMap.addMarker(MarkerOptions().position(latlng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15f))

                binding.mapsTextview.setText(it.name)
                binding.mapsSaveButton.visibility = View.GONE
                binding.mapsDeleteButton.visibility= View.VISIBLE
            }

        }



        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,100,locationListener)


    }

    private fun registerLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // permission granted
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            10000,
                            100f,
                            locationListener
                        )
                        val lastLocation =
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (lastLocation != null) {
                            val lastUserLocation =
                                LatLng(lastLocation.latitude, lastLocation.longitude)
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    lastUserLocation,
                                    15f
                                )
                            )
                        }
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission needed!", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onMapLongClick(p0: LatLng?) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0!!))

        selectedLatitute = p0.latitude
        selectedLongitute = p0.longitude
        binding.mapsSaveButton.isEnabled = true
    }

    fun save(view: View) {


        if (selectedLatitute != null && selectedLongitute != null) {
            val place = Place(
                binding.mapsTextview.text.toString(),
                selectedLatitute!!.toDouble(),
                selectedLongitute!!.toDouble()
            )
            compositeDisposable.add(
                placeDao.insert(place)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )

        }

    }

    fun delete(view: View) {

        placeFromMain?.let {
            compositeDisposable.add(
                placeDao.delete(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }


    }

    private fun handleResponse() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }


}

/*

val sydney = LatLng(39.5678031, 26.802981)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Altınoluk"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f))
 */