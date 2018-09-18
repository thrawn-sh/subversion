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
package de.shadowhunt.subversion.internal;

import de.shadowhunt.subversion.Resource;

public final class QualifiedResource {

    private final Resource base;

    private final Resource resource;

    public QualifiedResource(final Resource resource) {
        this(Resource.ROOT, resource);
    }

    public QualifiedResource(final Resource base, final Resource resource) {
        this.base = base;
        this.resource = resource;
    }

    public QualifiedResource append(final QualifiedResource suffix) {
        Resource newResource = resource.append(suffix.base);
        newResource = newResource.append(suffix.resource);
        return new QualifiedResource(base, newResource);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof QualifiedResource)) {
            return false;
        }
        final QualifiedResource other = (QualifiedResource) obj;
        if (base == null) {
            if (other.base != null) {
                return false;
            }
        } else if (!base.equals(other.base)) {
            return false;
        }
        if (resource == null) {
            if (other.resource != null) {
                return false;
            }
        } else if (!resource.equals(other.resource)) {
            return false;
        }
        return true;
    }

    public Resource getBase() {
        return base;
    }

    public QualifiedResource getParent() {
        final Resource parent = resource.getParent();
        return new QualifiedResource(base, parent);
    }

    public Resource getResource() {
        return resource;
    }

    public String getValue() {
        final Resource fullResource = base.append(resource);
        return fullResource.getValue();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((base == null) ? 0 : base.hashCode());
        result = (prime * result) + ((resource == null) ? 0 : resource.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
