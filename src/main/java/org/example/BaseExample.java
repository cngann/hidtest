/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Gary Rowe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.example;

import com.sun.jna.Platform;
import org.hid4java.*;
import org.hid4java.event.HidServicesEvent;

public class BaseExample implements HidServicesListener {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";

    public void printPlatform() {
        System.out.println("Platform architecture: " + Platform.ARCH);
        System.out.println("Resource prefix: " + Platform.RESOURCE_PREFIX);
        System.out.println("Libusb activation: " + Platform.isLinux());
    }

    @Override
    public void hidDeviceAttached(HidServicesEvent event) {
        System.out.println(ANSI_BLUE + "Device attached: " + event + ANSI_RESET);
        openPttDevice(event.getHidDevice());
    }

    @Override
    public void hidDeviceDetached(HidServicesEvent event) {
        System.out.println(ANSI_YELLOW + "Device detached: " + event + ANSI_RESET);
    }

    @Override
    public void hidFailure(HidServicesEvent event) {
        System.out.println(ANSI_RED + "HID failure: " + event + ANSI_RESET);
    }

    @Override
    public void hidDataReceived(HidServicesEvent event) {
        System.out.printf(ANSI_PURPLE + "Data received:%n");
        byte[] dataReceived = event.getDataReceived();
        System.out.printf("< [%02x]:", dataReceived.length);
        for (byte b : dataReceived) {
            System.out.printf(" %02x", b);
        }
        System.out.println(ANSI_RESET);
    }

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
        System.out.println("No PTT Devices Found");
        System.exit(1);
        return null;
    }

    public HidDevice init() {
//        HidApi.logTraffic = true;
        HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
        hidServicesSpecification.setAutoStart(false);
        HidServices services = HidManager.getHidServices(hidServicesSpecification);
        services.addHidServicesListener(this);
        System.out.println(ANSI_GREEN + "Manually starting HID services." + ANSI_RESET);
        services.start();
        System.out.println(ANSI_GREEN + "Enumerating attached devices..." + ANSI_RESET);
        HidDevice device = findPttDevice(services);
        openPttDevice(device);
        return device;
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