package com.d4rk.cleaner.rambooster.tasks;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import com.d4rk.cleaner.rambooster.RAMBooster;
import com.d4rk.cleaner.rambooster.interfaces.CleanListener;
import com.d4rk.cleaner.rambooster.utils.Constants;
import com.d4rk.cleaner.rambooster.utils.ProcessInfo;
import com.d4rk.cleaner.rambooster.utils.Utils;
import java.util.List;
public class CleanTask implements Runnable, Constants {
    private final Context context;
    private final List < ProcessInfo > appProcessInfos;
    private final CleanListener listener;
    public CleanTask(Context context, List < ProcessInfo > appProcessInfos, CleanListener listener) {
        this.context = context;
        this.appProcessInfos = appProcessInfos;
        this.listener = listener;
    }
    @Override
    public void run() {
        if (RAMBooster.isDEBUG())
            Log.d(TAG, "Cleaner started...");
        listener.onStarted();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
        if (appProcessInfos != null) {
            killAppProcesses(appProcessInfos);
        }
        long availableRam = Utils.calculateAvailableRAM(context) / weight;
        long totalRam = Utils.calculateTotalRAM() / weight;
        listener.onFinished(availableRam, totalRam);
        if (RAMBooster.isDEBUG())
            Log.d(TAG, "Cleaner finished");
    }
    private void killAppProcesses(List < ProcessInfo > runningAppProcesses) {
        for (ProcessInfo processInfo: runningAppProcesses) {
            killBackgroundProcess(processInfo.getProcessName());
        }
    }
    private void killBackgroundProcess(String packageName) {
        ActivityManager manager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        manager.killBackgroundProcesses(packageName);
    }
}