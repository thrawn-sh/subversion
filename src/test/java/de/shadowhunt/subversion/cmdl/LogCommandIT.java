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

import org.junit.Assert;
import org.junit.Test;

public class LogCommandIT extends AbstractCommandIT {

    public LogCommandIT() {
        super(new LogCommand());
    }

    @Test
    public void fileTest() throws Exception {
        final String resource = "--resource=/trunk/00000000-0000-0000-0000-000000000000/log/file.txt";
        final String start = "--start=HEAD";
        final String stop = "--stop=0";
        final String[] arguments = filterArguments(BASE, resource, USERNAME, PASSWORD, TRUST_SSL, start, stop);
        final boolean success = command.call(TEST_OUT, TEST_ERR, arguments);
        Assert.assertTrue("command must succeed", success);
    }

    @Test
    public void folderTest() throws Exception {
        final String resource = "--resource=/trunk/00000000-0000-0000-0000-000000000000/log";
        final String start = "--start=HEAD";
        final String stop = "--stop=0";
        final String[] arguments = filterArguments(BASE, resource, USERNAME, PASSWORD, TRUST_SSL, start, stop);
        final boolean success = command.call(TEST_OUT, TEST_ERR, arguments);
        Assert.assertTrue("command must succeed", success);
    }
}
