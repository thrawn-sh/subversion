package de.shadowhunt.subversion.internal;

import java.util.UUID;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.Transaction;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractRepositoryPropertiesDeleteIT {

	private final Resource prefix;

	private final Repository repository;

	protected AbstractRepositoryPropertiesDeleteIT(final Repository repository, final UUID testId) {
		prefix = Resource.create("/trunk/" + testId + "/propdel");
		this.repository = repository;
	}

	@Test(expected = SubversionException.class)
	public void test00_NonExisitingResource() throws Exception {
		final Resource resource = prefix.append(Resource.create("non_existing.txt"));
		final ResourceProperty property = new ResourceProperty(Type.CUSTOM, "test", "test");
		Assert.assertFalse(resource + " does already exist", repository.exists(resource, Revision.HEAD));

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.propertiesDelete(transaction, resource, property);
			Assert.fail("must not complete");
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test(expected = SubversionException.class)
	public void test00_rollback() throws Exception {
		final Resource resource = prefix.append(Resource.create("rollback.txt"));
		final ResourceProperty property = new ResourceProperty(Type.CUSTOM, "test", "test");

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.propertiesDelete(transaction, resource, property);
			Assert.assertTrue("transaction must be active", transaction.isActive());
			repository.rollback(transaction);
			Assert.assertFalse("transaction must not be valid", transaction.isActive());
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test(expected = SubversionException.class)
	public void test00_invalid() throws Exception {
		final Resource resource = prefix.append(Resource.create("invalid.txt"));
		final ResourceProperty property = new ResourceProperty(Type.CUSTOM, "test", "test");

		final Transaction transaction = repository.createTransaction();
		try {
			Assert.assertTrue("transaction must be active", transaction.isActive());
			transaction.invalidate();
			Assert.assertFalse("transaction must not be valid", transaction.isActive());
			repository.propertiesDelete(transaction, resource, property);
			Assert.fail("must not complete");
		} catch (final Exception e) {
			repository.rollback(transaction);
			throw e;
		}
	}

	@Test
	public void test01_deleteExisitingProperties() throws Exception {
		final Resource resource = prefix.append(Resource.create("file.txt"));
		final ResourceProperty property = new ResourceProperty(Type.CUSTOM, "test", "test");

		AbstractRepositoryAddIT.file(repository, resource, "test");
		AbstractRepositoryPropertiesSetIT.setProperties(repository, resource, property);

		{ // delete properties
			final Transaction transaction = repository.createTransaction();
			try {
				Assert.assertTrue("transaction must be active", transaction.isActive());
				repository.propertiesDelete(transaction, resource, property);
				Assert.assertTrue("transaction must be active", transaction.isActive());
				repository.commit(transaction, "delete " + resource);
				Assert.assertFalse("transaction must not be valid", transaction.isActive());
			} catch (final Exception e) {
				repository.rollback(transaction);
				throw e;
			}
		}

		{ // check properties
			final Info info = repository.info(resource, Revision.HEAD);
			final ResourceProperty[] properties = info.getProperties();
			Assert.assertEquals("expected number of properties", 0, properties.length);
		}
	}

	@Test
	public void test01_deleteNonExisitingProperties() throws Exception {
		final Resource resource = prefix.append(Resource.create("no_properties.txt"));
		final ResourceProperty property = new ResourceProperty(Type.CUSTOM, "test", "test");

		AbstractRepositoryAddIT.file(repository, resource, "test");

		{ // delete properties
			final Transaction transaction = repository.createTransaction();
			try {
				Assert.assertTrue("transaction must be active", transaction.isActive());
				repository.propertiesDelete(transaction, resource, property);
				Assert.assertTrue("transaction must be active", transaction.isActive());
				repository.commit(transaction, "delete " + resource);
				Assert.assertFalse("transaction must not be valid", transaction.isActive());
			} catch (final Exception e) {
				repository.rollback(transaction);
				throw e;
			}
		}

		{ // check properties
			final Info info = repository.info(resource, Revision.HEAD);
			final ResourceProperty[] properties = info.getProperties();
			Assert.assertEquals("expected number of properties", 0, properties.length);
		}
	}
}
