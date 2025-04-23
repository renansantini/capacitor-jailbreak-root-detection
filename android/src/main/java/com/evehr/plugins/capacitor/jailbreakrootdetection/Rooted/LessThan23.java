package com.evehr.plugins.capacitor.jailbreakrootdetection.Rooted;

import java.io.File;

public class LessThan23 implements CheckApiVersion {
    @Override
    public boolean checkRooted() {
        return canExecuteCommand("/system/xbin/which su") || isSuperuserPresent();
    }

    private static boolean canExecuteCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = in.readLine();
            return output != null && !output.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isSuperuserPresent() {
        String[] paths = {
            "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/system/app/Superuser.apk"
        };

        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }
}
