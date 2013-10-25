package de.shadowhunt.subversion.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;

public class ListLoader extends BaseLoader {

	public static List<Info> load(final Resource resource, final Revision revision, final Depth depth, final boolean withCustomProperties) throws Exception {
		final File root = new File(ROOT, resolve(revision) + resource.getValue());

		final Set<Info> result = new TreeSet<Info>(Info.RESOURCE_COMPARATOR);
		result.add(InfoLoader.load(resource, revision, withCustomProperties));

		if (root.isDirectory()) {
			Depth childDepth = Depth.INFINITY;
			switch (depth) {
				case EMPTY:
					// nothing more to do
					break;
				case FILES:
					// $FALL-THROUGH$
				case IMMEDIATES:
					childDepth = Depth.EMPTY;
					//$FALL-THROUGH$
				case INFINITY:
					result.add(InfoLoader.load(resource, revision, withCustomProperties));
					for (final File child : root.listFiles()) {
						final Resource childResource = resource.append(Resource.create(child.getName()));
						final List<Info> childInfo = load(childResource, revision, childDepth, withCustomProperties);
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
					break;
			}
		}
		return new ArrayList<Info>(result);
	}
}
