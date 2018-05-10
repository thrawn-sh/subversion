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

 HttpContext context = new BasicHttpContext();

 HttpClientBuilder httpBuilder = HttpClientBuilder.create();
 httpBuilder.setDefaultCredentialsProvider(cp);
 httpBuilder.setRetryHandler(new SubversionRequestRetryHandler());

 URIBuilder uriBuilder = new URIBuilder();
 URI uri = uriBuilder //
    .setScheme("http")//
    .setHost("scm.example.net") //
    .setPort(8080)
    .setPath("/svn/test-repo/trunk") //
    .build();

 RepositoryFactory factory = RepositoryFactory.getInstance();

 try (ClosableHttpClient client = httpBuilder.build()) {
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
    } finally {
        repository.rollbackIfNotCommitted(transaction);
    }
 }
```

The full documentation is located at https://dev.shadowhunt.de/subversion/

```sh
# to start a test environment via docker
docker run                                      \
    --init                                      \
    --read-only                                 \
    --rm=true                                   \
    --mount type=tmpfs,destination=/tmp         \
    --mount type=tmpfs,destination=/var/run     \
    --mount type=tmpfs,destination=/var/www/svn \
    shadowhunt/subversion:<VERSION>
```

Starting with version 4.0.0, a command line mode is included, which allows you
to execute various commands against a Subversion server without having the
Subverion client tools installed on the machine or a local copy checked out.

```sh
# listing all available commands
java -jar subversion-jar-with-dependencies.jar help

# to get a list of all parameters for a command
java -jar subversion-jar-with-dependencies.jar <COMMAND> --help

# example checkin
java -jar subversion-jar-with-dependencies.jar checkin    \
    --base=https://subversion.example.net/repository/test \
    --username=svnuser                                    \
    --password=secret                                     \
    --parents                                             \
    --resource=/release/test_report.txt                   \
    --message="upload new test report"                    \
    --input=/tmp/coverage_report.txt
```
