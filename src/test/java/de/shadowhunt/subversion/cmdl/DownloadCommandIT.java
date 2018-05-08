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

import java.io.File;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DownloadCommandIT extends AbstractCommandIT {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public DownloadCommandIT() {
        super(new DownloadCommand());
    }

    @Test
    public void fileTest() throws Exception {
        final String resource = "--resource=/trunk/00000000-0000-0000-0000-000000000000/download/file.txt";
        final String version = "--version=HEAD";
        final File file = folder.newFile();
        final String output = "--output=" + file.getAbsolutePath();
        final String[] arguments = filterArguments(BASE, resource, USERNAME, PASSWORD, TRUST_SSL, version, output);
        final boolean success = command.call(TEST_OUT, TEST_ERR, arguments);
        Assert.assertTrue("command must succeed", success);
    }

    @Test
    public void folderTest() throws Exception {
        final String resource = "--resource=/trunk/00000000-0000-0000-0000-000000000000/download";
        final String version = "--version=HEAD";
        final File file = folder.newFile();
        final String output = "--output=" + file.getAbsolutePath();
        final String[] arguments = filterArguments(BASE, resource, USERNAME, PASSWORD, TRUST_SSL, version, output);
        final boolean success = command.call(TEST_OUT, TEST_ERR, arguments);
        Assert.assertTrue("command must succeed", success);
    }
}
