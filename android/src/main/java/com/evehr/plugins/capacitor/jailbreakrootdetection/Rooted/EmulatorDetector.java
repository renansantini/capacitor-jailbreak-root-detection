package com.evehr.plugins.capacitor.jailbreakrootdetection.Rooted;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class EmulatorDetector {

    private final Context context;

    public EmulatorDetector(Context ctx) {
        context = ctx;
    }

    public boolean isEmulator() {
        return (
            checkBuildProperties() ||
            checkEmulatorFiles() ||
            checkTelephonyManager() ||
            checkQEmuDriverFile() ||
            checkQEmuProps()
        );
    }

    public boolean isDebuggedMode() {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).applicationInfo;
            return (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // --- Constantes ---

    private static final String[][] KNOWN_EMULATOR_BUILD_PROPS = {
        {"ro.hardware", "goldfish"},
        {"ro.hardware", "ranchu"},
        {"ro.kernel.qemu", "1"},
        {"ro.product.model", "sdk"},
        {"ro.product.model", "google_sdk"},
        {"ro.product.model", "sdk_x86"},
        {"ro.product.model", "vbox86p"},
    };

    private static final String[] KNOWN_EMULATOR_FILES = {
        "/dev/socket/qemud",
        "/dev/qemu_pipe",
        "/system/lib/libc_malloc_debug_qemu.so",
        "/sys/qemu_trace",
        "/system/bin/qemu-props"
    };

    private static final String[][] KNOWN_QEMU_PROPS = {
        {"ro.product.device", "qemu"},
        {"ro.product.brand", "generic"},
        {"ro.product.manufacturer", "unknown"},
        {"ro.product.model", "sdk"},
        {"ro.hardware", "goldfish"},
        {"ro.hardware", "ranchu"},
    };

    private static final int MIN_QEMU_MATCHES = 3;


    private boolean checkBuildProperties() {
        for (String[] pair : KNOWN_EMULATOR_BUILD_PROPS) {
            String prop = getSystemProperty(pair[0]);
            if (prop != null && prop.toLowerCase().contains(pair[1].toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkEmulatorFiles() {
        for (String path : KNOWN_EMULATOR_FILES) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkTelephonyManager() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            String networkOperatorName = tm.getNetworkOperatorName();
            return "android".equalsIgnoreCase(networkOperatorName);
        }
        return false;
    }

    private boolean checkQEmuDriverFile() {
        File driverFile = new File("/proc/tty/driver");
        if (driverFile.exists() && driverFile.canRead()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(driverFile)))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("goldfish") || line.contains("qemu")) {
                        return true;
                    }
                }
            } catch (IOException ignored) {
            }
        }
        return false;
    }

    private boolean checkQEmuProps() {
        int matchCount = 0;
        for (String[] pair : KNOWN_QEMU_PROPS) {
            String prop = getSystemProperty(pair[0]);
            if (prop != null && prop.toLowerCase().contains(pair[1].toLowerCase())) {
                matchCount++;
            }
        }
        return matchCount >= MIN_QEMU_MATCHES;
    }

    private String getSystemProperty(String propertyName) {
        try {
            Class<?> sp = Class.forName("android.os.SystemProperties");
            return (String) sp.getMethod("get", String.class).invoke(sp, propertyName);
        } catch (Exception e) {
            return null;
        }
    }
}