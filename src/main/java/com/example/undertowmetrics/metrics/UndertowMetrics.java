package com.example.undertowmetrics.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Optional;

@Component
@Slf4j
public class UndertowMetrics implements MeterBinder {
    // can be more than one worker. See server.undertow.threads.io
    // # The default is derived from the number of available processors.
    private static final String OBJECT_NAME = "org.xnio:type=Xnio,provider=\"nio\",worker=\"XNIO-1\"";
    private static final String GAUGE_NAME_WORKER_QUEUE_SIZE = "undertow.worker.queue.size";
    private static final String GAUGE_NAME_WORKER_POOL_SIZE = "undertow.worker.pool.size";
    private static final String GAUGE_NAME_MAX_WORKER_POOL_SIZE = "undertow.worker.pool.max";
    private static final String GAUGE_NAME_IO_THREAD_COUNT = "undertow.io.thread-count";
    private static final String ATTR_WORKER_QUEUE_SIZE = "WorkerQueueSize";
    private static final String ATTR_WORKER_POOL_SIZE = "CoreWorkerPoolSize";
    private static final String ATTR_MAX_WORKER_POOL_SIZE = "MaxWorkerPoolSize";
    private static final String ATTR_IO_THREAD_COUNT = "IoThreadCount";
    private final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

    @Override
    public void bindTo(@NonNull MeterRegistry registry) {
        buildAndRegisterGauge(GAUGE_NAME_WORKER_QUEUE_SIZE,
                ATTR_WORKER_QUEUE_SIZE,
                "Undertow worker queue size",
                registry);

        buildAndRegisterGauge(GAUGE_NAME_WORKER_POOL_SIZE,
                ATTR_WORKER_POOL_SIZE,
                "Undertow worker pool size",
                registry);

        buildAndRegisterGauge(GAUGE_NAME_MAX_WORKER_POOL_SIZE,
                ATTR_MAX_WORKER_POOL_SIZE,
                "Undertow max worker pool size",
                registry);

        buildAndRegisterGauge(GAUGE_NAME_IO_THREAD_COUNT,
                ATTR_IO_THREAD_COUNT,
                "Undertow IO thread count",
                registry);
    }

    private void buildAndRegisterGauge(@NonNull String name,
                                       @NonNull String attributeName,
                                       @NonNull String description,
                                       @NonNull MeterRegistry registry) {
        Gauge.builder(name,
                        platformMBeanServer,
                        mBeanServer -> getWorkerAttribute(mBeanServer, attributeName))
                .description(description)
                .register(registry);
    }

    private double getWorkerAttribute(@NonNull MBeanServer mBeanServer, @NonNull String attributeName) {
        Object attributeValueObj = null;
        try {
            MBeanInfo mBeanInfo = mBeanServer.getMBeanInfo(workerObjectName());
            attributeValueObj = mBeanServer.getAttribute(workerObjectName(), attributeName);
        } catch (Exception e) {
            log.warn("Unable to get {} from JMX", attributeName, e);
        }
        return Optional.ofNullable(attributeValueObj)
                .map(value -> (Number) value)
                .map(Number::doubleValue)
                .orElse(0d);
    }

    private ObjectName workerObjectName() throws MalformedObjectNameException {
        return new ObjectName(OBJECT_NAME);
    }
}
