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
import java.util.Optional;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

public class InfoCommand extends AbstractCommand {

    public InfoCommand() {
        super("info");
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
            final Repository repository = factory.createRepository(base, client, context, true);

            final Resource resource = resourceOption.value(options);
            final Revision revision = revisionOption.value(options);
            final Info info = repository.info(resource, revision);
            output.println("Reource: " + info.getResource());
            output.println("URL: " + repository.getBaseUri() + info.getResource());
            output.println("Repository Root: " + repository.getBaseUri());
            output.println("Repository UUID: " + info.getRepositoryId());
            output.println("Revision: " + revision);
            if (info.isDirectory()) {
                output.println("Node Kind: directory");
            } else {
                output.println("Node Kind: file");
            }
            output.println("Creation Date: " + info.getCreationDate());
            output.println("Last Changed Rev: " + info.getRevision());
            output.println("Last Changed Date: " + info.getLastModifiedDate());
            final Optional<LockToken> lockToken = info.getLockToken();
            if (lockToken.isPresent()) {
                output.println("Lock Token: opaquelocktoken: " + lockToken.get());
            }
            final Optional<String> lockOwner = info.getLockOwner();
            if (lockOwner.isPresent()) {
                output.println("Lock Owner: " + lockOwner.get());
            }
            final Optional<String> md5 = info.getMd5();
            if (md5.isPresent()) {
                output.println("Checksum: " + md5.get());
            }
            output.println();

            output.println("Properties:");
            final ResourceProperty[] properties = info.getProperties();
            for (final ResourceProperty property : properties) {
                output.println("  " + property.getName() + " = " + property.getValue());
            }
        }
        return true;
    }

}
