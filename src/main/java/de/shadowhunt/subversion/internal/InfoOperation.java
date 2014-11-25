package de.shadowhunt.subversion.internal;

import java.io.IOException;
import java.net.URI;

import javax.annotation.CheckForNull;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;

class InfoOperation extends PropfindOperation<Info> {

    private static final String LOCK_OWNER_HEADER = "X-SVN-Lock-Owner";

    InfoOperation(final URI repository, final Resource resource, final Resource marker, final ResourceProperty.Key[] requestedProperties) {
        super(repository, resource, marker, Depth.EMPTY, requestedProperties);
    }

    InfoOperation(final URI repository, final Resource resource, final Resource marker) {
        super(repository, resource, marker, Depth.EMPTY, PropfindOperation.ALL_PROPERTIES);
    }

    @Override
    @CheckForNull
    protected Info processResponse(final HttpResponse response) throws IOException {
        if (getStatusCode(response) == HttpStatus.SC_NOT_FOUND) {
            return null;
        }

        final Info info = InfoImplReader.read(getContent(response), repository.getPath(), marker.getValue());
        if (info.isLocked()) {
            final Header header = response.getFirstHeader(LOCK_OWNER_HEADER);
            ((InfoImpl) info).setLockOwner(header.getValue());
        }
        return info;
    }
}
