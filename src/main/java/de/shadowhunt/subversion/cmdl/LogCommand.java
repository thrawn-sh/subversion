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
import java.net.URI;
import java.util.List;

import de.shadowhunt.subversion.Log;
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

public class LogCommand extends AbstractCommand {

    public LogCommand() {
        super("log");
    }

    @Override
    public boolean call(final PrintStream output, final PrintStream error, final String... args) throws Exception {
        final OptionParser parser = createParser();
        final OptionSpec<URI> baseOption = createBaseOption(parser);
        final OptionSpec<String> resourceOption = createResourceOption(parser);
        final OptionSpec<String> usernameOption = createUsernameOption(parser);
        final OptionSpec<String> passwordOption = createPasswordOption(parser);
        final OptionSpecBuilder sslOption = createSslOption(parser);
        final OptionSpec<Integer> startRevisionOption = createStartRevisionOption(parser);
        final OptionSpec<Integer> stopRevisionOption = createStopRevisionOption(parser);

        final OptionSet options = parse(output, error, parser, args);
        if (options == null) {
            return false;
        }

        final Resource resource = Resource.create(resourceOption.value(options));
        final Revision startRevision;
        if (options.has(startRevisionOption)) {
            final int value = startRevisionOption.value(options);
            startRevision = Revision.create(value);
        } else {
            startRevision = Revision.HEAD;
        }

        final Revision stopRevision;
        if (options.has(stopRevisionOption)) {
            final int value = stopRevisionOption.value(options);
            stopRevision = Revision.create(value);
        } else {
            stopRevision = Revision.INITIAL;
        }

        final String username = usernameOption.value(options);
        final String password = passwordOption.value(options);
        try (CloseableHttpClient client = createHttpClient(username, password, options.has(sslOption))) {
            final RepositoryFactory factory = RepositoryFactory.getInstance();

            final HttpContext context = createHttpContext();
            final URI base = baseOption.value(options);
            final Repository repository = factory.createRepository(base, client, context, true);

            final List<Log> logs = repository.log(resource, startRevision, stopRevision, Integer.MAX_VALUE, false);
            for (final Log log : logs) {
                output.println("------------------------------------------------------------------------");
                output.print(log.getRevision());
                output.print(" | ");
                output.print(log.getAuthor());
                output.print(" | ");
                output.println(log.getDate());
                output.println();
                output.println(log.getMessage());
            }
        }
        return true;
    }

}
