# Shadowhunt Subversion Library

The Shadowhunt Subversion library provides a simple JAVA interface to
[Subversion](http://subversion.apache.org/) servers through http or https
interface. It doesn't rely on the subversion command-line client.

The main purpose of this library is to access and manipulate a subversion
repository without having to checkout the repository to the local system first.
Its API is based on the command-lines client.

*Usage*

```java
 CredentialsProvider cp = new BasicCredentialsProvider();
 Credentials credentials = new UsernamePasswordCredentials(USERNAME, PASSWORD);
 cp.setCredentials(AuthScope.ANY, credentials);

 DefaultHttpClient client = new DefaultHttpClient();
 client.setCredentialsProvider(cp);
 client.setHttpRequestRetryHandler(new SubversionRequestRetryHandler());

 HttpContext context = new BasicHttpContext();

 URIBuilder builder = new URIBuilder();
 URI uri = builder //
    .setScheme("http")//
    .setHost("scm.example.net") //
    .setPort(8080)
    .setPath("/svn/test-repo/trunk") //
    .build();

 RepositoryFactory factory = RepositoryFactory.getInstance();
 Repository repository = factory.createRepository(uri, client, context, true);

 Transaction transaction = repository.createTransaction();
 try { // adding new files
     InputStream file1 = ...;
     repository.add(transaction, Resource.create("/folder/file1.txt"), true, file1);
     InputStream file2 = ...;
     repository.add(transaction, Resource.create("/folder/file2.txt"), true, file2);
     Resource sourceFile = Resource.create("/folder/source.txt");
     if (repository.exists(sourceFile, Revision.HEAD)) {
         repository.move(transaction, sourceFile, Resource.create("/folder/target.txt"), false);
     }
     repository.commit(transaction, "adding 2 files, renaming 1");
 } catch (SubversionException se) {
     repository.rollback(transaction);
 }
```

The full documentation is located at https://dev.shadowhunt.de/subversion/
