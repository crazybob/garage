package org.crazybob.garage.remote;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/** @author Bob Lee (bob@squareup.com) */
public class OpenGarage extends Service {

  private Handler mainThread;

  private static final int VIBRATE_MS = 100;

  @Override public void onCreate() {
    mainThread = new Handler(getMainLooper());
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Toast.makeText(this, "Opening...", Toast.LENGTH_LONG).show();
    Log.i("GarageRemote", "Opening...");
    final URL url;
    try {
      url = new URL(this.getString(R.string.garage_url));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    new Thread() {
      @Override public void run() {
        try {
          HttpURLConnection connection = (HttpURLConnection) url.openConnection();
          connection.setRequestMethod("POST");
          connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
          connection.setRequestProperty("Content-Length", "0");
          connection.setUseCaches(false);
          connection.setDoInput(true);
          connection.setDoOutput(true);
          connection.setConnectTimeout(5000);
          connection.setReadTimeout(5000);
          connection.getOutputStream().close();
          connection.getInputStream().close();
          int code = connection.getResponseCode();
          Log.i("GarageRemote", "Response: " + code);
          if (code >= 200 && code < 300) {
            toast("Success");
            v.vibrate(VIBRATE_MS);
          } else {
            toast("Error: " + code);
            vibrateError();
          }
        } catch (Exception e) {
          Log.w("GarageRemote", e);
          toast("I/O error");
          vibrateError();
        } finally {
          stopSelf();
        }
      }

      private void vibrateError() {
        v.vibrate(new long[] { 0, VIBRATE_MS, 250, VIBRATE_MS }, -1);
      }
    }.start();
    return START_NOT_STICKY;
  }

  private void toast(final String message) {
    mainThread.post(new Runnable() {
      @Override public void run() {
        Toast.makeText(OpenGarage.this, message, Toast.LENGTH_LONG).show();
      }
    });
  }

  @Override public IBinder onBind(Intent intent) {
    return null;
  }
}
