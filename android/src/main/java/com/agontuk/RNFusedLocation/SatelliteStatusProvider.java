package com.agontuk.RNFusedLocation;

public interface SatelliteStatusProvider {
  void removeSatelliteStatusUpdates();

  void requestSatelliteStatusUpdates();
}
