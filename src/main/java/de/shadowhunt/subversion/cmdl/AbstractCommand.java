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
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.http.client.SubversionRequestRetryHandler;
import joptsimple.ArgumentAcceptingOptionSpec;
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

    protected final OptionSpec<URI> createBaseOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("base", "b");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "repository base");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("url");
        optionSpec = optionSpec.required();
        return optionSpec.ofType(URI.class);
    }

    protected final OptionSpec<String> createCommitMessageOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("message", "m");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "commit message");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("commit message");
        optionSpec = optionSpec.required();
        return optionSpec.ofType(String.class);
    }

    private OptionSpec<Void> createHelpOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("help", "h", "?");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "show this command help");
        return builder.forHelp();
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
            final SSLContext sslContext = createUnsaveSslContext();
            builder.setSSLContext(sslContext);
            builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }

        final SubversionRequestRetryHandler retryHandler = new SubversionRequestRetryHandler();
        builder.setRetryHandler(retryHandler);
        return builder.build();
    }

    protected final HttpContext createHttpContext() {
        return new BasicHttpContext();
    }

    protected final OptionSpec<Void> createNoUnlockOption(final OptionParser parser) {
        return parser.accepts("no-unlock", "don't unlock the resources");
    }

    protected final OptionSpec<File> createOutputOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("output", "o");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "output file");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("file");
        return optionSpec.ofType(File.class);
    }

    protected final OptionSpec<Void> createParentsOption(final OptionParser parser) {
        return parser.accepts("parents", "create missing parent folders");
    }

    protected final OptionParser createParser() {
        final OptionParser parser = new OptionParser(false);
        final BuiltinHelpFormatter formatter = new BuiltinHelpFormatter(160, 2);
        parser.formatHelpWith(formatter);
        parser.posixlyCorrect(true);
        return parser;
    }

    protected final OptionSpec<String> createPasswordOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("password", "p");
        OptionSpecBuilder builder = parser.acceptsAll(options, "login password");
        builder = builder.requiredIf("username"); // only final require password if final username is given
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("password");
        return optionSpec.ofType(String.class);
    }

    protected OptionSpec<ResourceProperty> createPropertiesOption(final OptionParser parser, final boolean onlyName) {
        final List<String> options = Arrays.asList("property", "p");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "property");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("property");
        optionSpec = optionSpec.required();
        optionSpec = optionSpec.withValuesSeparatedBy(",");
        final ResourcePropertyConverter converter = new ResourcePropertyConverter(onlyName);
        return optionSpec.withValuesConvertedBy(converter);
    }

    protected final OptionSpec<Resource> createResourceOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("resource", "r");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "resource path");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("path");
        optionSpec = optionSpec.required();
        final ResourceConverter converter = new ResourceConverter();
        return optionSpec.withValuesConvertedBy(converter);
    }

    protected final OptionSpec<Revision> createRevisionOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("version", "v");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "resource version");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("version");
        final RevisionConverter converter = new RevisionConverter();
        final ArgumentAcceptingOptionSpec<Revision> typedOptionSpec = optionSpec.withValuesConvertedBy(converter);
        return typedOptionSpec.defaultsTo(Revision.HEAD);
    }

    protected final OptionSpec<Resource> createSourceResourceOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("source", "s");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "source resource path");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("path");
        optionSpec = optionSpec.required();
        final ResourceConverter converter = new ResourceConverter();
        return optionSpec.withValuesConvertedBy(converter);
    }

    protected final OptionSpec<Void> createSslOption(final OptionParser parser) {
        return parser.accepts("trust-ssl", "don't validate SSL");
    }

    protected final OptionSpec<Revision> createStartRevisionOption(final OptionParser parser) {
        final OptionSpecBuilder builder = parser.accepts("start", "start version");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("version");
        final RevisionConverter converter = new RevisionConverter();
        final ArgumentAcceptingOptionSpec<Revision> typedOptionSpec = optionSpec.withValuesConvertedBy(converter);
        return typedOptionSpec.defaultsTo(Revision.HEAD);
    }

    protected final OptionSpec<Void> createStealLockOption(final OptionParser parser) {
        return parser.accepts("steal-lock", "steal existing lock");
    }

    protected final OptionSpec<Revision> createStopRevisionOption(final OptionParser parser) {
        final OptionSpecBuilder builder = parser.accepts("stop", "stop version");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("version");
        final RevisionConverter converter = new RevisionConverter();
        final ArgumentAcceptingOptionSpec<Revision> typedOptionSpec = optionSpec.withValuesConvertedBy(converter);
        return typedOptionSpec.defaultsTo(Revision.INITIAL);
    }

    protected final OptionSpec<Resource> createTargetResourceOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("target", "t");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "target resource path");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("path");
        optionSpec = optionSpec.required();
        final ResourceConverter converter = new ResourceConverter();
        return optionSpec.withValuesConvertedBy(converter);
    }

    private SSLContext createUnsaveSslContext() {
        try {
            final SSLContext sc = SSLContext.getInstance("TLS");
            final SecureRandom random = new SecureRandom();
            sc.init(null, new TrustManager[] { TRUST_ALL_MANAGER }, random);
            return sc;
        } catch (final Exception e) {
            throw new SubversionException("can not create unsave SSLContext", e);
        }
    }

    protected final OptionSpec<String> createUsernameOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("username", "u");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "login username");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("username");
        return optionSpec.ofType(String.class);
    }

    protected final OptionSpec<File> createWireLogOption(final OptionParser parser) {
        final List<String> options = Arrays.asList("wirelog", "w");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "dump all communication to");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("file");
        return optionSpec.ofType(File.class);
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
            final String absolutePath = log.getAbsolutePath();
            System.setProperty(SimpleLogger.LOG_FILE_KEY, absolutePath);
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
