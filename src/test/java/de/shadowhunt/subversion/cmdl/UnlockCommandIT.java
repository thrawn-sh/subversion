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

import java.util.UUID;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UnlockCommandIT extends AbstractCommandIT {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final UUID testId = UUID.randomUUID();

    public UnlockCommandIT() {
        super(new UnlockCommand());
    }

    private void prepare(final String resource) throws Exception {
        upload(folder, resource);
        final String steal = "--steal-lock";
        final boolean success = command.call(TEST_OUT, TEST_ERR, BASE, resource, USERNAME, PASSWORD, TRUST_SSL, steal);
        Assert.assertTrue("command must succeed", success);
    }

    @Test
    public void test() throws Exception {
        final String resource = "--resource=/trunk/" + testId + "/lock/file.txt";
        prepare(resource);
        final String steal = "--steal-lock";
        final boolean success = command.call(TEST_OUT, TEST_ERR, BASE, resource, USERNAME, PASSWORD, TRUST_SSL, steal);
        Assert.assertTrue("command must succeed", success);
    }
}
