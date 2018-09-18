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
package de.shadowhunt.subversion.internal.tracing;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class StopWatchLogger {

    public static final StopWatchLogger INSTANCE = new StopWatchLogger();

    private static final Logger LOGGER = LoggerFactory.getLogger("de.shadowhunt.subversion.tracing");

    private StopWatchLogger() {
        // prevent instantiation
    }

    @CheckForNull
    public StopWatch logStart(final String method, final Object... args) {
        if (!LOGGER.isTraceEnabled()) {
            return null;
        }

        if (args.length > 0) {
            final String argumentsString = Arrays.toString(args);
            LOGGER.trace("starting {} with arguments: {}", method, argumentsString);
        } else {
            LOGGER.trace("starting {}", method);
        }
        return StopWatch.createStarted();
    }

    public void logStop(final String method, @CheckForNull final StopWatch stopWatch) {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }

        stopWatch.stop();
        final long milliseconds = stopWatch.getTime(TimeUnit.MILLISECONDS);
        LOGGER.trace("completed {} in {}ms", method, milliseconds);
    }
}
