package com.agontuk.RNFusedLocation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;

import com.facebook.react.bridge.ReactApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class GpsSatelliteStatusProvider implements SatelliteStatusProvider {
  private final LocationManager locationManager;

  private final GpsStatus.Listener gpsStatusListener;

  @SuppressLint("MissingPermission")
  public GpsSatelliteStatusProvider(ReactApplicationContext context, SatelliteStatusListener satelliteStatusListener) {
    this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    this.gpsStatusListener = event -> {
      GpsStatus gpsStatus = locationManager.getGpsStatus(null);
      if (gpsStatus != null) {
        List<Satellite> satellites = new ArrayList<>();
        //noinspection deprecation
        for (GpsSatellite satellite : gpsStatus.getSatellites()) {
          satellites.add(new RNGpsSatellite(satellite));
        }
        satelliteStatusListener.onSatelliteStatusChange(GpsSatelliteStatusProvider.this, new SatelliteStatus(satellites));
      }
    };
  }

  @SuppressLint("MissingPermission")
  @Override
  public void removeSatelliteStatusUpdates() {
    locationManager.addGpsStatusListener(gpsStatusListener);
  }

  @Override
  public void requestSatelliteStatusUpdates() {
    locationManager.removeGpsStatusListener(gpsStatusListener);
  }
}
