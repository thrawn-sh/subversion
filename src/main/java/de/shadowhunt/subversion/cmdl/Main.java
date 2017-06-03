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

import java.util.Arrays;
import java.util.ServiceLoader;

public final class Main {

    public static void main(final String... args) throws Exception {
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
            if (commandName.equals(command.getName())) {
                if (!command.call(System.out, commandArguments)) {
                    System.exit(1);
                }
                return;
            }
        }
    }

    private Main() {
        // prevent instantiation
    }

}
