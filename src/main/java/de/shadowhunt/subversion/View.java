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
package de.shadowhunt.subversion;

import java.util.UUID;

import javax.annotation.concurrent.Immutable;

/**
 * {@link View} allows the application to define a view on the repository, it can be created via {@link Repository#createView()}.
 *
 * A {@link View} will freeze the maximal (newest) {@link Revision} the {@link Repository} can be accessed with, resulting in a uniform view when using the {@link Revision#HEAD}
 */
@Immutable
public interface View {

    /**
     * Returns the maximal (newest) {@link Revision} supported by this {@link View}.
     *
     * @return the maximal (newest) {@link Revision} supported by this {@link View}
     */
    Revision getHeadRevision();

    /**
     * Returns the {@link UUID} of the {@link Repository} this {@link View} belongs to.
     *
     * @return the {@link UUID} of the {@link Repository}
     */
    UUID getRepositoryId();
}
