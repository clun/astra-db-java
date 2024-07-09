package com.datastax.astra.test.integration.dev;

import com.datastax.astra.client.Database;
import com.datastax.astra.test.integration.AbstractDatabaseTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

/**
 * Integration tests against a Local Instance of Stargate.
 */
class AstraDevDatabaseITTest extends AbstractDatabaseTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return initAstraDatabase(AstraEnvironment.DEV, CloudProviderType.GCP, "europe-west4");
        // return initAstraDatabase(AstraEnvironment.TEST, CloudProviderType.GCP, "us-central1");
        // return initAstraDatabase(AstraEnvironment.TEST, CloudProviderType.AWS, "us-west-2");
    }

}
