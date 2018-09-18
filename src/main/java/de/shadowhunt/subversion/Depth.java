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

/**
 * {@link Depth} defines the recursion level for the listing call {@link Repository #list(View, Resource, Revision, Depth)}.
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
