package com.d4rk.cleaner.rambooster.interfaces;
public interface CleanListener {
    void onStarted();
    void onFinished(long availableRam, long totalRam);
}
