package org.freemountain.operator.datastore;

import java.util.Collection;

public interface DataStoreClient {
    /**
     * Creates a datastore. Does nothing if the datastore exists.
     * @param name datastore name
     */
    void createDataStore(String name);

    /**
     * Deletes a datastore if it exists.
     * @param name datastore name
     */
    void deleteDataStore(String name);

    /**
     * Give users access to the datastore. The datastore should exists. The users are created or updated.
     * @param datastoreName
     * @param users
     */
    void grantRightsOn(String datastoreName, Collection<DataStoreUser> users);

    /**
     * Deletes a user if it exists
     * @param name user name
     */
    void deleteUser(String name);
}
