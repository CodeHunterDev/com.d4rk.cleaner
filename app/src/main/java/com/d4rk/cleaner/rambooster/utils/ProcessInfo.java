package com.d4rk.cleaner.rambooster.utils;
import android.app.ActivityManager;
public class ProcessInfo {
    private int memoryUsage;
    private final String processName;
    public ProcessInfo(ActivityManager.RunningAppProcessInfo processInfo) {
        this.processName = processInfo.processName;
    }
    public long getSize() {
        return memoryUsage;
    }
    public void setMemoryUsage(int memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
    public String getProcessName() {
        return processName;
    }
}