package github.atharv3221.airhealth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscriptions")
public class Subscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "area_id")
  private Area area;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "route_id")
  private Route route;

  // the user's own label for this subscription; null means "use the area's name"
  @Column(length = 120)
  private String name;

  // what kind of place this is, picked by the user; defaults to OTHER
  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private SubscriptionType type = SubscriptionType.OTHER;

  private int aqiThreshold = 100;

  private boolean active = true;

  public Subscription() {}

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Area getArea() {
    return area;
  }

  public void setArea(Area area) {
    this.area = area;
  }

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route route) {
    this.route = route;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAqiThreshold() {
    return aqiThreshold;
  }

  public void setAqiThreshold(int aqiThreshold) {
    this.aqiThreshold = aqiThreshold;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public SubscriptionType getType() {
    // rows saved before this column existed load as null
    return type != null ? type : SubscriptionType.OTHER;
  }

  public void setType(SubscriptionType type) {
    if (type != null) {
      this.type = type;
    }
  }
}
