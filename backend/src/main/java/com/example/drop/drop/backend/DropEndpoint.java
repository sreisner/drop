package com.example.drop.drop.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.GeoPt;

import java.util.Date;
import java.util.List;

import static com.example.drop.drop.backend.OfyService.ofy;

@Api(
        name = "dropApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "drop-web-service.appspot.com",
                ownerName = "drop-web-service.appspot.com",
                packagePath="")
)
public class DropEndpoint {

    @ApiMethod(name = "create")
    public Drop create(Drop toCreate) {
        toCreate.setCreatedOnUTCSeconds(new Date().getTime());
        ofy().save().entity(toCreate).now();
        return toCreate;
    }

    @ApiMethod(name = "getDropsInRectangle")
    public List<Drop> getDropsInRectangle() {
        return ofy().load().type(Drop.class).list();
    }

}
