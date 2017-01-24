/**
 * Copyright (C) 2013-2017 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion.cmdl;

import java.io.PrintStream;
import java.net.URI;
import java.util.Arrays;

import de.shadowhunt.http.client.SubversionRequestRetryHandler;

import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionParser;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

abstract class AbstractCommand implements Command {

    private final String name;

    protected AbstractCommand(final String name) {
        this.name = name;
    }

    @Override
    public abstract boolean call(final PrintStream output, final String... args) throws Exception;

    protected final OptionSpec<URI> createBaseOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("base", "b"), "repository base") //
                .withRequiredArg() //
                .describedAs("url") //
                .ofType(URI.class) //
                .required();
    }

    protected final OptionSpec<Void> createHelpOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("help", "h", "?"), "show help") //
                .forHelp();
    }

    protected final HttpClient createHttpClient(final String username, final String password) {
        final HttpClientBuilder builder = HttpClientBuilder.create();

        if (StringUtils.isNotBlank(username)) {
            final CredentialsProvider cp = new BasicCredentialsProvider();
            final Credentials credentials = new UsernamePasswordCredentials(username, password);
            cp.setCredentials(AuthScope.ANY, credentials);
            builder.setDefaultCredentialsProvider(cp);
        }

        builder.setRetryHandler(new SubversionRequestRetryHandler());
        return builder.build();
    }

    protected final HttpContext createHttpContext() {
        return new BasicHttpContext();
    }

    protected final OptionParser createParser() {
        final OptionParser parser = new OptionParser(false);
        parser.formatHelpWith(new BuiltinHelpFormatter(160, 2));
        parser.posixlyCorrect(true);
        return parser;
    }

    protected final OptionSpec<String> createPasswordOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("password", "p"), "login password") //
                .withRequiredArg() //
                .describedAs("password") //
                .ofType(String.class) //
                .required();
    }

    protected final OptionSpec<String> createResourceOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("resource", "r"), "resource path") //
                .withRequiredArg() //
                .describedAs("path") //
                .ofType(String.class) //
                .required();
    }

    protected final OptionSpec<Integer> createRevisionOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("version", "v"), "resource version") //
                .withRequiredArg() //
                .describedAs("version") //
                .ofType(Integer.class);
    }

    protected final OptionSpecBuilder createSslOption(final OptionParser parser) {
        return parser //
                .accepts("trust-ssl", "don't validate SSL");
    }

    protected final OptionSpec<String> createUsernameOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("username", "u"), "login username") //
                .withRequiredArg() //
                .describedAs("username") //
                .ofType(String.class) //
                .required();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractCommand other = (AbstractCommand) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public final String toString() {
        return name;
    }
}
