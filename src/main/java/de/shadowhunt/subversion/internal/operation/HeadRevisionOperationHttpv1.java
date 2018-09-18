/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.subversion.internal.operation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.InfoImpl;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.ReadOnlyRepositoryInternal;
import de.shadowhunt.subversion.internal.ResourcePropertyUtils;
import de.shadowhunt.subversion.internal.jaxb.InfoParser;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

public class HeadRevisionOperationHttpv1 extends AbstractPropfindOperationHttpv1<Revision> {

    public HeadRevisionOperationHttpv1(final ReadOnlyRepositoryInternal repository, final QualifiedResource qualifiedResource) {
        super(repository, new int[] { HttpStatus.SC_MULTI_STATUS }, qualifiedResource, Depth.EMPTY, ResourcePropertyUtils.VERSION);
    }

    @Override
    // actually
    protected Revision processResponse(final HttpResponse response) throws IOException {
        try (final InputStream content = getContent(response)) {
            final Resource basePath = repository.getBasePath();
            final List<InfoImpl> infos = InfoParser.parse(content, basePath);
            assert (infos.size() == 1) : "must contain only 1 Info element";
            final InfoImpl info = infos.get(0);
            return info.getRevision();
        }
    }

}
