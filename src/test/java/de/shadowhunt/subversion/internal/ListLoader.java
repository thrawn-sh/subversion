package de.shadowhunt.subversion.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;

public final class ListLoader extends BaseLoader {

	private static final FilenameFilter NO_META = new FilenameFilter() {

		@Override
		public boolean accept(final File dir, final String name) {
			if (name.endsWith(InfoLoader.SUFFIX)) {
				return false;
			}
			if (name.endsWith(LogLoader.SUFFIX)) {
				return false;
			}
			if (name.endsWith(ResourcePropertyLoader.SUFFIX)) {
				return false;
			}
			return true;
		}
	};

	private ListLoader() {
		// prevent instantiation
	}

	public static Set<Info> load(final Resource resource, final Revision revision, final Depth depth) throws Exception {
		final File root = new File(ROOT, resolve(revision) + resource.getValue());

		final Set<Info> result = new TreeSet<Info>(Info.RESOURCE_COMPARATOR);
		result.add(InfoLoader.load(resource, revision));

		if ((depth != Depth.EMPTY) && root.isDirectory()) {
			final Depth childDepth = (depth == Depth.INFINITY) ? depth : Depth.EMPTY;

			for (final File child : root.listFiles(NO_META)) {
				final Resource childResource = resource.append(Resource.create(child.getName()));
				final Set<Info> childInfo = load(childResource, revision, childDepth);

				if (Depth.FILES == depth) {
					final Iterator<Info> it = childInfo.iterator();
					while (it.hasNext()) {
						final Info info = it.next();
						if (info.isDirectory()) {
							it.remove();
						}
					}
				}

				result.addAll(childInfo);
			}
		}

		return result;
	}
}
