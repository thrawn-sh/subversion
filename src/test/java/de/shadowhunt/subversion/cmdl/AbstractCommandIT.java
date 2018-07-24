/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
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

import java.io.File;
import java.io.PrintStream;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public abstract class AbstractCommandIT {

    public static final String BASE;

    public static final String BASE_URI;

    public static final String PASSWORD;

    public static final PrintStream TEST_ERR;

    public static final PrintStream TEST_OUT;

    public static final String TRUST_SSL;

    public static final String USERNAME;

    static {
        final String host = System.getProperty("subversion.cmdl.host", "127.0.0.1");
        final String protocol = System.getProperty("subversion.cmdl.protocol", "http");

        BASE_URI = protocol + "://" + host + "/svn-basic/test";
        BASE = "--base=" + BASE_URI;
        USERNAME = "--username=svnuser";
        PASSWORD = "--password=svnpass";
        TRUST_SSL = "--trust-ssl";

        TEST_ERR = new PrintStream(NullOutputStream.NULL_OUTPUT_STREAM);
        TEST_OUT = new PrintStream(NullOutputStream.NULL_OUTPUT_STREAM);
    }

    protected final Command command;

    protected AbstractCommandIT(final Command command) {
        this.command = command;
    }

    @Test
    public void helpTest() throws Exception {
        final boolean success = command.call(TEST_OUT, TEST_ERR, "--help");
        Assert.assertFalse("command must not succeed", success);
    }

    @Test
    public void noArgumentsTest() throws Exception {
        final boolean success = command.call(TEST_OUT, TEST_ERR);
        Assert.assertFalse("command must not succeed", success);
    }

    @Test
    public void testToString() throws Exception {
        Assert.assertNotNull("toString must not be null", command.toString());
    }

    protected void upload(final TemporaryFolder folder, final String resource) throws Exception {
        final File file = folder.newFile("input.txt");
        final String parents = "--parents";
        final String message = "--message=test";
        final String input = "--input=" + file;
        final CheckinCommand checkin = new CheckinCommand();
        final boolean success = checkin.call(TEST_OUT, TEST_ERR, BASE, resource, USERNAME, PASSWORD, TRUST_SSL, parents, message, input);
        Assert.assertTrue("command must succeed", success);
    }
}
