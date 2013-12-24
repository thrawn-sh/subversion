/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    private final InfoLoader infoLoader;

    ListLoader(final File root) {
        super(root);
        infoLoader = new InfoLoader(root);
    }

    public Set<Info> load(final Resource resource, final Revision revision, final Depth depth) throws Exception {
        final File base = new File(root, resolve(revision) + resource.getValue());

        final Set<Info> result = new TreeSet<Info>(Info.RESOURCE_COMPARATOR);
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
