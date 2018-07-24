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
package de.shadowhunt.subversion.internal;

import java.net.URI;
import java.net.URISyntaxException;

import de.shadowhunt.subversion.Resource;
import org.apache.http.client.utils.URIBuilder;

/**
 * Helper class to construct valid {@link URI}s.
 */
public final class URIUtils {

    private static URI appendResource0(final URI repository, final Resource... resources) throws URISyntaxException {
        final URIBuilder builder = new URIBuilder();
        builder.setScheme(repository.getScheme());
        builder.setHost(repository.getHost());
        builder.setPort(repository.getPort());
        final StringBuilder completePath = new StringBuilder(repository.getPath());
        for (final Resource resource : resources) {
            completePath.append(resource.getValue());
        }
        builder.setPath(completePath.toString());
        return builder.build();
    }

    /**
     * Combine repository {@link URI} and {@link QualifiedResource} to a valid {@link URI}.
     *
     * @param repository base {@link URI}, {@link QualifiedResource} is appended to the path of the repository
     * @param resource {@link QualifiedResource} to appended to the repository {@link URI}
     *
     * @return combination of repository {@link URI} and {@link QualifiedResource}
     *
     * @throws IllegalArgumentException if resource contain {@code null} elements
     * @throws NullPointerException if repository is {@code null}
     */
    public static URI appendResources(final URI repository, final QualifiedResource resource) {
        return appendResources(repository, resource.getBase(), resource.getResource());
    }

    /**
     * Combine repository {@link URI} and {@link Resource}s to a valid {@link URI}.
     *
     * @param repository base {@link URI}, {@link Resource}s are appended to the path of the repository
     * @param resources {@link Resource}s to appended to the repository {@link URI}
     *
     * @return combination of repository {@link URI} and {@link Resource}s
     *
     * @throws IllegalArgumentException if resources contain {@code null} elements
     * @throws NullPointerException if repository is {@code null}
     */
    public static URI appendResources(final URI repository, final Resource... resources) {
        try {
            return appendResource0(repository, resources);
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Replace path elements from repository {@link URI} with {@link Resource}s to a valid {@link URI}.
     *
     * @param repository base {@link URI}
     * @param resources {@link Resource}s to replace the path of the repository {@link URI}
     *
     * @return replacement of path elements from repository {@link URI} with {@link Resource}s
     *
     * @throws IllegalArgumentException if resources contain {@code null} elements
     * @throws NullPointerException if repository is {@code null}
     */
    public static URI replacePathWithResources(final URI repository, final Resource... resources) {
        try {
            return replacePathWithResources0(repository, resources);
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static URI replacePathWithResources0(final URI repository, final Resource... resources) throws URISyntaxException {
        final URIBuilder builder = new URIBuilder();
        builder.setScheme(repository.getScheme());
        builder.setHost(repository.getHost());
        builder.setPort(repository.getPort());
        final StringBuilder completePath = new StringBuilder();
        for (final Resource resource : resources) {
            completePath.append(resource.getValue());
        }
        builder.setPath(completePath.toString());
        return builder.build();
    }

    private URIUtils() {
        // prevent instantiation
    }
}
