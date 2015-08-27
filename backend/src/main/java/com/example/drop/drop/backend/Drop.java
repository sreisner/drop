package com.example.drop.drop.backend;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity()
public class Drop {
    @Id
    public Long id;

    private GeoPt location;
    private String caption;
    private long createdOnUTCSeconds;
    private BlobKey imageKey;
    private String imageUploadUrl;

    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(float latitude, float longitude) {
        this.location = new GeoPt(latitude, longitude);
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

    public BlobKey getImageKey() {
        return imageKey;
    }

    public void setImageKey(BlobKey imageKey) {
        this.imageKey = imageKey;
    }
}