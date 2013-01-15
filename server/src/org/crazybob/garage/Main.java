package org.crazybob.garage;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;
import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class Main extends Activity {

  private static final String ACTION_USB_PERMISSION = "USB_PERMISSION_ACTION";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    startService(new Intent(this, GarageService.class));

    UsbManager manager = UsbManager.getInstance(this);
    UsbAccessory[] accessoryList = manager.getAccessoryList();
    if (accessoryList != null && accessoryList.length >= 1) {
      UsbAccessory accessory = accessoryList[0];
      if (manager.hasPermission(accessory)) {
        Toast.makeText(Main.this, "Permission already granted.", Toast.LENGTH_SHORT).show();
      } else {
        askForPermission(manager, accessoryList[0]);
      }
    }

    finish();
  }

  private void askForPermission(UsbManager manager, UsbAccessory usbAccessory) {
    PendingIntent pending = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    registerReceiver(mUsbReceiver, filter);
    manager.requestPermission(usbAccessory, pending);
  }

  private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (ACTION_USB_PERMISSION.equals(action)) {
        boolean permissionGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
        if (permissionGranted) {
          Toast.makeText(Main.this, "Permission granted.", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(Main.this, "Permission denied.", Toast.LENGTH_SHORT).show();
        }
      }
    }
  };
}
