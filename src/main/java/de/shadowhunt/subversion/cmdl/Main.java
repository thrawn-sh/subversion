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
import java.util.Arrays;
import java.util.ServiceLoader;

public final class Main {

    static boolean delegate(final PrintStream output, final PrintStream error, final String... args) throws Exception {
        final String commandName;
        final String[] commandArguments;
        if (args.length > 0) {
            commandName = args[0];
            commandArguments = Arrays.copyOfRange(args, 1, args.length);
        } else {
            commandName = "help";
            commandArguments = new String[0];
        }

        for (final Command command : ServiceLoader.load(Command.class)) {
            final String name = command.getName();
            if (commandName.equals(name)) {
                return command.call(output, error, commandArguments);
            }
        }
        return false;
    }

    public static void main(final String... args) throws Exception {
        final boolean success = delegate(System.out, System.err, args);
        if (!success) {
            System.exit(1);
        }
    }

    private Main() {
        // prevent instantiation
    }

}
