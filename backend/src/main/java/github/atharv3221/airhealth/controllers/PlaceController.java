package github.atharv3221.airhealth.controllers;

import github.atharv3221.airhealth.dto.PlaceResponse;
import github.atharv3221.airhealth.service.PlaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

  private final PlaceService placeService;

  public PlaceController(PlaceService placeService) {
    this.placeService = placeService;
  }

  @GetMapping("/search")
  public List<PlaceResponse> search(@RequestParam String q) {
    return placeService.search(q);
  }

  @GetMapping("/reverse")
  public PlaceResponse reverse(@RequestParam double lat, @RequestParam double lon) {
    return placeService.reverse(lat, lon);
  }
}
