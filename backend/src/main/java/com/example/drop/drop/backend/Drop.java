package com.example.drop.drop.backend;

import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class Drop {
    @Id
    Long id;

    private GeoPt location;
    private String caption;
    private long createdOnUTCSeconds;

    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(GeoPt location) {
        this.location = location;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public long getCreatedOnUTCSeconds() {
        return createdOnUTCSeconds;
    }

    public void setCreatedOnUTCSeconds(long createdOnUTCSeconds) {
        this.createdOnUTCSeconds = createdOnUTCSeconds;
    }
}