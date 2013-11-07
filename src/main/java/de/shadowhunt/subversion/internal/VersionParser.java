package de.shadowhunt.subversion.internal;

import java.util.regex.Pattern;

import de.shadowhunt.subversion.Revision;

class VersionParser {

	private static final Pattern PATH_PATTERN = Pattern.compile("/");

	private final int prefix;

	VersionParser(final String prefix) {
		this.prefix = PATH_PATTERN.split(prefix).length;
	}

	public Revision getRevisionFromPath(final String path) {
		final String[] parts = PATH_PATTERN.split(path);
		final int revision = Integer.parseInt(parts[prefix + 2]); // prefix + $svn + bc/vrv + VERSION);
		return Revision.create(revision);
	}
}
