package org.example;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesSpecification;
import org.hid4java.jna.HidApi;

public class Main extends BaseExample {

    public boolean isPttDevice(HidDevice device) {
        return device.getUsagePage() == 0x1 && device.getUsage() == 0x4;
    }

    public HidDevice findPttDevice(HidServices services) {
        for (HidDevice hidDevice : services.getAttachedHidDevices()) {
            System.out.println(hidDevice.getManufacturer());
            System.out.println(hidDevice);
            if (isPttDevice(hidDevice)) {
                System.out.println("Found PTT Device");
                return hidDevice;
            }
        }
        return null;
    }

    public void openPttDevice(HidDevice device) {
        device.open();
    }

    public void readFromPttDevice(HidDevice device) {
        device.readAll(1000);
    }
    public HidServices execute() {
        HidApi.logTraffic = true;
        // Configure to use custom specification
        HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
        hidServicesSpecification.setAutoStart(false);
        HidServices hidServices = HidManager.getHidServices(hidServicesSpecification);
        hidServices.addHidServicesListener(this);
        System.out.println(ANSI_GREEN + "Manually starting HID services." + ANSI_RESET);
        hidServices.start();
        System.out.println(ANSI_GREEN + "Enumerating attached devices..." + ANSI_RESET);
        return hidServices;
    }
    public static void main(String[] args) {
        Main main = new Main();
        main.printPlatform();
        HidServices services = main.execute();
        HidDevice device = main.findPttDevice(services);
        main.openPttDevice(device);
        while (true) {
            main.readFromPttDevice(device);
            // do nothing
        }
    }
}
