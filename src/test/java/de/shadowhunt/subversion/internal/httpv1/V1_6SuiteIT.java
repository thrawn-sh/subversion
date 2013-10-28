package de.shadowhunt.subversion.internal.httpv1;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
// read only
		V1_6RepositoryExists.class, //
		V1_6RepositoryDownloadUri.class, // uses exists
		V1_6RepositoryDownload.class, // uses downloadUri
		V1_6RepositoryInfo.class, // uses downloadUri
		V1_6RepositoryLog.class, // uses downloadUri + info
		V1_6RepositoryList.class, // uses downloadUri + info 
		// transactions
		V1_6RepositoryTransaction.class, //
		// single modifications
		V1_6RepositoryMkdir.class, // uses transactions
		V1_6RepositoryAdd.class, // uses transactions + mkdir
		V1_6RepositoryPropertiesSet.class, // uses transactions + add
		V1_6RepositoryPropertiesDelete.class, // uses transactions + add + propertiesSet
// multiple modifications
})
public class V1_6SuiteIT {

}
