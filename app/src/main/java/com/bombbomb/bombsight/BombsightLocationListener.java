package com.bombbomb.bombsight;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;

/**
 * Created by cos-mbp-don on 3/21/17.
 */

public class BombsightLocationListener implements LocationListener {


    BombsightLocListenerCallbacks callbacks;


    public BombsightLocationListener(BombsightLocListenerCallbacks callingClass){

        this.callbacks = callingClass;
    }



    @Override
    public void onLocationChanged(Location location) {

        Point rawPoint = new Point(location.getLongitude(), location.getLatitude(), SpatialReferences.getWgs84());
        this.callbacks.onLocationChanged(rawPoint);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
