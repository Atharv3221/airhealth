package github.atharv3221.airhealth.controllers;

import github.atharv3221.airhealth.dto.CreateSubscriptionRequest;
import github.atharv3221.airhealth.dto.SubscriptionResponse;
import github.atharv3221.airhealth.dto.UpdateSubscriptionRequest;
import github.atharv3221.airhealth.service.SubscriptionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

  private final SubscriptionService subscriptionService;

  public SubscriptionController(SubscriptionService subscriptionService) {
    this.subscriptionService = subscriptionService;
  }

  @PostMapping("/area")
  public SubscriptionResponse subscribeArea(Authentication auth,
          @RequestBody CreateSubscriptionRequest request) {
    return subscriptionService.createAreaSubscription(userId(auth), request);
  }

  @GetMapping
  public List<SubscriptionResponse> listMine(Authentication auth) {
    return subscriptionService.listMine(userId(auth));
  }

  @PatchMapping("/{id}")
  public SubscriptionResponse update(Authentication auth, @PathVariable Long id,
          @RequestBody UpdateSubscriptionRequest request) {
    return subscriptionService.update(userId(auth), id, request);
  }

  @DeleteMapping("/{id}")
  public void unsubscribe(Authentication auth, @PathVariable Long id) {
    subscriptionService.unsubscribe(userId(auth), id);
  }

  private Long userId(Authentication auth) {
    return Long.parseLong(auth.getName());
  }
}
