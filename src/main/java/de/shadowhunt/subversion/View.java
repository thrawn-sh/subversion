/**
 * Copyright © 2013-2017 shadowhunt (dev@shadowhunt.de)
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
package de.shadowhunt.subversion;

import java.util.UUID;

/**
 * {@link View} allows the application to define a view on the repository, it can be created via {@link Repository#createView()}.
 *
 * A {@link View} will freeze the maximal (newest) {@link de.shadowhunt.subversion.Revision} the {@link Repository} can be accessed with, resulting in a uniform view when using the {@link de.shadowhunt.subversion.Revision#HEAD}
 */
public interface View {

    /**
     * Returns the maximal (newest) {@link de.shadowhunt.subversion.Revision} supported by this {@link View}.
     *
     * @return the maximal (newest) {@link de.shadowhunt.subversion.Revision} supported by this {@link View}
     */
    Revision getHeadRevision();

    /**
     * Returns the {@link UUID} of the {@link Repository} this {@link View} belongs to.
     *
     * @return the {@link UUID} of the {@link Repository}
     */
    UUID getRepositoryId();
}
