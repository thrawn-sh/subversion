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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Arrays;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Transaction;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

public class CheckinCommand extends AbstractCommand {

    public CheckinCommand() {
        super("checkin");
    }

    @Override
    public boolean call(final PrintStream output, final PrintStream error, final String... args) throws Exception {
        final OptionParser parser = createParser();
        final OptionSpec<URI> baseOption = createBaseOption(parser);
        final OptionSpec<Resource> resourceOption = createResourceOption(parser);
        final OptionSpec<String> usernameOption = createUsernameOption(parser);
        final OptionSpec<String> passwordOption = createPasswordOption(parser);
        final OptionSpec<Void> sslOption = createSslOption(parser);
        final OptionSpec<String> commitMessageOption = createCommitMessageOption(parser);
        final OptionSpec<Void> parentsOption = createParentsOption(parser);
        final OptionSpec<File> inputOption = createInputOption(parser);
        final OptionSpec<Void> noUnlockOption = createNoUnlockOption(parser);

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

            final Transaction transaction = repository.createTransaction();
            try {
                final File input = inputOption.value(options);
                try (InputStream content = new FileInputStream(input)) {
                    final Resource resource = resourceOption.value(options);
                    final boolean parents = options.has(parentsOption);

                    repository.add(transaction, resource, parents, content);

                    final String message = commitMessageOption.value(options);
                    final boolean releaseLocks = !options.has(noUnlockOption);
                    repository.commit(transaction, message, releaseLocks);
                }
            } finally {
                repository.rollbackIfNotCommitted(transaction);
            }
        }
        return true;
    }

    protected final OptionSpec<File> createInputOption(final OptionParser parser) {
        return parser //
                .acceptsAll(Arrays.asList("input", "i"), "input file") //
                .withRequiredArg() //
                .describedAs("file") //
                .ofType(File.class) //
                .required();
    }

}
