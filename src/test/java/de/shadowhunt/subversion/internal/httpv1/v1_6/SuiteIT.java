package de.shadowhunt.subversion.internal.httpv1.v1_6;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
// read only
		RepositoryExists.class, //
		RepositoryDownloadUri.class, // uses exists
		RepositoryDownload.class, // uses downloadUri
		RepositoryInfo.class, // uses downloadUri
		RepositoryLog.class, // uses downloadUri + info
		RepositoryList.class, // uses downloadUri + info 
		// transactions
		RepositoryTransaction.class, //
		// single modifications
		RepositoryMkdir.class, // uses transactions + (add)
		RepositoryAdd.class, // uses transactions + mkdir
		RepositoryDelete.class, // uses transactions + add + mkdir
		RepositoryCopy.class, // uses transactions + add + mkdir
		RepositoryMove.class, // uses transactions + add + mkdir + delete
		RepositoryPropertiesSet.class, // uses transactions + add
		RepositoryPropertiesDelete.class, // uses transactions + add + propertiesSet
		RepositoryLocking.class, // uses add + copy + move
// multiple modifications
})
public class SuiteIT {
	// no code, just a placeholder class
}
