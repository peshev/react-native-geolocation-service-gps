package com.agontuk.RNFusedLocation;

public interface RNSatellite {

  int getSvid();

  float getCn0DbHz();

  float getElevationDegrees();

  float getAzimuthDegrees();

  boolean hasEphemerisData();

  boolean hasAlmanacData();

  boolean usedInFix();

  boolean hasCarrierFrequencyHz();

  float getCarrierFrequencyHz();
}
