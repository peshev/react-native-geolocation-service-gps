package com.agontuk.RNFusedLocation;

public interface SatelliteStatusListener {
  void onSatelliteStatusChange(SatelliteStatusProvider satelliteStatusProvider, SatelliteStatus satelliteStatus);
}
