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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.View;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryPerformanceIT {

    public static class CountingHttpRequestInterceptor implements HttpRequestInterceptor {

        private int totalRequestCount = 0;

        int getTotalRequestCount() {
            return totalRequestCount;
        }

        @Override
        public void process(final HttpRequest request, final HttpContext context) {
            totalRequestCount++;
        }

        void reset() {
            totalRequestCount = 0;
        }
    }

    private final CountingHttpRequestInterceptor counter;

    private View currentView = null;

    private final Resource prefix;

    private final Repository repository;

    protected AbstractRepositoryPerformanceIT(final Repository repository, final CountingHttpRequestInterceptor counter, final UUID testId) {
        this.repository = repository;
        this.counter = counter;
        prefix = Resource.create("/" + testId + "/performance");
    }

    @Before
    public void before() throws Exception {
        currentView = repository.createView();
        counter.reset();
    }

    private void prepare() {
        final Transaction transaction = repository.createTransaction();
        repository.mkdir(transaction, prefix, true);
        repository.commit(transaction, "commit", true);
    }

    private void prepare(final Resource resource) {
        final Transaction transaction = repository.createTransaction();
        repository.mkdir(transaction, prefix, true);

        repository.add(transaction, resource, false, new ByteArrayInputStream("test".getBytes()));
        repository.commit(transaction, "commit", true);
    }

    private void prepare(final Resource resource, final ResourceProperty... properties) {
        final Transaction transaction = repository.createTransaction();
        repository.mkdir(transaction, prefix, true);

        repository.add(transaction, resource, false, new ByteArrayInputStream("test".getBytes()));
        repository.propertiesSet(transaction, resource, properties);
        repository.commit(transaction, "commit", true);
    }

    @Test
    public void test00_createView() throws Exception {
        repository.createView();
        Assert.assertEquals("number of requests must match", 1, counter.getTotalRequestCount());
    }

    @Test
    public void test01_existingResource() throws Exception {
        final Resource resource = AbstractRepositoryExistsIT.PREFIX.append(Resource.create("/file.txt"));

        repository.exists(currentView, resource, Revision.HEAD);
        Assert.assertEquals("number of requests must match", 1, counter.getTotalRequestCount());
    }

    @Test
    public void test01_nonExistingResource() throws Exception {
        final Resource resource = AbstractRepositoryExistsIT.PREFIX.append(Resource.create("/non_existing.txt"));

        repository.exists(currentView, resource, Revision.HEAD);
        Assert.assertEquals("number of requests must match", 1, counter.getTotalRequestCount());
    }

    @Test
    public void test02_download() throws Exception {
        final Resource resource = AbstractRepositoryDownloadIT.PREFIX.append(Resource.create("/file.txt"));

        try (final InputStream is = repository.download(currentView, resource, Revision.HEAD)) {
            Assert.assertEquals("number of requests must match", 1, counter.getTotalRequestCount());
        }
    }

    @Test
    public void test02_downloadUri() throws Exception {
        final Resource resource = AbstractRepositoryDownloadUriIT.PREFIX.append(Resource.create("/file.txt"));

        repository.downloadURI(currentView, resource, Revision.HEAD);
        Assert.assertEquals("number of requests must match", 1, counter.getTotalRequestCount());
    }

    @Test
    public void test02_info() throws Exception {
        final Resource resource = AbstractRepositoryInfoIT.PREFIX.append(Resource.create("/file.txt"));

        repository.info(currentView, resource, Revision.HEAD);
        Assert.assertEquals("number of requests must match", 1, counter.getTotalRequestCount());
    }

    @Test
    public void test02_list_Empty() throws Exception {
        final Resource resource = AbstractRepositoryListIT.PREFIX.append(Resource.create("/folder"));

        repository.list(currentView, resource, Revision.HEAD, Depth.EMPTY);
        Assert.assertEquals("number of requests must match", 1, counter.getTotalRequestCount());
    }

    @Test
    public void test02_list_Immediate() throws Exception {
        final Resource resource = AbstractRepositoryListIT.PREFIX.append(Resource.create("/folder"));

        repository.list(currentView, resource, Revision.HEAD, Depth.IMMEDIATES);
        Assert.assertEquals("number of requests must match", 1, counter.getTotalRequestCount());
    }

    @Test
    public void test02_list_Infinity() throws Exception {
        final Resource resource = AbstractRepositoryListIT.PREFIX.append(Resource.create("/folder"));

        repository.list(currentView, resource, Revision.HEAD, Depth.INFINITY);
        Assert.assertEquals("number of requests must match", 1, counter.getTotalRequestCount());
    }

    @Test
    public void test03_log() throws Exception {
        final Resource resource = AbstractRepositoryLogIT.PREFIX.append(Resource.create("/file.txt"));

        repository.log(currentView, resource, Revision.INITIAL, Revision.HEAD, 0, false);
        Assert.assertEquals("number of requests must match", 1, counter.getTotalRequestCount());
    }

    @Test
    public void test10_transactionCommit() throws Exception {
        final Transaction transaction = repository.createTransaction();
        int expectedRequestForCreate = 2;
        if (repository.getProtocolVersion() == Repository.ProtocolVersion.HTTP_V1) {
            expectedRequestForCreate += 1;
        }
        Assert.assertEquals("number of requests must match", expectedRequestForCreate, counter.getTotalRequestCount());

        counter.reset();
        repository.commit(transaction, "empty commit", true);
        Assert.assertEquals("number of requests must match", 1, counter.getTotalRequestCount());
    }

    @Test
    public void test10_transactionRollback() throws Exception {
        final Transaction transaction = repository.createTransaction();
        int expectedRequestForCreate = 2;
        if (repository.getProtocolVersion() == Repository.ProtocolVersion.HTTP_V1) {
            expectedRequestForCreate += 1;
        }
        Assert.assertEquals("number of requests must match", expectedRequestForCreate, counter.getTotalRequestCount());

        counter.reset();
        repository.rollback(transaction);
        Assert.assertEquals("number of requests must match", 1, counter.getTotalRequestCount());
    }

    @Test
    public void test11_mkdirMultipleFolder() throws Exception {
        prepare();

        final Transaction transaction = repository.createTransaction();
        counter.reset();

        final Resource resource = prefix.append(Resource.create("/mkdir_with_parent/a/b/c"));
        repository.mkdir(transaction, resource, true);
        int expectedRequest = 9;
        if (repository.getProtocolVersion() == Repository.ProtocolVersion.HTTP_V1) {
            expectedRequest += 1;
        }
        Assert.assertEquals("number of requests must match", expectedRequest, counter.getTotalRequestCount());

        repository.commit(transaction, "commit", true);
    }

    @Test
    public void test11_mkdirSingleFolder() throws Exception {
        prepare();

        final Transaction transaction = repository.createTransaction();
        counter.reset();

        final Resource resource = prefix.append(Resource.create("/mkdir_single"));
        repository.mkdir(transaction, resource, false);
        Assert.assertEquals("number of requests must match", 2, counter.getTotalRequestCount());

        repository.commit(transaction, "commit", true);
    }

    @Test
    public void test12_add() throws Exception {
        final Transaction transaction = repository.createTransaction();
        repository.mkdir(transaction, prefix, true);
        counter.reset();

        final Resource resource = prefix.append(Resource.create("/file.txt"));
        repository.add(transaction, resource, false, new ByteArrayInputStream("test".getBytes()));
        Assert.assertEquals("number of requests must match", 2, counter.getTotalRequestCount());

        repository.commit(transaction, "commit", true);
    }

    @Test
    public void test12_delete() throws Exception {
        final Resource resource = prefix.append(Resource.create("/delete.txt"));
        prepare(resource);

        final Transaction transaction = repository.createTransaction();
        counter.reset();

        repository.delete(transaction, resource);
        Assert.assertEquals("number of requests must match", 2, counter.getTotalRequestCount());

        repository.commit(transaction, "commit", true);
    }

    @Test
    public void test12_propertiesDelete() throws Exception {
        final Resource resource = prefix.append(Resource.create("/properties_delete.txt"));
        final ResourceProperty a = new ResourceProperty(ResourceProperty.Type.SUBVERSION_CUSTOM, "a", "a");
        final ResourceProperty b = new ResourceProperty(ResourceProperty.Type.SUBVERSION_CUSTOM, "b", "b");
        prepare(resource, a, b);

        final Transaction transaction = repository.createTransaction();
        counter.reset();

        repository.propertiesDelete(transaction, resource, a, b);
        Assert.assertEquals("number of requests must match", 2, counter.getTotalRequestCount());

        repository.commit(transaction, "commit", true);
    }

    @Test
    public void test12_propertiesSet() throws Exception {
        final Resource resource = prefix.append(Resource.create("/properties_set.txt"));
        prepare(resource);

        final Transaction transaction = repository.createTransaction();
        counter.reset();

        final ResourceProperty a = new ResourceProperty(ResourceProperty.Type.SUBVERSION_CUSTOM, "a", "a");
        final ResourceProperty b = new ResourceProperty(ResourceProperty.Type.SUBVERSION_CUSTOM, "b", "b");
        repository.propertiesSet(transaction, resource, a, b);
        Assert.assertEquals("number of requests must match", 2, counter.getTotalRequestCount());

        repository.commit(transaction, "commit", true);
    }

    @Test
    public void test13_copy() throws Exception {
        final Resource source = prefix.append(Resource.create("/copy_source.txt"));
        prepare(source);

        final Transaction transaction = repository.createTransaction();
        counter.reset();

        final Resource target = prefix.append(Resource.create("/copy_target.txt"));
        repository.copy(transaction, source, Revision.HEAD, target, false);
        int expectedRequest = 3;
        if (repository.getProtocolVersion() == Repository.ProtocolVersion.HTTP_V1) {
            expectedRequest += 1;
        }
        Assert.assertEquals("number of requests must match", expectedRequest, counter.getTotalRequestCount());

        repository.commit(transaction, "commit", true);
    }

    @Test
    public void test13_move() throws Exception {
        final Resource source = prefix.append(Resource.create("/move_source.txt"));
        prepare(source);

        final Transaction transaction = repository.createTransaction();
        counter.reset();

        final Resource target = prefix.append(Resource.create("/move_target.txt"));
        repository.move(transaction, source, target, false);
        int expectedRequest = 5;
        if (repository.getProtocolVersion() == Repository.ProtocolVersion.HTTP_V1) {
            expectedRequest += 1;
        }
        Assert.assertEquals("number of requests must match", expectedRequest, counter.getTotalRequestCount());

        repository.commit(transaction, "commit", true);
    }
}
