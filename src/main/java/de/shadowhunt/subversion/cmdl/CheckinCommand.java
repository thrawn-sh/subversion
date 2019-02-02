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

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Transaction;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
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
            final Repository repository = factory.createRepository(base, client, context);

            final Transaction transaction = repository.createTransaction();
            try {
                final File input = inputOption.value(options);
                final Path path = input.toPath();
                try (InputStream content = Files.newInputStream(path)) {
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
        final List<String> options = Arrays.asList("input", "i");
        final OptionSpecBuilder builder = parser.acceptsAll(options, "input file");
        ArgumentAcceptingOptionSpec<String> optionSpec = builder.withRequiredArg();
        optionSpec = optionSpec.describedAs("file");
        optionSpec = optionSpec.required();
        return optionSpec.ofType(File.class);
    }

}
