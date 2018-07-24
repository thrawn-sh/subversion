/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

public final class ListLoader extends AbstractBaseLoader {

    private static final FilenameFilter NO_META = (dir, name) -> {
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
    };

    private final InfoLoader infoLoader;

    ListLoader(final File root, final Resource base) {
        super(root, base);
        infoLoader = new InfoLoader(root, base);
    }

    public Set<Info> load(final Resource resource, final Revision revision, final Depth depth) throws Exception {
        final File base = new File(root, resolve(revision) + this.base.getValue() + resource.getValue());

        final Set<Info> result = new TreeSet<>(Info.RESOURCE_COMPARATOR);
        result.add(infoLoader.load(resource, revision));

        if ((depth != Depth.EMPTY) && base.isDirectory()) {
            final Depth childDepth = (depth == Depth.INFINITY) ? depth : Depth.EMPTY;

            for (final File child : base.listFiles(NO_META)) {
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
