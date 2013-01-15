package org.crazybob.garage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import org.eclipse.jetty.server.Server;

public class GarageService extends Service {

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    start();
    return START_STICKY;
  }

  private final Opener opener = new Opener();

  private boolean started = false;

  private void start() {
    if (started) {
      Log.i("Garage", "Already started.");
      Toast.makeText(this, "Already running.", Toast.LENGTH_SHORT).show();
      return;
    }

    // No need to clean up after failures. If anything raises an exception, the process should die.
    startForeground(1, notificationWith("Starting..."));
    startWebServer();
    stayAwake();
    startUsb();

    Toast.makeText(this, "Garage service started.", Toast.LENGTH_SHORT).show();
    started = true;
  }

  private void startUsb() {
    new Thread(new UsbHandler(this, opener)).start();
  }

  // Keep a reference in case the lock gets released in the finalizer.
  private PowerManager.WakeLock wakeLock;

  private void stayAwake() {
    // Keep the device awake. PARTIAL_WAKE_LOCK isn't sufficient because the WIFI slows down. Keeping the screen
    // slightly on keeps WIFI going at full speed.
    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Garage");
    wakeLock.acquire();

    // Keep WIFI on.
    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "Garage").acquire();
  }

  private Notification notificationWith(String message) {
    Notification notification = new Notification(R.drawable.icon,
        "Garage ready.", System.currentTimeMillis());
    PendingIntent pending = PendingIntent.getActivity(this, 0, new Intent(this, Main.class), 0);
    notification.setLatestEventInfo(this, "Garage", message, pending);
    return notification;
  }

  void updateNotification(String message) {
    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    manager.notify(1, notificationWith(message));
  }

  private void startWebServer() {
    Server server = new Server(8080);
    server.setHandler(new WebHandler(this, opener));
    try {
      server.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
