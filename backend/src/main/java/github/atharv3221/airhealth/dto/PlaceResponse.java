package github.atharv3221.airhealth.dto;

// admin1 is the state/region; any field except the coordinates can be null
public record PlaceResponse(String name, double latitude, double longitude,
        String admin1, String country) {}
