/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
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

import de.shadowhunt.subversion.ResourceProperty;

public final class XmlConstants {

    public static final String CUSTOM_PROPERTIES_NAMESPACE = ResourceProperty.Type.CUSTOM.getPrefix();

    public static final String CUSTOM_PROPERTIES_PREFIX = "C";

    public static final String DAV_NAMESPACE = "DAV:";

    public static final String DAV_PREFIX = "D";

    public static final String ENCODING = "UTF-8";

    public static final String SVN_DAV_NAMESPACE = "http://subversion.tigris.org/xmlns/dav/";

    public static final String SVN_DAV_PREFIX = "V";

    public static final String SVN_NAMESPACE = "svn:";

    public static final String SVN_PREFIX = "S";

    public static final String SVN_PROPERTIES_NAMESPACE = ResourceProperty.Type.SVN.getPrefix();

    public static final String SVN_PROPERTIES_PREFIX = "S";

    public static final String VERSION_1_0 = "1.0";

    private XmlConstants() {
        // prevent instantiation
    }
}
