package com.example.google_maps

import android.os.Build
import androidx.annotation.RequiresApi


import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {



    private lateinit var mMap: GoogleMap
   val locationManager by lazy {
       getSystemService(Context.LOCATION_SERVICE) as LocationManager
   }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        requestAccessFineLocation()
        super.onStart()
        when{
            isFineLocationGranted()->{
                when{
                    isLocationEnabled()-> setupLocationListener()
                    else -> showGPSNotEnabledDialog()
                }
            }
            else->requestAccessFineLocation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isFineLocationGranted(): Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode)
        {
            999->{
                if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {

                    when
                    {
                        isLocationEnabled()-> setupLocationListener()
                        else -> showGPSNotEnabledDialog()
                    }

                }
                else
                    Toast.makeText(this,"Request Cancelled",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupLocationListener() {
        val providers = locationManager.getProviders(true)
        var l: Location? = null
        for (i in providers.indices.reversed()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            l = locationManager.getLastKnownLocation(providers[i])
            if (l != null) break
        }
        l?.let {
            if (::mMap.isInitialized) {
                val current = LatLng(it.latitude, it.longitude)
                mMap.addMarker(MarkerOptions().position(current).title("Marker in  Current Area"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(current))
            }
        }

//            var lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            val provider = lm.getProviders(true)
//            var l: Location? = null
//            for (i in provider.indices.reversed()) {
//                if (ActivityCompat.checkSelfPermission(
//                                this,
//                                Manifest.permission.ACCESS_FINE_LOCATION
//                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                                this,
//                                Manifest.permission.ACCESS_COARSE_LOCATION
//                        ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return
//                }
//                l = lm.getLastKnownLocation(provider[i])
//                if (l != null)
//                    break
//            }
//            l?.let {
//                if (::mMap.isInitialized)
//                {
//                    val current = LatLng(it.latitude, it.longitude)
//                    mMap.addMarker(MarkerOptions().position(current).title("Marker in  Current Area"))
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(current))
//                }
//                   }
//        }
    }

    fun isLocationEnabled():Boolean
    {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
    fun showGPSNotEnabledDialog()
    {
        //Toast.makeText(this,"aaya",Toast.LENGTH_LONG).show()
        val di=AlertDialog.Builder(this)
                .setTitle("Enable GPS")
                .setMessage("GPS is required for Maps")
                .setCancelable(false)
                .setPositiveButton("Enable Now"){ dialogInterface: DialogInterface, i: Int ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    dialogInterface.dismiss()
                }
        di.show()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestAccessFineLocation() {
        this.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),999)
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
        mMap.uiSettings.apply {
            isMyLocationButtonEnabled=true
            isCompassEnabled=true
            isCompassEnabled=true
            isMapToolbarEnabled=true
            isRotateGesturesEnabled=true
            isScrollGesturesEnabledDuringRotateOrZoom=true
            isZoomControlsEnabled=true
            isZoomGesturesEnabled=true

        }
        mMap.setMaxZoomPreference(14f)
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}
