/**
 * Copyright (C) 2013-2017 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        return new QualifiedResource(base, resource.append(suffix.base).append(suffix.resource));
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
        QualifiedResource other = (QualifiedResource) obj;
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
        return new QualifiedResource(base, resource.getParent());
    }

    public Resource getResource() {
        return resource;
    }

    public String getValue() {
        return base.append(resource).getValue();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((base == null) ? 0 : base.hashCode());
        result = prime * result + ((resource == null) ? 0 : resource.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return base.append(resource).toString();
    }
}
