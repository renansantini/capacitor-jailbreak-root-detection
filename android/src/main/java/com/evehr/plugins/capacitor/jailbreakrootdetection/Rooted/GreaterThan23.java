package com.evehr.plugins.capacitor.jailbreakrootdetection.Rooted;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class GreaterThan23 implements CheckApiVersion {
    @Override
    public boolean checkRooted() {
        return hasSUFile() || canExecuteSU();
    }

    private boolean hasSUFile() {
        String[] paths = {
            "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su"
        };

        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private boolean canExecuteSU() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return in.readLine() != null;
            }
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }
}
