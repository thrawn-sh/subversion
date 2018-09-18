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

import java.util.Date;

import javax.annotation.concurrent.Immutable;

/**
 * {@link LogEntry} holds all log information for a single {@link Revision} of a {@link Resource}.
 */
@Immutable
public interface LogEntry {

    /**
     * Returns the name of the author that committed changes to the repository.
     *
     * @return the name of the author that committed changes to the repository
     */
    String getAuthor();

    /**
     * Returns the time of the commit.
     *
     * @return the time of the commit
     */
    Date getDate();

    /**
     * Returns the commit message.
     *
     * @return the commit message or an empty {@link String} if no commit message was specified
     */
    String getMessage();

    /**
     * Returns the {@link Revision} that was created by the commit.
     *
     * @return the {@link Revision} that was created by the commit
     */
    Revision getRevision();
}
