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

import java.io.File;
import java.util.UUID;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.View;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryNamespacePropertiesIT {

    private static final ResourceProperty[] EMPTY = new ResourceProperty[0];

    private static final ResourceProperty[] NAMESPACE_PROPERTIES = { new ResourceProperty(ResourceProperty.Type.SUBVERSION_CUSTOM, "namespace:name", "value") };

    public static final Resource READ_PREFIX = Resource.create("/00000000-0000-0000-0000-000000000000/namespace_properties");

    private final InfoLoader infoLoader;

    private final Repository repository;

    private final Resource write_prefix;

    protected AbstractRepositoryNamespacePropertiesIT(final Repository repository, final File root, final UUID testId) {
        this.repository = repository;
        write_prefix = Resource.create("/" + testId + "/namespace_properties");
        infoLoader = new InfoLoader(root, repository.getBasePath());
    }

    @Test
    public void test01_existingFile() throws Exception {
        final Resource resource = READ_PREFIX.append(Resource.create("/file.txt"));
        final Revision revision = Revision.HEAD;

        final Info expected = infoLoader.load(resource, revision);
        final String message = AbstractRepositoryInfoIT.createMessage(resource, revision);
        final View view = repository.createView();
        AbstractRepositoryInfoIT.assertInfoEquals(message, expected, repository.info(view, resource, revision));
    }

    @Test
    public void test02_newFile() throws Exception {
        final Resource resource = write_prefix.append(Resource.create("/file.txt"));
        final Revision revision = Revision.HEAD;

        AbstractRepositoryAddIT.file(repository, resource, "namespace_properties", true);
        final View beforeView = repository.createView();
        final Info before = repository.info(beforeView, resource, revision);
        Assert.assertArrayEquals("must not contain properties", EMPTY, before.getProperties());

        AbstractRepositoryPropertiesSetIT.setProperties(repository, resource, NAMESPACE_PROPERTIES);
        final View afterView = repository.createView();
        final Info after = repository.info(afterView, resource, revision);
        Assert.assertArrayEquals("must contain properties", NAMESPACE_PROPERTIES, after.getProperties());
    }
}
