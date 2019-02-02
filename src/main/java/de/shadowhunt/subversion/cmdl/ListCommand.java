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

import java.io.PrintStream;
import java.net.URI;
import java.util.Set;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.ReadOnlyRepository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.View;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

public class ListCommand extends AbstractCommand {

    public ListCommand() {
        super("list");
    }

    @Override
    public boolean call(final PrintStream output, final PrintStream error, final String... args) throws Exception {
        final OptionParser parser = createParser();
        final OptionSpec<URI> baseOption = createBaseOption(parser);
        final OptionSpec<Resource> resourceOption = createResourceOption(parser);
        final OptionSpec<String> usernameOption = createUsernameOption(parser);
        final OptionSpec<String> passwordOption = createPasswordOption(parser);
        final OptionSpec<Void> sslOption = createSslOption(parser);
        final OptionSpec<Revision> revisionOption = createRevisionOption(parser);

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
            final URI base = baseOption.value(options);
            final ReadOnlyRepository repository = factory.createReadOnlyRepository(base, client, context);
            final View view = repository.createView();

            final Resource resource = resourceOption.value(options);
            final Revision revision = revisionOption.value(options);
            final Set<Info> infos = repository.list(view, resource, revision, Depth.FILES);
            for (final Info info : infos) {
                final Revision infoRevision = info.getRevision();
                final Resource infoResource = info.getResource();
                output.format("%5s %s%n", infoRevision, infoResource);
            }
        }
        return true;
    }

}
