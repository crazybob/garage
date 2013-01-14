package org.crazybob.garage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Main extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    startService(new Intent(this, GarageService.class));
    finish();
  }
}
