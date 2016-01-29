/**
 * Copyright (C) 2013-2016 shadowhunt (dev@shadowhunt.de)
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

import java.util.Date;

/**
 * {@link Log} holds all log information for a single {@link Revision} of a {@link Resource}.
 */
public interface Log {

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
