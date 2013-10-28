package de.shadowhunt.subversion.internal;

import java.util.Collection;
import java.util.HashMap;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;

public class RepositoryCache {

	private final HashMap<Key, Info> cache = new HashMap<Key, Info>();

	protected final AbstractBasicRepository repository;

	public RepositoryCache(AbstractBasicRepository repository) {
		this.repository = repository;
	}

	private Revision headRevision = null;

	private static final class Key {
		private final Resource resource;
		private final Revision revision;

		private Key(Resource resource, Revision revision) {
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

		private Resource getResource() {

			return resource;
		}

		private Revision getRevision() {
			return revision;
		}
	}

	public final  boolean isEmpty() {
		return cache.isEmpty();
	}

	public final Info get(final Resource resource, Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			revision = getConcreteRevision(revision);
		}
		return cache.get(new Key(resource, revision));
	}

	public final boolean contains(Resource resource, Revision revision) {
		return get(resource, revision) != null;
	}

	public final void put(final Info info) {
		cache.put(new Key(info.getResource(), info.getRevision()), info);
	}

	public final void putAll(final Collection<Info> collection) {
		for (final Info info : collection) {
			put(info);
		}
	}

	public final void clear() {
		cache.clear();
		headRevision = null;
	}

	public final Revision getConcreteRevision(final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			if (headRevision == null) {
				final Resource resolved = repository.resolve(this, Resource.ROOT, revision, false);
				final InfoOperation operation = new InfoOperation(repository.getBaseUri(), resolved, Depth.EMPTY);
				Info info = operation.execute(repository.client, repository.context);
				headRevision = info.getRevision();
				put(info);
			}
			return headRevision;
		}
		return revision;
	}
}
