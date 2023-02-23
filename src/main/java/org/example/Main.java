package org.example;

import org.hid4java.HidDevice;

public class Main extends BaseExample {

    public static void main(String[] args) {
        Main main = new Main();
        main.printPlatform();
        HidDevice device = main.init();
        System.out.println("+++" + device.getManufacturer());
        while (!device.isClosed()) {
            main.readFromPttDevice(device);
        }
    }
}
