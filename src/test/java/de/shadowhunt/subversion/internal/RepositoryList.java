/*
 * #%L
 * Shadowhunt Subversion
 * %%
 * Copyright (C) 2013 shadowhunt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.shadowhunt.subversion.internal;

import java.util.List;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryList {

	private static final Resource PREFIX = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/list");

	private Repository repository;

	private String createMessage(final Resource resource, final Revision revision, final Depth depth) {
		return resource + ": @" + revision + " with depth: " + depth;
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingResource() {
		final Resource resource = PREFIX.append(Resource.create("/non_existing.txt"));
		final Revision revision = Revision.HEAD;

		repository.list(resource, revision, Depth.EMPTY, true);
		Assert.fail("list must not complete");
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingRevision() {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision revision = Revision.create(10000000); // there should not be a such high revision

		repository.list(resource, revision, Depth.EMPTY, true);
		Assert.fail("list must not complete");
	}

	@Test
	public void test01_FileHead() {
		final Resource resource = PREFIX.append(Resource.create("/file.txt"));
		final Revision revision = Revision.HEAD;

		for (final Depth depth : Depth.values()) {
			final List<Info> expected = null;
			final String message = createMessage(resource, revision, depth);
			Assert.assertEquals(message, expected, repository.list(resource, revision, depth, true));
		}
	}

	@Test
	public void test01_FileRevision() {
		final Resource resource = PREFIX.append(Resource.create("/file_deleted.txt"));
		final Revision revision = null; // FIXME

		for (final Depth depth : Depth.values()) {
			final List<Info> expected = null; //FIXME
			final String message = createMessage(resource, revision, depth);
			Assert.assertEquals(message, expected, repository.list(resource, revision, depth, true));
		}
	}

	@Test
	public void test01_FolderHead() {
		final Resource resource = PREFIX.append(Resource.create("/folder"));
		final Revision revision = Revision.HEAD;

		for (final Depth depth : Depth.values()) {
			final List<Info> expected = null; //FIXME
			final String message = createMessage(resource, revision, depth);
			Assert.assertEquals(message, expected, repository.list(resource, revision, depth, true));
		}
	}

	@Test
	public void test01_FolderRevision() {
		final Resource resource = PREFIX.append(Resource.create("/folder_deleted"));
		final Revision revision = null; // FIXME

		for (final Depth depth : Depth.values()) {
			final List<Info> expected = null; //FIXME
			final String message = createMessage(resource, revision, depth);
			Assert.assertEquals(message, expected, repository.list(resource, revision, depth, true));
		}
	}

	@Test
	public void test02_FileCopy() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/file_copy.txt"));
		final Revision revision = Revision.create(51);

		for (final Depth depth : Depth.values()) {
			final List<Info> expected = null; //FIXME
			final String message = createMessage(resource, revision, depth);
			Assert.assertEquals(message, expected, repository.list(resource, revision, depth, true));
		}
	}

	@Test
	public void test02_FileMove() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/file_move.txt"));
		final Revision revision = null; // FIXME

		for (final Depth depth : Depth.values()) {
			final List<Info> expected = null; //FIXME
			final String message = createMessage(resource, revision, depth);
			Assert.assertEquals(message, expected, repository.list(resource, revision, depth, true));
		}
	}

	@Test
	public void test02_FolderCopy() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/folder_copy"));
		final Revision revision = Revision.create(57);

		for (final Depth depth : Depth.values()) {
			final List<Info> expected = null; //FIXME
			final String message = createMessage(resource, revision, depth);
			Assert.assertEquals(message, expected, repository.list(resource, revision, depth, true));
		}
	}

	@Test
	public void test02_FolderMove() throws Exception {
		final Resource resource = PREFIX.append(Resource.create("/folder_move"));
		final Revision revision = null; // FIXME

		for (final Depth depth : Depth.values()) {
			final List<Info> expected = null; //FIXME
			final String message = createMessage(resource, revision, depth);
			Assert.assertEquals(message, expected, repository.list(resource, revision, depth, true));
		}
	}
}
