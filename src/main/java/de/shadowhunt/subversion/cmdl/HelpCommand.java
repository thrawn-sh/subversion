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
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;

public class HelpCommand extends AbstractCommand {

    public HelpCommand() {
        super("help");
    }

    @Override
    public boolean call(final PrintStream output, final PrintStream error, final String... args) throws Exception {
        final Set<String> names = new TreeSet<>();
        for (final Command command : ServiceLoader.load(Command.class)) {
            final String name = command.getName();
            names.add(name);
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
