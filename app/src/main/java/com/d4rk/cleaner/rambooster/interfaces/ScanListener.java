package com.d4rk.cleaner.rambooster.interfaces;
import com.d4rk.cleaner.rambooster.utils.ProcessInfo;
import java.util.List;
public interface ScanListener {
    void onStarted();
    void onClick();
    boolean onSupportNavigateUp();
    void onFinished(long availableRam, long totalRam, List<ProcessInfo> appsToClean);
}