/**
 * Copyright (C) 2013-2015 shadowhunt (dev@shadowhunt.de)
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
package de.shadowhunt.subversion;

/**
 * {@link Depth} defines the recursion level for the listing call {@link Repository #list(Resource, Revision, Depth)}.
 */
public enum Depth {

    /**
     * Only list the resources itself, no sub-resources.
     */
    EMPTY("0"),

    /**
     * Only list all direct file sub-resources.
     */
    FILES("1"),

    /**
     * Only list all direct sub-resources (files and directories).
     */
    IMMEDIATES("1"),

    /**
     * Recursively list all sub-resources (files and directories).
     */
    INFINITY("infinity");

    /**
     * Recursion level.
     */
    public final String value;

    Depth(final String value) {
        this.value = value;
    }
}
