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

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

public class CatCommand extends AbstractCommand {

    public CatCommand() {
        super("cat");
    }

    @Override
    public boolean call(final PrintStream output, final String... args) throws Exception {
        final OptionParser parser = createParser();
        final OptionSpec<URI> baseOption = createBaseOption(parser);
        final OptionSpec<Void> helpOption = createHelpOption(parser);
        final OptionSpec<String> resourceOption = createResourceOption(parser);
        final OptionSpec<Integer> revisionOption = createRevisionOption(parser);
        final OptionSpec<String> usernameOption = createUsernameOption(parser);
        final OptionSpec<String> passwordOption = createPasswordOption(parser);
        // final OptionSpecBuilder sslOption = createSslOption(parser); TODO

        final OptionSet options;
        try {
            options = parser.parse(args);
        } catch (final OptionException e) {
            parser.printHelpOn(output);
            return false;
        }

        if (options.has(helpOption)) {
            parser.printHelpOn(output);
            return true;
        }

        final HttpClient client = createHttpClient(usernameOption.value(options), passwordOption.value(options));
        final HttpContext context = createHttpContext();

        final RepositoryFactory factory = RepositoryFactory.getInstance();
        final Repository repository = factory.createRepository(baseOption.value(options), client, context, true);

        final Resource resource = Resource.create(resourceOption.value(options));
        final Revision revision;
        if (options.has(revisionOption)) {
            revision = Revision.create(revisionOption.value(options));
        } else {
            revision = Revision.HEAD;
        }

        try (InputStream download = repository.download(resource, revision)) {
            IOUtils.copy(download, output);
        }
        return true;
    }

}
