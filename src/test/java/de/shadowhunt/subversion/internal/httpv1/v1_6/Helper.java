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
package de.shadowhunt.subversion.internal.httpv1.v1_6;

import java.io.File;

import de.shadowhunt.subversion.internal.AbstractHelper;

final class Helper extends AbstractHelper {

    private static final File BASE = new File("src/test/resources/dump/v1_6");

    private static final String HOST = System.getProperty("subversion.1_6.host", "127.0.0.1");

    private static final String PROTOCOL = System.getProperty("subversion.1_6.protocol", "http");

    Helper() {
        super(BASE, PROTOCOL, HOST);
    }
}
