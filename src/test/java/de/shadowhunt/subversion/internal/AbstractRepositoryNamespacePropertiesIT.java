/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
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
package de.shadowhunt.subversion.internal;

import java.io.File;
import java.util.UUID;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryNamespacePropertiesIT {

    private static final ResourceProperty[] EMPTY = new ResourceProperty[0];

    private static final ResourceProperty[] NAMESPACE_PROPERTIES = { new ResourceProperty(ResourceProperty.Type.SUBVERSION_CUSTOM, "namespace:name", "value") };

    public static final Resource READ_PREFIX = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/namespace_properties");

    private final InfoLoader infoLoader;

    private final Repository repository;

    private final Resource write_prefix;

    protected AbstractRepositoryNamespacePropertiesIT(final Repository repository, final File root, final UUID testId) {
        this.repository = repository;
        write_prefix = Resource.create("/trunk/" + testId + "/namespace_properties");
        infoLoader = new InfoLoader(root);
    }

    @Test
    public void test01_existingFile() throws Exception {
        final Resource resource = READ_PREFIX.append(Resource.create("/file.txt"));
        final Revision revision = Revision.HEAD;

        final Info expected = infoLoader.load(resource, revision);
        final String message = AbstractRepositoryInfoIT.createMessage(resource, revision);
        AbstractRepositoryInfoIT.assertInfoEquals(message, expected, repository.info(resource, revision));
    }

    @Test
    public void test02_newFile() throws Exception {
        final Resource resource = write_prefix.append(Resource.create("/file.txt"));
        final Revision revision = Revision.HEAD;

        AbstractRepositoryAddIT.file(repository, resource, "namespace_properties", true);
        final Info before = repository.info(resource, revision);
        Assert.assertArrayEquals("must not contain properties", EMPTY, before.getProperties());

        AbstractRepositoryPropertiesSetIT.setProperties(repository, resource, NAMESPACE_PROPERTIES);
        final Info after = repository.info(resource, revision);
        Assert.assertArrayEquals("must contain properties", NAMESPACE_PROPERTIES, after.getProperties());
    }
}
