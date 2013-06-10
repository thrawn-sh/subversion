package de.shadowhunt.subversion;

import org.apache.commons.lang3.StringUtils;

/**
 * {@link Path} defines a resource location in the repository
 */
public final class Path implements Comparable<Path> {

	/**
	 * Represents the base {@link Path} in the repository
	 */
	public static final Path ROOT = new Path("/");

	/**
	 * Create a new {@link Path} instance for the given value
	 * @param path value of the {@link Path}
	 * @return the new {@link Path} instance with the given value
	 */
	public static Path create(final String path) {
		if (StringUtils.isEmpty(path) || "/".equals(path)) {
			return ROOT;
		}

		final StringBuilder sb = new StringBuilder();
		for (final String segment : path.split("/")) {
			if (!StringUtils.isEmpty(segment)) {
				sb.append('/');
				sb.append(segment);
			}
		}

		return new Path(sb.toString());
	}

	private final String value;

	private Path(final String value) {
		this.value = value;
	}

	@Override
	public int compareTo(final Path o) {
		return value.compareTo(o.value);
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
		final Path other = (Path) obj;
		return value.equals(other.value);
	}

	/**
	 * Returns the parent {@link Path} of the {@link Path}, the parent of the ROOT element is the ROOT itself
	 * @return the parent {@link Path} of the {@link Path}
	 */
	public Path getParent() {
		if (ROOT == this) {
			return ROOT; // parent of root is root
		}
		final int indexOf = value.lastIndexOf('/');
		return new Path(value.substring(0, indexOf));
	}

	/**
	 * Returns a {@link String} representation of the {@link Path}
	 * @return the {@link String} representation of the {@link Path}
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns a {@link String} representation of the {@link Path} without the leading slash
	 * @return the {@link String} representation of the {@link Path} without the leading slash
	 */
	public String getValueWithoutLeadingSeparator() {
		return value.substring(1);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return value;
	}
}
