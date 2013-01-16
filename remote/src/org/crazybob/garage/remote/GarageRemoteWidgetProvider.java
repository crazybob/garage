package org.crazybob.garage.remote;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/** @author Bob Lee (bob@squareup.com) */
public class GarageRemoteWidgetProvider extends AppWidgetProvider {
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    for (int widgetId : appWidgetIds) {
      Intent intent = new Intent(context, OpenGarage.class);
      PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
      views.setOnClickPendingIntent(R.id.button, pendingIntent);
      appWidgetManager.updateAppWidget(widgetId, views);
    }
  }
}
