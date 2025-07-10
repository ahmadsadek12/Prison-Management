package org.example.models;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Embeddable
public class Location {
    private String state;
    private String city;
    private String street;
    private String country;

    public Location() {}

    public Location(String state, String city, String street, String country) {
        this.state = state;
        this.city = city;
        this.street = street;
        this.country = country;
    }

}
