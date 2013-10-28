package de.shadowhunt.subversion.internal.httpv1;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
// read only
		V1_6RepositoryExists.class, //
		V1_6RepositoryDownloadUri.class, //
		V1_6RepositoryDownload.class, //
		V1_6RepositoryInfo.class, //
		V1_6RepositoryList.class, //
		V1_6RepositoryLog.class, //
		// transactions
		V1_6RepositoryTransaction.class, //
		// single modifications
		V1_6RepositoryMkdir.class, //
		V1_6RepositoryAdd.class, //
// multiple modifications
})
public class V1_6SuiteIT {

}
