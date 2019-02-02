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

import org.junit.Assert;
import org.junit.Test;

public class InfoCommandIT extends AbstractCommandIT {

    public InfoCommandIT() {
        super(new InfoCommand());
    }

    @Test
    public void fileTest() throws Exception {
        final String resource = "--resource=/trunk/00000000-0000-0000-0000-000000000000/info/file.txt";
        final String version = "--version=HEAD";
        final boolean success = command.call(TEST_OUT, TEST_ERR, BASE, resource, USERNAME, PASSWORD, TRUST_SSL, version);
        Assert.assertTrue("command must succeed", success);
    }

    @Test
    public void folderTest() throws Exception {
        final String resource = "--resource=/trunk/00000000-0000-0000-0000-000000000000/info/folder";
        final String version = "--version=HEAD";
        final boolean success = command.call(TEST_OUT, TEST_ERR, BASE, resource, USERNAME, PASSWORD, TRUST_SSL, version);
        Assert.assertTrue("command must succeed", success);
    }

    @Test
    public void lockedFileTest() throws Exception {
        final String resource = "--resource=/trunk/00000000-0000-0000-0000-000000000000/lock/file.txt";
        final String version = "--version=HEAD";
        final boolean success = command.call(TEST_OUT, TEST_ERR, BASE, resource, USERNAME, PASSWORD, TRUST_SSL, version);
        Assert.assertTrue("command must succeed", success);
    }
}
