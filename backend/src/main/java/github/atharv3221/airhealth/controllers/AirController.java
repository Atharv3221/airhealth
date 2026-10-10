package github.atharv3221.airhealth.controllers;

import github.atharv3221.airhealth.dto.AirQualityResponse;
import github.atharv3221.airhealth.service.AirService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/air")
public class AirController {

  private final AirService airService;

  public AirController(AirService airService) {
    this.airService = airService;
  }

  @GetMapping
  public AirQualityResponse get(@RequestParam double lat, @RequestParam double lon) {
    return airService.getCurrent(lat, lon);
  }
}
