/**
 * Copyright © 2013-2017 shadowhunt (dev@shadowhunt.de)
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
import java.net.URI;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

public class ExistsCommand extends AbstractCommand {

    public ExistsCommand() {
        super("exists");
    }

    @Override
    public boolean call(final PrintStream output, final String... args) throws Exception {
        final OptionParser parser = createParser();
        final OptionSpec<URI> baseOption = createBaseOption(parser);
        final OptionSpec<String> resourceOption = createResourceOption(parser);
        final OptionSpec<String> usernameOption = createUsernameOption(parser);
        final OptionSpec<String> passwordOption = createPasswordOption(parser);
        final OptionSpecBuilder sslOption = createSslOption(parser);
        final OptionSpec<Integer> revisionOption = createRevisionOption(parser);

        final OptionSet options = parse(output, parser, args);
        if (options == null) {
            return false;
        }

        final Resource resource = Resource.create(resourceOption.value(options));
        final Revision revision;
        if (options.has(revisionOption)) {
            final int value = revisionOption.value(options);
            revision = Revision.create(value);
        } else {
            revision = Revision.HEAD;
        }

        final String username = usernameOption.value(options);
        final String password = passwordOption.value(options);
        try (CloseableHttpClient client = createHttpClient(username, password, options.has(sslOption))) {
            final RepositoryFactory factory = RepositoryFactory.getInstance();

            final HttpContext context = createHttpContext();
            final URI base = baseOption.value(options);
            final Repository repository = factory.createRepository(base, client, context, true);

            if (repository.exists(resource, revision)) {
                output.println("resource: " + resource + "@" + revision + " does exist");
            } else {
                output.println("resource: " + resource + "@" + revision + " does not exist");
            }
        }
        return true;
    }

}