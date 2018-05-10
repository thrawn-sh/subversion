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
package de.shadowhunt.subversion.cmdl;

import java.util.regex.Pattern;

import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Type;
import joptsimple.ValueConverter;

public class ResourcePropertyConverter implements ValueConverter<ResourceProperty> {

    @Override
    public ResourceProperty convert(final String value) {
        final String regex = Pattern.quote("|");
        final String[] parts = value.split(regex);
        if (parts.length != 2) {
            return null;
        }
        return new ResourceProperty(Type.SUBVERSION_CUSTOM, parts[0], parts[1]);
    }

    @Override
    public String valuePattern() {
        return "<NAME>|<VALUE>";
    }

    @Override
    public Class<? extends ResourceProperty> valueType() {
        return ResourceProperty.class;
    }

}
