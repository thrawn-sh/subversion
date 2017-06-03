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

import java.io.PrintStream;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;

public class HelpCommand extends AbstractCommand {

    public HelpCommand() {
        super("help");
    }

    @Override
    public boolean call(final PrintStream output, final String... args) throws Exception {
        final Set<String> names = new TreeSet<>();
        for (final Command command : ServiceLoader.load(Command.class)) {
            names.add(command.getName());
        }

        output.println("Available subcommands:");
        for (final String name : names) {
            output.println(" * " + name);
        }
        output.println();
        output.println(" for help on a specific subcommand: <subcommand> --help");
        return true;
    }
}
