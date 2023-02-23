package org.example;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesSpecification;
import org.hid4java.jna.HidApi;

public class Main extends BaseExample {

    public static void main(String[] args) {
        Main main = new Main();
        main.printPlatform();
        HidDevice device = main.init();
        System.out.println("+++" + device.getManufacturer());
        while (!device.isClosed()) {
            main.readFromPttDevice(device);
            // do nothing
        }
    }
}
