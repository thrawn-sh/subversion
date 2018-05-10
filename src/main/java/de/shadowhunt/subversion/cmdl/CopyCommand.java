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

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

public class CopyCommand extends AbstractCommand {

    public CopyCommand() {
        super("copy");
    }

    @Override
    public boolean call(final PrintStream output, final PrintStream error, final String... args) throws Exception {
        final OptionParser parser = createParser();
        final OptionSpec<URI> baseOption = createBaseOption(parser);
        final OptionSpec<Resource> sourceOption = createSourceResourceOption(parser);
        final OptionSpec<Resource> targetOption = createTargetResourceOption(parser);
        final OptionSpec<String> usernameOption = createUsernameOption(parser);
        final OptionSpec<String> passwordOption = createPasswordOption(parser);
        final OptionSpecBuilder sslOption = createSslOption(parser);
        final OptionSpecBuilder parentsOption = createParentsOption(parser);
        final OptionSpec<String> commitMessageOption = createCommitMessageOption(parser);
        final OptionSpec<Revision> revisionOption = createRevisionOption(parser);
        final OptionSpecBuilder noUnlockOption = createNoUnlockOption(parser);

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
                final Resource source = sourceOption.value(options);
                final Revision sourceRevision = revisionOption.value(options);
                final Resource target = targetOption.value(options);
                final boolean parents = options.has(parentsOption);
                repository.copy(transaction, source, sourceRevision, target, parents);

                final String message = commitMessageOption.value(options);
                final boolean releaseLocks = !options.has(noUnlockOption);
                repository.commit(transaction, message, releaseLocks);
            } finally {
                repository.rollbackIfNotCommitted(transaction);
            }
        }
        return true;
    }

}
