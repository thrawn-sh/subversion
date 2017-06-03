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
package de.shadowhunt.subversion.internal;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.View;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryEncodingIT {

    private final DownloadLoader downloadLoader;

    private final InfoLoader infoLoader;

    private final ListLoader listLoader;

    private final LogLoader logLoader;

    private final Resource read;

    private final Repository repository;

    private final Resource write;

    protected AbstractRepositoryEncodingIT(final Repository repository, final UUID testId, final File root) {
        this.repository = repository;
        this.read = Resource.create("/00000000-0000-0000-0000-000000000000/encoding");
        this.write = Resource.create("/" + testId + "/encoding");
        final Resource basePath = repository.getBasePath();
        infoLoader = new InfoLoader(root, basePath);
        logLoader = new LogLoader(root, basePath);
        downloadLoader = new DownloadLoader(root, basePath);
        listLoader = new ListLoader(root, basePath);
    }

    private void checkProperties(final Resource resource) {
        final Info info = repository.info(resource, Revision.HEAD);
        final ResourceProperty[] properties = info.getProperties();
        Assert.assertEquals("expected number of properties", 0, properties.length);
    }

    private void deleteProperties(final Resource resource, final ResourceProperty property) {
        final Transaction transaction = repository.createTransaction();
        try {
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.propertiesDelete(transaction, resource, property);
            Assert.assertTrue("transaction must be active", transaction.isActive());
            repository.commit(transaction, "delete " + resource, true);
            Assert.assertFalse("transaction must not be active", transaction.isActive());
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
    }

    @Test
    public void test01_exists_01_Utf8File() throws Exception {
        testExists(read.append(Resource.create("/file_日本国.txt")), Revision.HEAD);
    }

    @Test
    public void test01_exists_01_Utf8Folder() throws Exception {
        testExists(read.append(Resource.create("/folder_中华人民共和国")), Revision.HEAD);
    }

    @Test
    public void test01_exists_02_XmlFile() throws Exception {
        testExists(read.append(Resource.create("/file_<&>'\".txt")), Revision.HEAD);
    }

    @Test
    public void test01_exists_02_XmlFolder() throws Exception {
        testExists(read.append(Resource.create("/folder_<&>'\"")), Revision.HEAD);
    }

    @Test
    public void test01_exists_03_UrlFile() throws Exception {
        testExists(read.append(Resource.create("/file ^%.txt")), Revision.HEAD);
    }

    @Test
    public void test01_exists_03_UrlFolder() throws Exception {
        testExists(read.append(Resource.create("/folder ^%")), Revision.HEAD);
    }

    @Test
    public void test01_exists_04_CombinedFile() throws Exception {
        testExists(read.append(Resource.create("/file_日本国_<&>'\"_ ^%.txt")), Revision.HEAD);
    }

    @Test
    public void test01_exists_04_CombinedFolder() throws Exception {
        testExists(read.append(Resource.create("/folder_中华人民共和国_<&>'\"_ ^%")), Revision.HEAD);
    }

    @Test
    public void test02_info_01_Utf8File() throws Exception {
        testInfo(read.append(Resource.create("/file_日本国.txt")), Revision.HEAD);
    }

    @Test
    public void test02_info_01_Utf8Folder() throws Exception {
        testInfo(read.append(Resource.create("/folder_中华人民共和国")), Revision.HEAD);
    }

    @Test
    public void test02_info_02_XmlFile() throws Exception {
        testInfo(read.append(Resource.create("/file_<&>'\".txt")), Revision.HEAD);
    }

    @Test
    public void test02_info_02_XmlFolder() throws Exception {
        testInfo(read.append(Resource.create("/folder_<&>'\"")), Revision.HEAD);
    }

    @Test
    public void test02_info_03_UrlFile() throws Exception {
        testInfo(read.append(Resource.create("/file ^%.txt")), Revision.HEAD);
    }

    @Test
    public void test02_info_03_UrlFolder() throws Exception {
        testInfo(read.append(Resource.create("/folder ^%")), Revision.HEAD);
    }

    @Test
    public void test02_info_04_CombinedFile() throws Exception {
        testInfo(read.append(Resource.create("/file_日本国_<&>'\"_ ^%.txt")), Revision.HEAD);
    }

    @Test
    public void test02_info_04_CombinedFolder() throws Exception {
        testInfo(read.append(Resource.create("/folder_中华人民共和国_<&>'\"_ ^%")), Revision.HEAD);
    }

    @Test
    public void test02_list_01_Utf8File() throws Exception {
        testList(read.append(Resource.create("/file_日本国.txt")), Revision.HEAD, Depth.INFINITY);
    }

    @Test
    public void test02_list_01_Utf8Folder() throws Exception {
        testList(read.append(Resource.create("/folder_中华人民共和国")), Revision.HEAD, Depth.INFINITY);
    }

    @Test
    public void test02_list_02_XmlFile() throws Exception {
        testList(read.append(Resource.create("/file_<&>'\".txt")), Revision.HEAD, Depth.INFINITY);
    }

    @Test
    public void test02_list_02_XmlFolder() throws Exception {
        testList(read.append(Resource.create("/folder_<&>'\"")), Revision.HEAD, Depth.INFINITY);
    }

    @Test
    public void test02_list_03_UrlFile() throws Exception {
        testList(read.append(Resource.create("/file ^%.txt")), Revision.HEAD, Depth.INFINITY);
    }

    @Test
    public void test02_list_03_UrlFolder() throws Exception {
        testList(read.append(Resource.create("/folder ^%")), Revision.HEAD, Depth.INFINITY);
    }

    @Test
    public void test02_list_04_CombinedFile() throws Exception {
        testList(read.append(Resource.create("/file_日本国_<&>'\"_ ^%.txt")), Revision.HEAD, Depth.INFINITY);
    }

    @Test
    public void test02_list_04_CombinedFolder() throws Exception {
        testList(read.append(Resource.create("/folder_中华人民共和国_<&>'\"_ ^%")), Revision.HEAD, Depth.INFINITY);
    }

    @Test
    public void test02_log_01_Utf8File() throws Exception {
        testLog(read.append(Resource.create("/file_日本国.txt")), Revision.INITIAL, Revision.HEAD);
    }

    @Test
    public void test02_log_01_Utf8Folder() throws Exception {
        testLog(read.append(Resource.create("/folder_中华人民共和国")), Revision.INITIAL, Revision.HEAD);
    }

    @Test
    public void test02_log_02_XmlFile() throws Exception {
        testLog(read.append(Resource.create("/file_<&>'\".txt")), Revision.INITIAL, Revision.HEAD);
    }

    @Test
    public void test02_log_02_XmlFolder() throws Exception {
        testLog(read.append(Resource.create("/folder_<&>'\"")), Revision.INITIAL, Revision.HEAD);
    }

    @Test
    public void test02_log_03_UrlFile() throws Exception {
        testLog(read.append(Resource.create("/file ^%.txt")), Revision.INITIAL, Revision.HEAD);
    }

    @Test
    public void test02_log_03_UrlFolder() throws Exception {
        testLog(read.append(Resource.create("/folder ^%")), Revision.INITIAL, Revision.HEAD);
    }

    @Test
    public void test02_log_04_CombinedFile() throws Exception {
        testLog(read.append(Resource.create("/file_日本国_<&>'\"_ ^%.txt")), Revision.INITIAL, Revision.HEAD);
    }

    @Test
    public void test02_log_04_CombinedFolder() throws Exception {
        testLog(read.append(Resource.create("/folder_中华人民共和国_<&>'\"_ ^%")), Revision.INITIAL, Revision.HEAD);
    }

    @Test
    public void test03_download_01_Utf8File() throws Exception {
        testDownload(read.append(Resource.create("/file_日本国.txt")), Revision.HEAD);
    }

    @Test
    public void test03_download_02_XmlFile() throws Exception {
        testDownload(read.append(Resource.create("/file_<&>'\".txt")), Revision.HEAD);
    }

    @Test
    public void test03_download_03_UrlFile() throws Exception {
        testDownload(read.append(Resource.create("/file ^%.txt")), Revision.HEAD);
    }

    @Test
    public void test03_download_04_CombinedFile() throws Exception {
        testDownload(read.append(Resource.create("/file_日本国_<&>'\"_ ^%.txt")), Revision.HEAD);
    }

    @Test
    public void test03_downloadUri_01_Utf8File() throws Exception {
        testDownloadUri(read.append(Resource.create("/file_日本国.txt")), Revision.HEAD);
    }

    @Test
    public void test03_downloadUri_02_XmlFile() throws Exception {
        testDownloadUri(read.append(Resource.create("/file_<&>'\".txt")), Revision.HEAD);
    }

    @Test
    public void test03_downloadUri_03_UrlFile() throws Exception {
        testDownloadUri(read.append(Resource.create("/file ^%.txt")), Revision.HEAD);
    }

    @Test
    public void test03_downloadUri_04_CombinedFile() throws Exception {
        testDownloadUri(read.append(Resource.create("/file_日本国_<&>'\"_ ^%.txt")), Revision.HEAD);
    }

    @Test
    public void test03_downloadUri_04_CombinedFolder() throws Exception {
        testDownloadUri(read.append(Resource.create("/folder_中华人民共和国_<&>'\"_ ^%")), Revision.HEAD);
    }

    @Test
    public void test04_add_01_Utf8File() throws Exception {
        AbstractRepositoryAddIT.file(repository, write.append(Resource.create("/add_日本国.txt")), "A", true);
    }

    @Test
    public void test04_add_02_XmlFile() throws Exception {
        AbstractRepositoryAddIT.file(repository, write.append(Resource.create("/add_<&>'\".txt")), "A", true);
    }

    @Test
    public void test04_add_03_UrlFile() throws Exception {
        AbstractRepositoryAddIT.file(repository, write.append(Resource.create("/add ^%.txt")), "A", true);
    }

    @Test
    public void test04_add_04_CombinedFile() throws Exception {
        AbstractRepositoryAddIT.file(repository, write.append(Resource.create("/add_日本国_<&>'\"_ ^%.txt")), "A", true);
    }

    @Test
    public void test04_mkdir_01_Utf8File() throws Exception {
        testMkdir(write.append(Resource.create("/mkdir_中华人民共和国")));
    }

    @Test
    public void test04_mkdir_02_XmlFile() throws Exception {
        testMkdir(write.append(Resource.create("/mkdir_<&>'\"")));
    }

    @Test
    public void test04_mkdir_03_UrlFile() throws Exception {
        testMkdir(write.append(Resource.create("/mkdir ^%")));
    }

    @Test
    public void test04_mkdir_04_CombinedFile() throws Exception {
        testMkdir(write.append(Resource.create("/mkdir_中华人民共和国_<&>'\"_ ^%")));
    }

    @Test
    public void test05_copy_01_Utf8File() throws Exception {
        final Resource source = write.append(Resource.create("/copy_source_日本国.txt"));
        final Resource target = write.append(Resource.create("/copy_target_日本国.txt"));
        testCopy(source, target);
    }

    @Test
    public void test05_copy_02_XmlFile() throws Exception {
        final Resource source = write.append(Resource.create("/copy_source_<&>'\".txt"));
        final Resource target = write.append(Resource.create("/copy_target_<&>'\".txt"));
        testCopy(source, target);
    }

    @Test
    public void test05_copy_03_UrlFile() throws Exception {
        final Resource source = write.append(Resource.create("/copy_source ^%.txt"));
        final Resource target = write.append(Resource.create("/copy_target ^%.txt"));
        testCopy(source, target);
    }

    @Test
    public void test05_copy_04_CombinedFile() throws Exception {
        final Resource source = write.append(Resource.create("/copy_source_日本国_<&>'\"_ ^%.txt"));
        final Resource target = write.append(Resource.create("/copy_target_日本国_<&>'\"_ ^%.txt"));
        testCopy(source, target);
    }

    @Test
    public void test05_delete_01_Utf8File() throws Exception {
        testDelete(write.append(Resource.create("/delete_日本国.txt")));
    }

    @Test
    public void test05_delete_02_XmlFile() throws Exception {
        testDelete(write.append(Resource.create("/delete_<&>'\".txt")));
    }

    @Test
    public void test05_delete_03_UrlFile() throws Exception {
        testDelete(write.append(Resource.create("/delete ^%.txt")));
    }

    @Test
    public void test05_delete_04_CombinedFile() throws Exception {
        testDelete(write.append(Resource.create("/delete_日本国_<&>'\"_ ^%.txt")));
    }

    @Test
    public void test05_propSet_01_Utf8File() throws Exception {
        testPropertiesSet(write.append(Resource.create("/propset_日本国.txt")));
    }

    @Test
    public void test05_propSet_02_XmlFile() throws Exception {
        testPropertiesSet(write.append(Resource.create("/propset_<&>'\".txt")));
    }

    @Test
    public void test05_propSet_03_UrlFile() throws Exception {
        testPropertiesSet(write.append(Resource.create("/propset ^%.txt")));
    }

    @Test
    public void test05_propSet_04_CombinedFile() throws Exception {
        testPropertiesSet(write.append(Resource.create("/propset_日本国_<&>'\"_ ^%.txt")));
    }

    @Test
    public void test06_move_01_Utf8File() throws Exception {
        final Resource source = write.append(Resource.create("/move_source_日本国.txt"));
        final Resource target = write.append(Resource.create("/move_target_日本国.txt"));
        testMove(source, target);
    }

    @Test
    public void test06_move_02_XmlFile() throws Exception {
        final Resource source = write.append(Resource.create("/move_source_<&>'\".txt"));
        final Resource target = write.append(Resource.create("/move_target_<&>'\".txt"));
        testMove(source, target);
    }

    @Test
    public void test06_move_03_UrlFile() throws Exception {
        final Resource source = write.append(Resource.create("/move_source ^%.txt"));
        final Resource target = write.append(Resource.create("/move_target ^%.txt"));
        testMove(source, target);
    }

    @Test
    public void test06_move_04_CombinedFile() throws Exception {
        final Resource source = write.append(Resource.create("/move_source_日本国_<&>'\"_ ^%.txt"));
        final Resource target = write.append(Resource.create("/move_target_日本国_<&>'\"_ ^%.txt"));
        testMove(source, target);
    }

    @Test
    public void test06_propDelete_01_Utf8File() throws Exception {
        testPropertiesDelete(write.append(Resource.create("/propdelete_日本国.txt")));
    }

    @Test
    public void test06_propDelete_02_XmlFile() throws Exception {
        testPropertiesDelete(write.append(Resource.create("/propdelete_<&>'\".txt")));
    }

    @Test
    public void test06_propDelete_03_UrlFile() throws Exception {
        testPropertiesDelete(write.append(Resource.create("/propdelete ^%.txt")));
    }

    @Test
    public void test06_propDelete_04_CombinedFile() throws Exception {
        testPropertiesDelete(write.append(Resource.create("/propdelete_日本国_<&>'\"_ ^%.txt")));
    }

    private void testCopy(final Resource source, final Resource target) throws Exception {
        AbstractRepositoryAddIT.file(repository, source, "A", true);
        Assert.assertFalse(target + " must not exist", repository.exists(target, Revision.HEAD));

        final Transaction transaction = repository.createTransaction();
        try {
            repository.copy(transaction, source, Revision.HEAD, target, false);
            repository.commit(transaction, "copy", true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));
    }

    private void testDelete(final Resource resource) throws Exception {
        AbstractRepositoryAddIT.file(repository, resource, "A", true);
        final Transaction transaction = repository.createTransaction();
        try {
            repository.delete(transaction, resource);
            repository.commit(transaction, "delete", true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
        Assert.assertFalse(resource + " must not exist", repository.exists(resource, Revision.HEAD));
    }

    private void testDownload(final Resource resource, final Revision revision) throws Exception {
        final InputStream expected = downloadLoader.load(resource, revision);
        final String message = resource + ": @" + revision;
        AbstractRepositoryDownloadIT.assertEquals(message, expected, repository.download(resource, revision));
    }

    private void testDownloadUri(final Resource resource, final Revision revision) {
        final View view = repository.createView();
        final AbstractBaseRepository ar = (AbstractBaseRepository) repository;
        final QualifiedResource qualifiedResource = new QualifiedResource(repository.getBasePath(), resource);
        final URI expected = URIUtils.appendResources(repository.getBaseUri(), ar.config.getVersionedResource(qualifiedResource, view.getHeadRevision()));
        final String message = resource + ": @" + revision;
        Assert.assertEquals(message, expected, repository.downloadURI(resource, revision));
    }

    private void testExists(final Resource resource, final Revision revision) throws Exception {
        final String message = resource + ": @" + revision;
        Assert.assertTrue(message, repository.exists(resource, revision));
    }

    private void testInfo(final Resource resource, final Revision revision) throws Exception {
        final Info expected = infoLoader.load(resource, revision);
        final String message = resource + ": @" + revision;
        AbstractRepositoryInfoIT.assertInfoEquals(message, expected, repository.info(resource, revision));
    }

    private void testList(final Resource resource, final Revision revision, final Depth depth) throws Exception {
        final Set<Info> expected = listLoader.load(resource, revision, depth);
        final String message = resource + ": @" + revision + " with depth: " + depth;
        AbstractRepositoryListIT.assertListEquals(message, expected, repository.list(resource, revision, depth));
    }

    private void testLog(final Resource resource, final Revision start, final Revision end) throws Exception {
        final List<Log> expected = logLoader.load(resource, start, end, 0);
        final String message = resource + ": " + start + " -> " + end + " (" + 0 + ")";
        Assert.assertEquals(message, expected, repository.log(resource, start, end, 0, false));
    }

    private void testMkdir(final Resource resource) throws Exception {
        Assert.assertFalse(resource + " must not exist", repository.exists(resource, Revision.HEAD));

        final Transaction transaction = repository.createTransaction();
        try {
            repository.mkdir(transaction, resource, true);
            repository.commit(transaction, "mkdir", true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
        Assert.assertTrue(resource + " must exist", repository.exists(resource, Revision.HEAD));
    }

    private void testMove(final Resource source, final Resource target) throws Exception {
        AbstractRepositoryAddIT.file(repository, source, "A", true);
        Assert.assertFalse(target + " must not exist", repository.exists(target, Revision.HEAD));

        final Transaction transaction = repository.createTransaction();
        try {
            repository.move(transaction, source, target, false);
            repository.commit(transaction, "move", true);
        } finally {
            repository.rollbackIfNotCommitted(transaction);
        }
        Assert.assertTrue(target + " must exist", repository.exists(target, Revision.HEAD));
    }

    private void testPropertiesDelete(final Resource resource) throws Exception {
        final ResourceProperty property = new ResourceProperty(ResourceProperty.Type.SUBVERSION_CUSTOM, "test", "test");
        testPropertiesSet(resource);

        deleteProperties(resource, property);
        checkProperties(resource);
    }

    private void testPropertiesSet(final Resource resource) throws Exception {
        AbstractRepositoryAddIT.file(repository, resource, "A", true);

        final ResourceProperty property = new ResourceProperty(ResourceProperty.Type.SUBVERSION_CUSTOM, "test", "test");
        AbstractRepositoryPropertiesSetIT.setProperties(repository, resource, property);
    }
}
