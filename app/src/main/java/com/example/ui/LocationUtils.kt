package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

@SuppressLint("MissingPermission")
fun getCurrentLocationString(context: Context): String {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            
        if (location != null) {
            return "${String.format("%.4f", location.latitude)}° N, ${String.format("%.4f", location.longitude)}° W"
        }
    }
    return "UNKNOWN (OFFLINE)"
}
