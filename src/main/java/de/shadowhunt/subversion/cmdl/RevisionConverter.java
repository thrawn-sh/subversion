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
package de.shadowhunt.subversion.cmdl;

import de.shadowhunt.subversion.Revision;
import joptsimple.ValueConverter;

public class RevisionConverter implements ValueConverter<Revision> {

    @Override
    public Revision convert(final String value) {
        if ("HEAD".equals(value)) {
            return Revision.HEAD;
        }
        final int version = Integer.parseInt(value);
        return Revision.create(version);
    }

    @Override
    public String valuePattern() {
        return null;
    }

    @Override
    public Class<? extends Revision> valueType() {
        return Revision.class;
    }

}
