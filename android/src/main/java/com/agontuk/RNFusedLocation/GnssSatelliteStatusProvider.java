package com.agontuk.RNFusedLocation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.GnssStatus;
import android.location.LocationManager;

import com.facebook.react.bridge.ReactApplicationContext;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressLint("NewApi")
public class GnssSatelliteStatusProvider implements SatelliteStatusProvider {
  private final LocationManager locationManager;

  private final GnssStatus.Callback gnssStatusCallback;

  public GnssSatelliteStatusProvider(ReactApplicationContext context, SatelliteStatusListener satelliteStatusListener) {
    this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    gnssStatusCallback = new GnssStatus.Callback() {
      @Override
      public void onSatelliteStatusChanged(GnssStatus status) {
        satelliteStatusListener.onSatelliteStatusChange(
          GnssSatelliteStatusProvider.this,
          new SatelliteStatus(IntStream
            .range(0, status.getSatelliteCount())
            .mapToObj(i -> new RNGnssSatellite(status, i))
            .collect(Collectors.toList())));
      }
    };

  }

  @Override
  public void removeSatelliteStatusUpdates() {
    locationManager.unregisterGnssStatusCallback(gnssStatusCallback);
  }

  @Override
  @SuppressLint("MissingPermission")
  public void requestSatelliteStatusUpdates() {
    locationManager.registerGnssStatusCallback(gnssStatusCallback);
  }
}
