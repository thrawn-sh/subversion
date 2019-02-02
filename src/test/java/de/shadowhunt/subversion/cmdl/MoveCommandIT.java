/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
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
package de.shadowhunt.subversion.cmdl;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MoveCommandIT extends AbstractCommandIT {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final UUID testId = UUID.randomUUID();

    public MoveCommandIT() {
        super(new MoveCommand());
    }

    @Test
    public void test() throws Exception {
        final String aa = "/trunk/" + testId + "/move/file.txt";
        final String resource = "--resource=" + aa;
        upload(folder, resource);
        final String source = "--source=" + aa;
        final String target = "--target=/trunk/" + testId + "/move/file2.txt";
        final String parents = "--parents";
        final String message = "--message=test";
        final boolean success = command.call(TEST_OUT, TEST_ERR, BASE, source, target, USERNAME, PASSWORD, TRUST_SSL, message, parents);
        Assert.assertTrue("command must succeed", success);
    }
}
