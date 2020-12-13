package com.d4rk.cleaner;
import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.widget.TextView;
import com.fxn.stash.Stash;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import static com.d4rk.cleaner.WhitelistActivity.getWhiteList;
@SuppressWarnings("StatementWithEmptyBody")
public class FileScanner{
    private final File path;
    private Resources res;
    private MainActivity gui;
    private int filesRemoved = 0;
    private long kilobytesTotal = 0;
    private boolean delete = false;
    private boolean emptyDir = false;
    private boolean autoWhite = true;
    private boolean corpse = false;
    private static final ArrayList<String> filters = new ArrayList<>();
    private static final String[] protectedFileList = {"backup", "copy", "copies", "important", "do_not_edit"};
    FileScanner(File path) {
        this.path = path;
    }
    private List<File> getListFiles() {
        return getListFiles(path);
    }
    /**
     * Used to generate a list of all files on device.
     *
     * @param parentDirectory where to start searching from.
     * @return List of all files on device (besides whitelisted ones).
     */
    private synchronized List<File> getListFiles(File parentDirectory) {
        ArrayList<File> inFiles = new ArrayList<>();
        File[] files = parentDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!isWhiteListed(file)) {
                    if (file.isDirectory()) {
                        if (autoWhite) {
                            if (!autoWhiteList(file))
                                inFiles.add(file);
                        } else inFiles.add(file);
                        inFiles.addAll(getListFiles(file));
                    } else inFiles.add(file);
                }
            }
        }
        return inFiles;
    }
    /**
     * Runs a for each loop through the white list, and compares the path of the file
     * to each path in the list
     *
     * @param file file to check if in the whitelist
     * @return true if is the file is in the white list, false if not
     */
    private synchronized boolean isWhiteListed(File file) {
        for (String path : getWhiteList())
            if (path.equalsIgnoreCase(file.getAbsolutePath())
                    || path.equalsIgnoreCase(file.getName()))
                return true;
        return false;
    }
    /**
     * Runs before anything is filtered/cleaned. Automatically adds folders to the whitelist
     * based on the name of the folder itself
     *
     * @param file file to check whether it should be added to the whitelist
     */
    private synchronized boolean autoWhiteList(File file) {
        for (String protectedFile : protectedFileList) {
            if (file.getName().toLowerCase().contains(protectedFile) &&
                    !getWhiteList().contains(file.getAbsolutePath().toLowerCase())) {
                getWhiteList().add(file.getAbsolutePath().toLowerCase());
                Stash.put("whiteList", getWhiteList());
                return true;
            }
        }
        return false;
    }
    /**
     * Runs as for each loop through the filter, and checks if
     * the file matches any filters
     *
     * @param file file to check
     * @return true if the file's extension is in the filter, false otherwise
     */
    public synchronized boolean filter(File file) {
        // corpse checking - TODO: needs improved!
        if (file.getParentFile() != null && file.getParentFile().getParentFile() != null && corpse)
            if (file.getParentFile().getName().equals("data") && file.getParentFile().getParentFile().getName().equals("Android"))
                if (!getInstalledPackages().contains(file.getName()) && !file.getName().equals(".nomedia"))
                    return true;
        if (file.isDirectory() && isDirectoryEmpty(file)
                && emptyDir) return true;
        for (String filter : filters)
            if (file.getAbsolutePath().toLowerCase()
                    .matches(filter.toLowerCase()))
                return true;
        return false;
    }
    private synchronized List<String> getInstalledPackages() {
        final PackageManager pm = gui.getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<String> packagesString = new LinkedList<>();
        for (ApplicationInfo packageInfo : packages) {
            packagesString.add(packageInfo.packageName);
        }
        return packagesString;
    }
    /**
     * lists the contents of the file to an array, if the array length is 0, then return true,
     * else false
     *
     * @param directory directory to test
     * @return true if empty, false if containing a file(s)
     */
    private synchronized boolean isDirectoryEmpty(File directory) {
        if (directory.list() != null) return Objects.requireNonNull(directory.list()).length == 0;
        else return false;
    }
    /**
     * Adds paths to the white list that are not to be cleaned. As well as adds
     * extensions to filter. 'generic', 'aggressive', and 'apk' should be assigned
     * by calling settings.getBoolean()
     */
    synchronized void setUpFilters(boolean generic, boolean aggressive, boolean apk) {
        List<String> folders = new ArrayList<>();
        List<String> files = new ArrayList<>();
        if (gui != null)
            setResources(gui.getResources());
        if (generic) {
            folders.addAll(Arrays.asList(res.getStringArray(R.array.generic_filter_folders)));
            files.addAll(Arrays.asList(res.getStringArray(R.array.generic_filter_files)));
        }
        if (aggressive) {
            folders.addAll(Arrays.asList(res.getStringArray(R.array.aggressive_filter_folders)));
            files.addAll(Arrays.asList(res.getStringArray(R.array.aggressive_filter_files)));
        }
        filters.clear();
        for (String folder : folders)
            filters.add(getRegexForFolder(folder));
        for (String file : files)
            filters.add(getRegexForFile(file));
        if (apk) filters.add(getRegexForFile(".apk"));
    }
    @SuppressLint("SetTextI18n")
    long startScan() {
        byte cycles = 0;
        byte maxCycles = 10;
        List<File> foundFiles;
        if (!delete) maxCycles = 1;
        while (cycles < maxCycles) {
            foundFiles = getListFiles();
            if (gui != null) gui.scanPBar.setMax(gui.scanPBar.getMax() + foundFiles.size());
            for (File file : foundFiles) {
                if (filter(file)) {
                    TextView tv = null;
                    if (gui != null)
                        tv = gui.displayPath(file);
                    if (delete) {
                        kilobytesTotal += file.length();
                        ++filesRemoved;
                        if (file.delete()) {
                        } else if (tv != null) tv.setTextColor(Color.GRAY);
                    } else {
                        kilobytesTotal += file.length();
                    }
                }
                if (gui != null) {
                    gui.runOnUiThread(() -> gui.scanPBar.setProgress(gui.scanPBar.getProgress() + 1));
                    double scanPercent = gui.scanPBar.getProgress() * 100.0 / gui.scanPBar.getMax();
                    gui.runOnUiThread(() -> gui.progressText.setText(String.format(Locale.US, "%.0f", scanPercent) + "%"));
                }
            }
            if (filesRemoved == 0) break;
            filesRemoved = 0;
            ++cycles;
        }
        return kilobytesTotal;
    }
    private String getRegexForFolder(String folder) {
        return ".*(\\\\|/)" + folder + "(\\\\|/|$).*";
    }
    private String getRegexForFile(String file) {
        return ".+" + file.replace(".", "\\.") + "$";
    }
    void setGUI(MainActivity gui) {
        this.gui = gui;
    }
    void setResources(Resources res) {
        this.res = res;
    }
    void setEmptyDir(boolean emptyDir) {
        this.emptyDir = emptyDir;
    }
    void setDelete(boolean delete) {
        this.delete = delete;
    }
    void setCorpse(boolean corpse) {
        this.corpse = corpse;
    }
    void setAutoWhite(boolean autoWhite) {
        this.autoWhite = autoWhite;
    }
}