package org.freemountain.operator.kubernetes;

import io.quarkus.runtime.StartupEvent;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

public class OperatorStart {

    DataStoreAccessClaimOperator dataStoreAccessClaimOperator;
    DataStoreOperator dataStoreOperator;

    void onStartup(@Observes StartupEvent _ev) {
        //dataStoreOperator = CDI.current().select( DataStoreOperator.class ).get();
        //dataStoreAccessClaimOperator = CDI.current().select( DataStoreAccessClaimOperator.class ).get();

    }


}
