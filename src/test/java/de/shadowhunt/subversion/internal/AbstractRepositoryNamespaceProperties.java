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

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractRepositoryNamespaceProperties {

    private static final ResourceProperty[] EMPTY = new ResourceProperty[0];

    private static final ResourceProperty[] NAMESPACE_PROPERTIES = { new ResourceProperty(ResourceProperty.Type.SUBVERSION_CUSTOM, "namespace:name", "value") };

    public static final Resource READ_PREFIX = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/namespace_properties");

    private final InfoLoader infoLoader;

    private final Repository repository;

    private final Resource write_prefix;

    protected AbstractRepositoryNamespaceProperties(final Repository repository, final File root, final UUID testId) {
        this.repository = repository;
        write_prefix = Resource.create("/trunk/" + testId + "/namespace_properties");
        infoLoader = new InfoLoader(root);
    }

    @Test
    public void test01_existingFile() throws Exception {
        final Resource resource = READ_PREFIX.append(Resource.create("/file.txt"));
        final Revision revision = Revision.HEAD;

        final Info expected = infoLoader.load(resource, revision);
        final String message = AbstractRepositoryInfo.createMessage(resource, revision);
        AbstractRepositoryInfo.assertInfoEquals(message, expected, repository.info(resource, revision));
    }

    @Test
    public void test02_newFile() throws Exception {
        final Resource resource = write_prefix.append(Resource.create("/file.txt"));
        final Revision revision = Revision.HEAD;

        AbstractRepositoryAdd.file(repository, resource, "namespace_properties", true);
        final Info before = repository.info(resource, revision);
        Assert.assertArrayEquals("must not contain properties", EMPTY, before.getProperties());

        AbstractRepositoryPropertiesSet.setProperties(repository, resource, NAMESPACE_PROPERTIES);
        final Info after = repository.info(resource, revision);
        Assert.assertArrayEquals("must contain properties", NAMESPACE_PROPERTIES, after.getProperties());
    }
}
