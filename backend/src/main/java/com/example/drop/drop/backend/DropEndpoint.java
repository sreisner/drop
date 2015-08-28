package com.example.drop.drop.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.repackaged.com.google.api.client.util.IOUtils;
import com.googlecode.objectify.cmd.Query;

import org.apache.geronimo.mail.util.StringBufferOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Api(
        canonicalName = "Drop API",
        title = "Drop API",
        description = "Drop App API",
        name = "dropApi",
        version = "v1",
        resource = "drop",
        namespace = @ApiNamespace(
                ownerDomain = "backend.drop.drop.example.com",
                ownerName = "backend.drop.drop.example.com",
                packagePath = ""
        )
)
public class DropEndpoint {

    private static final Logger logger = Logger.getLogger(DropEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    @ApiMethod(
            name = "get",
            path = "drop/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Drop get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting Drop with ID: " + id);
        Drop drop = ofy().load().type(Drop.class).id(id).now();
        if (drop == null) {
            throw new NotFoundException("Could not find Drop with ID: " + id);
        }
        return drop;
    }

    @ApiMethod(
            name = "insert",
            path = "drop",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Drop insert(Drop drop) {
        ofy().save().entity(drop).now();
        logger.info("Created Drop.");

        return ofy().load().entity(drop).now();
    }

    @ApiMethod(
            name = "createUploadUrl",
            path = "drop/image",
            httpMethod = ApiMethod.HttpMethod.GET)
    public StringResource createUploadUrl() {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        String uploadUrlString = blobstoreService.createUploadUrl("/");

        StringResource response = new StringResource();
        response.setData(uploadUrlString);
        return response;
    }

    /**
     * Updates an existing {@code Drop}.
     *
     * @param id   the ID of the entity to be updated
     * @param drop the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Drop}
     */
    @ApiMethod(
            name = "update",
            path = "drop/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Drop update(@Named("id") Long id, Drop drop) throws NotFoundException {
        checkExists(id);
        ofy().save().entity(drop).now();
        logger.info("Updated Drop: " + drop);
        return ofy().load().entity(drop).now();
    }

    /**
     * Deletes the specified {@code Drop}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Drop}
     */
    @ApiMethod(
            name = "remove",
            path = "drop/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(Drop.class).id(id).now();
        logger.info("Deleted Drop with ID: " + id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "list",
            path = "drop",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Drop> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Drop> query = ofy().load().type(Drop.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Drop> queryIterator = query.iterator();
        List<Drop> dropList = new ArrayList<Drop>(limit);
        while (queryIterator.hasNext()) {
            dropList.add(queryIterator.next());
        }
        return CollectionResponse.<Drop>builder().setItems(dropList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(Drop.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Drop with ID: " + id);
        }
    }
}