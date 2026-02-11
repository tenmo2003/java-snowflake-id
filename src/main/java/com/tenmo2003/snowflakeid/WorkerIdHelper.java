package com.tenmo2003.snowflakeid;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Enumeration;

/**
 * @author anhvn
 * @since 2026-02-10
 */
public class WorkerIdHelper {

    private WorkerIdHelper() {}

    public static int getMachineIdBasedOnMAC(int maxMachineId) {
        int generated;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                }
            }
            generated = sb.toString().hashCode();
        } catch (Exception e) {
            generated = (new SecureRandom()).nextInt();
        }

        generated = generated & maxMachineId;
        return generated;
    }
}
