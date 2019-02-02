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
import org.junit.Test;

public class CopyCommandIT extends AbstractCommandIT {

    private final UUID testId = UUID.randomUUID();

    public CopyCommandIT() {
        super(new CopyCommand());
    }

    @Test
    public void test() throws Exception {
        final String source = "--source=/trunk/00000000-0000-0000-0000-000000000000/copy/file.txt";
        final String target = "--target=/trunk/" + testId + "/copy";
        final String parents = "--parents";
        final String message = "--message=test";
        final String revision = "--version=HEAD";
        final boolean success = command.call(TEST_OUT, TEST_ERR, BASE, source, target, USERNAME, PASSWORD, TRUST_SSL, parents, message, revision);
        Assert.assertTrue("command must succeed", success);
    }
}
