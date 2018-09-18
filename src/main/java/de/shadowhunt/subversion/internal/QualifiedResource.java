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

    private final Resource suffix;

    public QualifiedResource(final Resource base, final Resource suffix) {
        this.base = base;
        this.suffix = suffix;
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
        if (suffix == null) {
            if (other.suffix != null) {
                return false;
            }
        } else if (!suffix.equals(other.suffix)) {
            return false;
        }
        return true;
    }

    public Resource getBase() {
        return base;
    }

    public Resource getSuffix() {
        return suffix;
    }

    public String getValue() {
        final Resource fullResource = toResource();
        return fullResource.getValue();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((base == null) ? 0 : base.hashCode());
        result = (prime * result) + ((suffix == null) ? 0 : suffix.hashCode());
        return result;
    }

    public Resource toResource() {
        return base.append(suffix);
    }

    @Override
    public String toString() {
        return getValue();
    }
}
