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
package de.shadowhunt.subversion.internal.httpv2.v1_7;

import java.io.File;
import java.net.URI;
import java.util.UUID;

import de.shadowhunt.subversion.internal.AbstractHelper;

final class Helper extends AbstractHelper {

    private static final File BASE = new File("src/test/resources/dump/v1_7");

    private static final URI DUMP_URI = URI.create("http://subversion.vm.shadowhunt.de/1.7.0/dump.zip");

    private static final URI REPOSITORY_URI = URI.create("http://subversion.vm.shadowhunt.de/1.7.0/svn-basic/test");

    private static final UUID TEST_ID = UUID.randomUUID();

    Helper() {
        super(BASE, DUMP_URI, REPOSITORY_URI, TEST_ID);
    }
}
