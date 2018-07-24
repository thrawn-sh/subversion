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

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;

abstract class AbstractBaseLoader {

    protected static Revision resolvedHeadRevision;

    protected final Resource base;

    protected final File root;

    protected AbstractBaseLoader(final File root, final Resource base) {
        this.root = root;
        this.base = base;
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
