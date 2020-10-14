package org.freemountain.operator.caches;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.freemountain.operator.common.LifecycleType;
import org.freemountain.operator.events.LifecycleEvent;

public class LifecycleClient<T extends HasMetadata> {
    public static class KubernetesEventHandler<T extends HasMetadata> {
        private final UnicastProcessor<LifecycleEvent<T>> buffer = UnicastProcessor.create();

        public void onAdd(T obj) {
            buffer.onNext(new LifecycleEvent<>(LifecycleType.ADDED, obj));
        }

        public void onUpdate(T oldObj, T newObj) {
            buffer.onNext(new LifecycleEvent<>(LifecycleType.MODIFIED, newObj, oldObj));
        }

        public void onDelete(T obj, boolean deletedFinalStateUnknown) {
            buffer.onNext(new LifecycleEvent<>(LifecycleType.DELETED, obj));
        }
    }

    private final SharedIndexInformer<T> informer;
    private final ResourceEventHandler<T> handler;
    private final UnicastProcessor<LifecycleEvent<T>> buffer;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public LifecycleClient(SharedIndexInformer<T> informer, ResourceEventHandler<T> handler) {
        this.informer = informer;
        this.handler = handler;
        this.buffer = getBuffer();
    }

    @SuppressWarnings(value = "unchecked")
    private UnicastProcessor<LifecycleEvent<T>> getBuffer() {
        try {
            return ((KubernetesEventHandler<T>) handler).buffer;
        } catch (ClassCastException e) {
            throw new RuntimeException("the provided handler has to extend KubernetesEventHandler");
        }
    }

    public Multi<LifecycleEvent<T>> connect() {
        boolean alreadyConnected = connected.getAndSet(true);
        if (alreadyConnected) {
            throw new RuntimeException("Client is already connected");
        }

        informer.addEventHandler(handler);
        return buffer.onSubscribe().invoke(sub -> informer.run());
    }
}
