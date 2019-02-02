/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
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
package de.shadowhunt.subversion.internal.httpv1.v1_3;

import de.shadowhunt.subversion.internal.AbstractRepositoryPropertiesSetIT;

public class RepositoryPropertiesSetIT extends AbstractRepositoryPropertiesSetIT {

    private static final Helper HELPER = new Helper();

    public RepositoryPropertiesSetIT() {
        super(HELPER.getRepositoryA(), HELPER.getTestId());
    }
}
