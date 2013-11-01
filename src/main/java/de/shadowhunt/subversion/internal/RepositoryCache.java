package de.shadowhunt.subversion.internal;

import java.util.Collection;
import java.util.HashMap;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
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
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Key)) {
				return false;
			}

			Key key = (Key) o;

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
			result = 31 * result + revision.hashCode();
			return result;
		}
	}

	private final HashMap<Key, Info> cache = new HashMap<Key, Info>();

	private Revision headRevision = null;

	protected final AbstractBaseRepository repository;

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

	public final Info get(final Resource resource, final Revision revision) {
		Revision concreteRevision = revision;
		if (Revision.HEAD.equals(revision)) {
			concreteRevision = getConcreteRevision(revision);
		}
		return cache.get(new Key(resource, concreteRevision));
	}

	public final Revision getConcreteRevision(final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			if (headRevision == null) {
				final Resource resolved = repository.resolve(this, Resource.ROOT, revision, false, true);
				final InfoOperation operation = new InfoOperation(repository.getBaseUri(), resolved);
				final Info info = operation.execute(repository.client, repository.context);
				headRevision = info.getRevision();
				put(info);
			}
			return headRevision;
		}
		return revision;
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

	public Status status(@SuppressWarnings("unused") final Resource resource) {
		// no transaction => no "hidden" resources
		return null;
	}
}
