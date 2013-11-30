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
package de.shadowhunt.subversion.internal.httpv1.v1_4;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ Prepare.class, // pull dump.zip
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
		RepositoryResolve.class, // uses *ALL*
		// multiple modifications
		RepositoryCombinedOperations.class, // *ALL*
})
public class SuiteIT {
	// no code, just a placeholder class
}
