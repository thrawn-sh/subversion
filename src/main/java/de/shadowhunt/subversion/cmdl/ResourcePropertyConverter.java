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
package de.shadowhunt.subversion.cmdl;

import java.util.regex.Pattern;

import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Type;
import joptsimple.ValueConverter;

public class ResourcePropertyConverter implements ValueConverter<ResourceProperty> {

    private final boolean onlyName;

    public ResourcePropertyConverter(final boolean onlyName) {
        this.onlyName = onlyName;
    }

    @Override
    public ResourceProperty convert(final String value) {
        final String regex = Pattern.quote("|");
        final String[] parts = value.split(regex);
        if (onlyName) {
            return new ResourceProperty(Type.SUBVERSION_CUSTOM, parts[0], "");
        }
        return new ResourceProperty(Type.SUBVERSION_CUSTOM, parts[0], parts[1]);
    }

    @Override
    public String valuePattern() {
        return null;
    }

    @Override
    public Class<? extends ResourceProperty> valueType() {
        return ResourceProperty.class;
    }

}
