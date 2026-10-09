package github.atharv3221.airhealth.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "routes")
public class Route {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String sourceName;
  private double sourceLatitude;
  private double sourceLongitude;

  private String destinationName;
  private double destinationLatitude;
  private double destinationLongitude;

  // areas along the route, in order
  @ManyToMany
  @JoinTable(
      name = "route_areas",
      joinColumns = @JoinColumn(name = "route_id"),
      inverseJoinColumns = @JoinColumn(name = "area_id")
  )
  @OrderColumn(name = "position")
  private List<Area> areas = new ArrayList<>();

  public Route() {}

  public double getDestinationLatitude() {
    return destinationLatitude;
  }

  public void setDestinationLatitude(double destinationLatitude) {
    this.destinationLatitude = destinationLatitude;
  }

  public List<Area> getAreas() {
    return areas;
  }

  public void setAreas(List<Area> areas) {
    this.areas = areas;
  }

  public double getDestinationLongitude() {
    return destinationLongitude;
  }

  public void setDestinationLongitude(double destinationLongitude) {
    this.destinationLongitude = destinationLongitude;
  }

  public String getDestinationName() {
    return destinationName;
  }

  public void setDestinationName(String destinationName) {
    this.destinationName = destinationName;
  }

  public double getSourceLongitude() {
    return sourceLongitude;
  }

  public void setSourceLongitude(double sourceLongitude) {
    this.sourceLongitude = sourceLongitude;
  }

  public double getSourceLatitude() {
    return sourceLatitude;
  }

  public void setSourceLatitude(double sourceLatitude) {
    this.sourceLatitude = sourceLatitude;
  }

  public String getSourceName() {
    return sourceName;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
