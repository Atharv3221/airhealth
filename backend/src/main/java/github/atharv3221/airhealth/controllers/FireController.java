package github.atharv3221.airhealth.controllers;

import java.util.List;

import github.atharv3221.airhealth.dto.FirePoint;
import github.atharv3221.airhealth.dto.FireSummary;
import github.atharv3221.airhealth.service.FireService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/fires")
public class FireController {
  private final FireService service;
  public FireController(FireService service) { this.service = service; }

  @GetMapping
  public List<FirePoint> fires(@RequestParam double west, @RequestParam double south,
                               @RequestParam double east, @RequestParam double north,
                               @RequestParam(defaultValue = "2") int days) {
    if (east < west || north < south) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bounds");
    }
    if ((east - west) > 15 || (north - south) > 15) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zoom in to see fires");
    }
    return service.getFires(west, south, east, north, Math.min(Math.max(days, 1), 5));
  }

  @GetMapping("/nearby")
  public FireSummary nearby(@RequestParam double lat, @RequestParam double lon,
                            @RequestParam(defaultValue = "50") int radiusKm,
                            @RequestParam(defaultValue = "2") int days) {
    // the box is small, so the 15-degree guard above doesn't apply
    if (!Double.isFinite(lat) || !Double.isFinite(lon) || Math.abs(lat) > 85 || Math.abs(lon) > 180) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid location");
    }
    return service.summarize(lat, lon, Math.min(Math.max(radiusKm, 10), 100),
        Math.min(Math.max(days, 1), 5));
  }
}
