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
