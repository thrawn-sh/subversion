/**
 * Copyright (C) 2013-2015 shadowhunt (dev@shadowhunt.de)
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

import de.shadowhunt.subversion.Revision;

abstract class AbstractBaseLoader {

    protected static Revision resolvedHeadRevision;

    protected final File root;

    protected AbstractBaseLoader(final File root) {
        this.root = root;
    }

    protected Revision resolve(final Revision revision) {
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

    private Revision resolveHead() {
        Revision revision = Revision.EMPTY;
        for (final File child : root.listFiles()) {
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
