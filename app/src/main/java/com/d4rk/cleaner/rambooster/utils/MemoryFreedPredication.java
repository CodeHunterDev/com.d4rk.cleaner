package com.d4rk.cleaner.rambooster.utils;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug.MemoryInfo;
/**
 * @author Pratik Poat(prtkpopat@yahoo.com)
 * @info calculate memory freed predication of processes.
 * *
 */
public class MemoryFreedPredication {
    private final ActivityManager activityManager;
    private static MemoryFreedPredication INSTANCE;
    /**
     * gives object(singleton) of MemoryFreePredication
     */
    public static MemoryFreedPredication getInstance(Context context) {
        if (INSTANCE != null)
            return INSTANCE;
        else
            return INSTANCE = new MemoryFreedPredication(context);
    }
    /**
     * Constructor
     */
    private MemoryFreedPredication(Context context) {
        activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
    }
    /**
     * calculates memory predication of given process ids
     */
    public int calculateMemoryUsage(int pid) {
        MemoryInfo[] memoryInfo = activityManager.getProcessMemoryInfo(new int[] {
                pid
        });
        int totalMemory = 0;
        for (MemoryInfo info: memoryInfo) {
            totalMemory += (info.dalvikPss + info.nativePss + info.otherPss);
        }
        return totalMemory * 1024;
    }
}