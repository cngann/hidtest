package org.example;

import org.hid4java.HidDevice;

public class PttDevice /* work on this tomorrow change to HidDevice */ {
    public HidDevice device;

    public PttDevice(HidDevice device) {
        this.device = device;
    }
    public void openPttDevice(HidDevice device) {
        device.open();
    }

    public void readFromPttDevice(HidDevice device) {
        byte[] bytes = device.readAll(10);
        if (bytes.length == 0) return;
        for (byte b : bytes) {
            System.out.printf(" %02x", b);
        }
        System.out.println();
    }

}
