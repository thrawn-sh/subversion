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

import java.io.PrintStream;
import java.net.URI;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Repository.ProtocolVersion;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.Resource;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

public class ProbeCommand extends AbstractCommand {

    public ProbeCommand() {
        super("probe");
    }

    @Override
    public boolean call(final PrintStream output, final PrintStream error, final String... args) throws Exception {
        final OptionParser parser = createParser();
        final OptionSpec<String> usernameOption = createUsernameOption(parser);
        final OptionSpec<String> passwordOption = createPasswordOption(parser);
        final OptionSpec<Void> sslOption = createSslOption(parser);
        final OptionSpec<URI> urlOption = createUrlOption(parser);

        final OptionSet options = parse(output, error, parser, args);
        if (options == null) {
            return false;
        }

        final String username = usernameOption.value(options);
        final String password = passwordOption.value(options);
        final boolean allowAllSsl = options.has(sslOption);
        try (CloseableHttpClient client = createHttpClient(username, password, allowAllSsl)) {
            final RepositoryFactory factory = RepositoryFactory.getInstance();

            final HttpContext context = createHttpContext();
            final URI uri = urlOption.value(options);
            final Repository repository = factory.createRepository(uri, client, context, true);
            output.println("complete uri: " + uri);
            final URI baseUri = repository.getBaseUri();
            output.println("    base uri: " + baseUri);
            final Resource basePath = repository.getBasePath();
            output.println("    resource: " + basePath);
            final ProtocolVersion protocolVersion = repository.getProtocolVersion();
            output.println("    protocol: " + protocolVersion);
        }
        return true;
    }

    protected final OptionSpec<URI> createUrlOption(final OptionParser parser) {
        final OptionSpecBuilder builder = parser.accepts("url", "URL to resource");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("url");
        optionSpec = optionSpec.required();
        return optionSpec.ofType(URI.class);
    }

}
