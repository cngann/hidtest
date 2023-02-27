package org.example;

import com.sun.jna.Platform;
import org.hid4java.*;
import org.hid4java.event.HidServicesEvent;

import java.util.ArrayList;
import java.util.List;

import static org.example.AnsiColors.*;

public class Main implements HidServicesListener {
    public List<HidDevice> hidDeviceList = new ArrayList<>();
    public static List<HidDevice> pttDevices = new ArrayList<>();

    @Override
    public void hidDeviceAttached(HidServicesEvent event) {
        System.out.println(ANSI_BLUE + "Device attached: " + event + ANSI_RESET);
        HidDevice hidDevice = event.getHidDevice();
        if (isPttDevice(hidDevice)) {
            attachDevice(hidDevice);
        }
    }

    @Override
    public void hidDeviceDetached(HidServicesEvent event) {
        System.out.println(ANSI_YELLOW + "Device detached: " + event + ANSI_RESET);
        pttDevices.remove(event.getHidDevice());
        System.out.println("Count: " + pttDevices.size());
    }

    @Override
    public void hidFailure(HidServicesEvent event) {
        System.out.println(ANSI_RED + "HID failure: " + event + ANSI_RESET);
    }

    @Override
    public void hidDataReceived(HidServicesEvent event) {
        System.out.printf(ANSI_PURPLE + "Data received:%n");
        byte[] dataReceived = event.getDataReceived();
        System.out.printf(ANSI_RED + event.getHidDevice().getProduct());
        System.out.printf("< [%02x]:", dataReceived.length);
        for (byte b : dataReceived) {
            System.out.printf(" %02x", b);
        }
        System.out.println(ANSI_RESET);
    }
    public void printPlatform() {
        System.out.println("Platform architecture: " + Platform.ARCH);
        System.out.println("Resource prefix: " + Platform.RESOURCE_PREFIX);
        System.out.println("Libusb activation: " + Platform.isLinux());
    }

    public boolean isPttDevice(HidDevice device) {
        boolean isPttDevice = device.getUsagePage() == 0x1 && device.getUsage() == 0x4;
        if (isPttDevice) {
            System.out.println("Found PTT Device");
        }
        return isPttDevice;
    }

    public void identifyDevices() {
        for (HidDevice hidDevice : hidDeviceList) {
            System.out.println(hidDevice.getManufacturer());
            System.out.println(hidDevice);
            if (isPttDevice(hidDevice)) {
                pttDevices.add(hidDevice);
            }
        }
        if (pttDevices.size() == 0) {
            System.out.println("No PTT Devices Found");
            System.exit(1);
        }
    }

    public void initDevices() {
        HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
        hidServicesSpecification.setAutoStart(false);
        HidServices services = HidManager.getHidServices(hidServicesSpecification);
        services.addHidServicesListener(this);
        hidDeviceList = services.getAttachedHidDevices();
        System.out.println(AnsiColors.ANSI_GREEN + "Manually starting HID services." + ANSI_RESET);
        services.start();
        System.out.println(AnsiColors.ANSI_GREEN + "Enumerating attached devices..." + ANSI_RESET);
    }

    public static class DeviceThread extends Thread {
        HidDevice device;

        DeviceThread(HidDevice device) {
            this.device = device;
        }

        public void readFromPttDevice(HidDevice device) {
            byte[] bytes = device.readAll(10);
            if (bytes.length == 0) return;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(ANSI_RED);
            stringBuilder.append(device.getManufacturer());
            stringBuilder.append(ANSI_PURPLE);
            for (byte b : bytes) {
                stringBuilder.append((char)b);
            }
            System.out.println(stringBuilder);
        }

        public void run() {
            System.out.println("Opening " + device.getManufacturer());
            while (!device.isClosed()) {
                try {
                    readFromPttDevice(device);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            if (device.isClosed()) {
                System.out.println("Closed " + device.getManufacturer());
                Thread.currentThread().interrupt();
            }
        }
    }

    public void attachDevice(HidDevice hidDevice) {
        pttDevices.add(hidDevice);
        hidDevice.open();
        new DeviceThread(hidDevice).start();
    }

    public void startup() {
        printPlatform();
        initDevices();
        identifyDevices();
        for (HidDevice device : pttDevices) {
            new DeviceThread(device).start();
        }
        while (pttDevices.size() > 0) {
            // do nothing
        }
    }
    public static void main(String[] args) {
        Main main = new Main();
        main.startup();
        System.out.println("I quit");
    }
}
