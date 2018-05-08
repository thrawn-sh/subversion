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
