/**
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
