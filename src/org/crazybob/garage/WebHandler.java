package org.crazybob.garage;

import android.util.Log;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Web interface for garage door opener.
 */
public class WebHandler extends AbstractHandler {

  private final GarageService garageService;
  private Opener opener;

  public WebHandler(GarageService garageService, Opener opener) {
    this.garageService = garageService;
    this.opener = opener;
  }

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request,
                     HttpServletResponse response) throws IOException, ServletException {
    try {
      if (target.equals("/garage")) {
        String method = request.getMethod().toUpperCase();
        if (method.equals("GET")) {
          handleGet(request, response);
          return;
        } else if (method.equals("POST")) {
          handlePost(request, response);
          return;
        }
      }

      Log.i("Garage", "Not found: " + target);
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } finally {
      baseRequest.setHandled(true);
    }
  }

  private void handleGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;charset=utf-8");
    response.setStatus(HttpServletResponse.SC_OK);

    // TODO: Use an image instead. Make more mobile friendly.
    response.getWriter().println("<html>" +
        "<head><meta name=\"viewport\" content=\"width=device-width," +
        " initial-scale=1.0, user-scalable=yes\"></head>" +
        "<body><form method=\"POST\" action=\"/garage\">" +
        "<input type=\"submit\" value=\"Open\">" +
        "</form></body>" +
        "</html>");
  }

  private void handlePost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Log.i("Garage", "Opening...");
    opener.press();
    response.sendRedirect("/garage");
  }
}
