//
//  UIDevice+JailBroken.swift
//  IsJailBroken
//
//  Created by Vineet Choudhary on 07/02/20.
//  Copyright © 2020 Developer Insider. All rights reserved.
//

import Foundation
import UIKit
import Darwin

extension UIDevice {
    
    var isSimulator: Bool {
        return ProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != nil
    }

    var isDebuggedMode: Bool {
        var info = kinfo_proc()
        var size = MemoryLayout<kinfo_proc>.stride
        var name: [Int32] = [CTL_KERN, KERN_PROC, KERN_PROC_PID, getpid()]
        let result = sysctl(&name, u_int(name.count), &info, &size, nil, 0)
        return result == 0 && (info.kp_proc.p_flag & P_TRACED) != 0
    }
    
    var isJailBroken: Bool {
        #if targetEnvironment(simulator)
        return false
        #else
        return JailBrokenHelper.hasCydiaInstalled() ||
               JailBrokenHelper.containsSuspiciousApps() ||
               JailBrokenHelper.suspiciousSystemPathsExist() ||
               JailBrokenHelper.canEditSystemFiles()
        #endif
    }
}

private struct JailBrokenHelper {
    
    static func hasCydiaInstalled() -> Bool {
        guard let url = URL(string: "cydia://") else { return false }
        return UIApplication.shared.canOpenURL(url)
    }
    
    static func containsSuspiciousApps() -> Bool {
        return suspiciousAppsPathToCheck.contains { FileManager.default.fileExists(atPath: $0) }
    }
    
    static func suspiciousSystemPathsExist() -> Bool {
        return suspiciousSystemPathsToCheck.contains { FileManager.default.fileExists(atPath: $0) }
    }
    
    static func canEditSystemFiles() -> Bool {
        let testPath = "/private/jailbreak_test.txt"
        do {
            try "test".write(toFile: testPath, atomically: true, encoding: .utf8)
            try FileManager.default.removeItem(atPath: testPath)
            return true
        } catch {
            return false
        }
    }

    static var suspiciousAppsPathToCheck: [String] {
        return [
            "/Applications/Cydia.app",
            "/Applications/blackra1n.app",
            "/Applications/FakeCarrier.app",
            "/Applications/Icy.app",
            "/Applications/IntelliScreen.app",
            "/Applications/MxTube.app",
            "/Applications/RockApp.app",
            "/Applications/SBSettings.app",
            "/Applications/WinterBoard.app"
        ]
    }
    
    static var suspiciousSystemPathsToCheck: [String] {
        return [
            "/Library/MobileSubstrate/DynamicLibraries/LiveClock.plist",
            "/Library/MobileSubstrate/DynamicLibraries/Veency.plist",
            "/private/var/lib/apt",
            "/private/var/lib/apt/",
            "/private/var/lib/cydia",
            "/private/var/mobile/Library/SBSettings/Themes",
            "/private/var/stash",
            "/private/var/tmp/cydia.log",
            "/System/Library/LaunchDaemons/com.ikey.bbot.plist",
            "/System/Library/LaunchDaemons/com.saurik.Cydia.Startup.plist",
            "/usr/bin/sshd",
            "/usr/libexec/sftp-server",
            "/usr/sbin/sshd",
            "/etc/apt",
            "/bin/bash",
            "/Library/MobileSubstrate/MobileSubstrate.dylib"
        ]
    }
}

