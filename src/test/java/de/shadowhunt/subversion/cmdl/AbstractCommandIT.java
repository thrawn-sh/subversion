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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import de.shadowhunt.subversion.Resource;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractCommandIT {

    public static final String BASE;

    private static final String BASE_URI;

    public static final String PASSWORD;

    public static final PrintStream TEST_ERR;

    public static final PrintStream TEST_OUT;

    public static final String TRUST_SSL;

    public static final String USERNAME;

    static {
        final String host = System.getProperty("subversion.host", "172.17.0.2"); // FIXME
        final String protocol = System.getProperty("subversion.protocol", "http");

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

    protected String[] filterArguments(final String... arguments) {
        final List<String> filtered = new ArrayList<>(arguments.length);
        for (final String argument : arguments) {
            if (StringUtils.isNotBlank(argument)) {
                filtered.add(argument);
            }
        }
        final String[] result = new String[filtered.size()];
        return filtered.toArray(result);
    }

    protected String getUri(final Resource resource) {
        return BASE_URI + resource;
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
}
