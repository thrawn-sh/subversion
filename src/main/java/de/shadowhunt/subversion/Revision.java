package de.shadowhunt.subversion;

import java.io.Serializable;

/**
 * {code Revision} defines the revision of a repository or a resource in that repository
 */
public final class Revision implements Comparable<Revision>, Serializable {

	/**
	 * Represents the newest {@link Revision} in the repository
	 */
	public static final Revision HEAD = new Revision(-1);

	/**
	 * Represents the first {@link Revision} in the repository
	 */
	public static final Revision INITIAL = new Revision(0);

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new {@link Revision} instance for the given value
	 * @param revision value of the {@link Revision} must be greater than 0
	 * @throws IllegalArgumentException if revision is smaller or equal 0
	 * @return the new {@link Revision} instance with the given value
	 */
	public static Revision create(final int revision) {
		if (revision <= 0) {
			throw new IllegalArgumentException("revision must be greater than 0, was " + revision);
		}
		return new Revision(revision);
	}

	private final int version;

	private Revision(final int revision) {
		version = revision;
	}

	@Override
	public int compareTo(final Revision o) {
		return version - o.version;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Revision other = (Revision) obj;
		return version == other.version;
	}

	@Override
	public int hashCode() {
		return version;
	}

	@Override
	public String toString() {
		return Integer.toString(version);
	}
}
