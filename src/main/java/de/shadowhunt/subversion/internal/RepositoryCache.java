/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction.Status;

public class RepositoryCache {

    private static final class Key {

        private final Resource resource;

        private final Revision revision;

        Key(final Resource resource, final Revision revision) {
            this.resource = resource;
            this.revision = revision;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key)) {
                return false;
            }

            final Key key = (Key) o;

            if (!resource.equals(key.resource)) {
                return false;
            }
            if (!revision.equals(key.revision)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = resource.hashCode();
            result = (31 * result) + revision.hashCode();
            return result;
        }
    }

    private final Map<Key, Info> cache = new HashMap<Key, Info>();

    private Revision headRevision = null;

    private AbstractBaseRepository repository = null;

    public RepositoryCache() {
        // nothing to do
    }

    public RepositoryCache(final AbstractBaseRepository repository) {
        this.repository = repository;
    }

    public final void clear() {
        cache.clear();
        headRevision = null;
    }

    public final boolean contains(final Resource resource, final Revision revision) {
        return get(resource, revision) != null;
    }

    private Revision determineHeadRevision() {
        if (headRevision != null) {
            return headRevision;
        }

        final Resource resolved = repository.resolve(this, Resource.ROOT, Revision.HEAD, false);
        if (resolved == null) {
            throw new SubversionException("Can't resolve: " + Resource.ROOT + '@' + Revision.HEAD);
        }

        final Resource marker = repository.config.getPrefix();
        final InfoOperation operation = new InfoOperation(repository.getBaseUri(), resolved, marker);
        final Info info = operation.execute(repository.client, repository.context);
        headRevision = info.getRevision();
        put(info);
        return headRevision;
    }

    @CheckForNull
    public final Info get(final Resource resource, final Revision revision) {
        Revision concreteRevision = revision;
        if (Revision.HEAD.equals(revision)) {
            concreteRevision = getConcreteRevision(revision);
        }
        return cache.get(new Key(resource, concreteRevision));
    }

    public final Revision getConcreteRevision(final Revision revision) {
        if (Revision.HEAD.equals(revision)) {
            return determineHeadRevision();
        }
        return revision;
    }

    public AbstractBaseRepository getRepository() {
        return repository;
    }

    public final boolean isEmpty() {
        return cache.isEmpty();
    }

    public final void put(final Info info) {
        cache.put(new Key(info.getResource(), info.getRevision()), info);
    }

    public final void putAll(final Collection<Info> collection) {
        for (final Info info : collection) {
            put(info);
        }
    }

    public void setRepository(final AbstractBaseRepository repository) {
        this.repository = repository;
    }

    @CheckForNull
    public Status status(@SuppressWarnings("unused") final Resource resource) {
        // no transaction => no "hidden" resources
        return null;
    }
}
