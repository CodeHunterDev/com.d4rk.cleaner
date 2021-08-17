package com.d4rk.cleaner.rambooster.interfaces;
public interface Booster {
    void setScanListener (ScanListener listener);
    void setCleanListener (CleanListener listener);
    void startScan(boolean isSystem);
    void startClean();
    void setDebug (boolean isDebug);
}