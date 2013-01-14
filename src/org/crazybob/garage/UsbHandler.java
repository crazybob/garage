package org.crazybob.garage;

import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import java.io.*;

public class UsbHandler implements Runnable {

  private static final int DELAY = 5;

  private final GarageService garageService;
  private Opener opener;

  public UsbHandler(GarageService garageService, Opener opener) {
    this.garageService = garageService;
    this.opener = opener;
  }

  @Override
  public void run() {
    UsbManager manager = UsbManager.getInstance(garageService);
    while (true) {
      UsbAccessory[] accessoryList = manager.getAccessoryList();

      if (accessoryList == null || accessoryList.length < 1) {
        Log.i("Garage", "No device connected.");
        sleep(DELAY);
        continue;
      }

      UsbAccessory accessory = accessoryList[0];
      if (!manager.hasPermission(accessory)) {
        Log.i("Garage", "We don't have permission yet.");
        sleep(DELAY);
        continue;
      }

      ParcelFileDescriptor pfd = manager.openAccessory(accessory);
      if (pfd == null) {
        Log.i("Garage", "Failed to open accessory.");
        sleep(DELAY);
        continue;
      }

      try {
        FileDescriptor fd = pfd.getFileDescriptor();
        new Thread(new IgnoreInput(pfd, new FileInputStream(fd))).start();
        OutputStream out = new FileOutputStream(fd);

        while (true) {
          if (opener.shouldOpen()) {
            setRelay(out, 1);
            sleep(1);
            setRelay(out, 0);
            opener.reset();
          } else {
            opener.waitForPress();
          }
        }
      } catch (Exception e) {
        try {
          pfd.close();
        } catch (IOException e1) {}

        Log.w("Garage", e);
        sleep(DELAY);
      }
    }
  }

  private void setRelay(OutputStream out, int state) throws IOException {
    byte[] buffer = new byte[3];
    buffer[0] = 3; // Relay
    buffer[1] = 0; // #1
    buffer[2] = (byte) state;
    out.write(buffer);
  }

  class IgnoreInput implements Runnable {

    private ParcelFileDescriptor pfd;
    private FileInputStream in;

    public IgnoreInput(ParcelFileDescriptor pfd, FileInputStream in) {
      this.pfd = pfd;
      this.in = in;
    }

    @Override
    public void run() {
      byte[] buffer = new byte[4096];
      while (true) {
        try {
          in.read(buffer);
        } catch (IOException e) {
          Log.w("Garage", e);
          try {
            pfd.close();
          } catch (IOException e1) {}
          return;
        }
      }
    }
  }

  private void sleep(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException e) {}
  }
}
