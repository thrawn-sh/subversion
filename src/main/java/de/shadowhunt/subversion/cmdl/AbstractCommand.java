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
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.annotation.CheckForNull;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.http.client.SubversionRequestRetryHandler;
import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.impl.SimpleLogger;

abstract class AbstractCommand implements Command {

    private static final TrustManager TRUST_ALL_MANAGER = new X509TrustManager() {

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
            // nothing to to
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
            // nothing to to
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    private final String name;

    protected AbstractCommand(final String name) {
        this.name = name;
    }

    @Override
    public abstract boolean call(final PrintStream output, final PrintStream error, final String... args) throws Exception;

    protected final OptionSpec<URI> createBaseOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("base", "b"), "repository base") //
                .withRequiredArg() //
                .describedAs("url") //
                .ofType(URI.class) //
                .required();
    }

    protected final OptionSpec<String> createCommitMessageOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("message", "m"), "commit message") //
                .withRequiredArg() //
                .describedAs("commit message") //
                .ofType(String.class) //
                .required();
    }

    private OptionSpec<Void> createHelpOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("help", "h", "?"), "show this command help") //
                .forHelp();
    }

    protected final CloseableHttpClient createHttpClient(final String username, final String password, final boolean allowAllSsl) {
        final HttpClientBuilder builder = HttpClientBuilder.create();

        if (StringUtils.isNotBlank(username)) {
            final CredentialsProvider cp = new BasicCredentialsProvider();
            final Credentials credentials = new UsernamePasswordCredentials(username, password);
            cp.setCredentials(AuthScope.ANY, credentials);
            builder.setDefaultCredentialsProvider(cp);
        }

        if (allowAllSsl) {
            builder.setSSLContext(createUnsaveSslContext());
            builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }

        builder.setRetryHandler(new SubversionRequestRetryHandler());
        return builder.build();
    }

    protected final HttpContext createHttpContext() {
        return new BasicHttpContext();
    }

    protected final OptionSpec<File> createInputOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("input", "i"), "input file") //
                .withRequiredArg() //
                .describedAs("file") //
                .ofType(File.class) //
                .required();
    }

    protected final OptionSpecBuilder createNoUnlockOption(final OptionParser parser) {
        return parser //
                .accepts("no-unlock", "don't unlock the resources");
    }

    protected final OptionSpec<File> createOutputOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("output", "o"), "output file") //
                .withRequiredArg() //
                .describedAs("file") //
                .ofType(File.class);
    }

    protected final OptionSpecBuilder createParentsOption(final OptionParser parser) {
        return parser //
                .accepts("parents", "create missing parent folders");
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
                .ofType(String.class);
    }

    protected final OptionSpec<Resource> createResourceOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("resource", "r"), "resource path") //
                .withRequiredArg() //
                .describedAs("path") //
                .withValuesConvertedBy(new ResourceConverter()) //
                .required();
    }

    protected final OptionSpec<Revision> createRevisionOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("version", "v"), "resource version") //
                .withRequiredArg() //
                .describedAs("version") //
                .withValuesConvertedBy(new RevisionConverter()) //
                .defaultsTo(Revision.HEAD);
    }

    protected final OptionSpec<Resource> createSourceResourceOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("source", "s"), "source resource path") //
                .withRequiredArg() //
                .describedAs("path") //
                .withValuesConvertedBy(new ResourceConverter()) //
                .required();
    }

    protected final OptionSpecBuilder createSslOption(final OptionParser parser) {
        return parser //
                .accepts("trust-ssl", "don't validate SSL");
    }

    protected final OptionSpec<Revision> createStartRevisionOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("start"), "start version") //
                .withRequiredArg() //
                .describedAs("version") //
                .withValuesConvertedBy(new RevisionConverter()) //
                .defaultsTo(Revision.HEAD);
    }

    protected final OptionSpecBuilder createStealLockOption(final OptionParser parser) {
        return parser //
                .accepts("steal-lock", "steal existing lock");
    }

    protected final OptionSpec<Revision> createStopRevisionOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("stop"), "stop version") //
                .withRequiredArg() //
                .describedAs("version") //
                .withValuesConvertedBy(new RevisionConverter()) //
                .defaultsTo(Revision.INITIAL);
    }

    protected final OptionSpec<Resource> createTargetResourceOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("target", "t"), "target resource path") //
                .withRequiredArg() //
                .describedAs("path") //
                .withValuesConvertedBy(new ResourceConverter()) //
                .required();
    }

    private SSLContext createUnsaveSslContext() {
        try {
            final SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[] { TRUST_ALL_MANAGER }, new SecureRandom());
            return sc;
        } catch (final Exception e) {
            throw new SubversionException("can not create unsave SSLContext", e);
        }
    }

    protected final OptionSpec<String> createUsernameOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("username", "u"), "login username") //
                .withRequiredArg() //
                .describedAs("username") //
                .ofType(String.class);
    }

    protected final OptionSpec<File> createWireLogOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("wirelog", "w"), "dump all communication to") //
                .withRequiredArg() //
                .describedAs("file") //
                .ofType(File.class);
    }

    @Override
    public final String getName() {
        return name;
    }

    @CheckForNull
    protected final OptionSet parse(final PrintStream output, final PrintStream error, final OptionParser parser, final String... args) throws IOException {
        final OptionSpec<Void> helpOption = createHelpOption(parser);
        final OptionSpec<File> wireLogOption = createWireLogOption(parser);

        final OptionSet options;
        try {
            options = parser.parse(args);
        } catch (final OptionException e) {
            parser.printHelpOn(error);
            return null;
        }

        if (options.has(helpOption)) {
            parser.printHelpOn(output);
            return null;
        }

        final File log = options.valueOf(wireLogOption);
        if (log != null) {
            System.setProperty(SimpleLogger.DATE_TIME_FORMAT_KEY, "[yyyy-MM-dd HH:mm:ss.SSS]");
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "error");
            System.setProperty(SimpleLogger.LEVEL_IN_BRACKETS_KEY, "true");
            System.setProperty(SimpleLogger.LOG_FILE_KEY, log.getAbsolutePath());
            System.setProperty(SimpleLogger.LOG_KEY_PREFIX + "org.apache.http.wire", "debug");
            System.setProperty(SimpleLogger.SHOW_DATE_TIME_KEY, "true");
            System.setProperty(SimpleLogger.SHOW_LOG_NAME_KEY, "false");
            System.setProperty(SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        }

        return options;
    }

    @Override
    public final String toString() {
        return name;
    }
}
