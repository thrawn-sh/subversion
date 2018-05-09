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
import java.io.PrintStream;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public abstract class AbstractCommandIT {

    public static final String BASE;

    private static final String BASE_URI;

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

    protected String getUri(final String suffix) {
        return BASE_URI + suffix;
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
