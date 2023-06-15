package com.agontuk.RNFusedLocation;

public interface Satellite {

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
