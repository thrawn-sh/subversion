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

import java.util.UUID;

import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.View;
import org.apache.commons.lang3.Validate;

final class ViewImpl implements View {

    private final Revision headRevision;

    private final UUID repositoryId;

    ViewImpl(final UUID repositoryId, final Revision headRevision) {
        Validate.notNull(repositoryId, "repositoryId must not be null");
        Validate.notNull(headRevision, "headRevision must not be null");

        this.repositoryId = repositoryId;
        this.headRevision = headRevision;
    }

    @Override
    public Revision getHeadRevision() {
        return headRevision;
    }

    @Override
    public UUID getRepositoryId() {
        return repositoryId;
    }
}
