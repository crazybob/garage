package org.crazybob.garage;

public class Opener {

  private boolean shouldOpen = false;

  public synchronized boolean shouldOpen() {
    return shouldOpen;
  }

  public synchronized void waitForPress() {
    try {
      wait();
    } catch (InterruptedException e) {}
  }

  public synchronized void press() {
    shouldOpen = true;
    notifyAll();
  }

  public synchronized void reset() {
    shouldOpen = false;
  }
}
