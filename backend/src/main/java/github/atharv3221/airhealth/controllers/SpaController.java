package github.atharv3221.airhealth.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

  // client-side routes from frontend/src/App.jsx; forwarding lets a browser refresh load the React app
  @GetMapping({"/about", "/login", "/signup", "/app", "/app/subscriptions"})
  public String forwardToIndex() {
    return "forward:/index.html";
  }
}
