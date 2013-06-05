package de.shadowhunt.scm.subversion;

import org.apache.commons.lang3.StringUtils;

public final class Path implements Comparable<Path> {

	public static final Path ROOT = new Path("/");

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
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	public Path getParent() {
		final int indexOf = value.indexOf('/');
		if (indexOf == 0) {
			return ROOT; // parent of root is root
		}
		return new Path(value.substring(0, indexOf));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return value;
	}
}
