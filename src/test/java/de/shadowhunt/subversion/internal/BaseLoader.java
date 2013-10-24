package de.shadowhunt.subversion.internal;

import java.io.File;

import de.shadowhunt.subversion.Revision;

abstract class BaseLoader {

	protected static final File ROOT = new File("/home/ade/dump");

	protected static Revision resolvedHeadRevision;

	protected static Revision resolve(final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			synchronized (revision) {
				if (resolvedHeadRevision == null) {
					resolvedHeadRevision = resolveHead();
				}
				return resolvedHeadRevision;
			}
		}
		return revision;
	}

	private static Revision resolveHead() {
		Revision revision = Revision.HEAD;
		for (final File child : ROOT.listFiles()) {
			if (child.isDirectory()) {
				final String name = child.getName();
				final Revision current = Revision.create(Integer.parseInt(name));
				if (revision.compareTo(current) < 0) {
					revision = current;
				}
			}
		}
		return revision;
	}
}
