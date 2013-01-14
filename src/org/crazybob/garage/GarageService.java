package org.crazybob.garage;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

  private void runInForeground() {
    Notification notification = new Notification(R.drawable.icon,
        "Garage ready.", System.currentTimeMillis());
    PendingIntent pending = PendingIntent.getActivity(this, 0, new Intent(this, Main.class), 0);
    notification.setLatestEventInfo(this, "Garage", "Ready", pending);
    startForeground(1, notification);
  }

  private boolean started = false;

  private void start() {
    if (started) {
      Log.i("Garage", "Already started.");
      Toast.makeText(this, "Already running.", Toast.LENGTH_SHORT).show();
      return;
    }

    // No need to clean up after failures. If anything raises an exception, the process should die.
    startWebServer();
    stayAwake();
    runInForeground();

    Toast.makeText(this, "Garage service started.", Toast.LENGTH_SHORT).show();
    started = true;
  }

  // Keep a reference in case the lock gets released in the finalizer.
  private PowerManager.WakeLock wakeLock;

  private void stayAwake() {
    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Garage");
    wakeLock.acquire();
  }

  private void startWebServer() {
    Server server = new Server(8080);
    server.setHandler(new GarageHandler(this));
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
