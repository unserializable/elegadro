package org.elegadro.poc.service.monitor.impl;

import org.elegadro.poc.service.monitor.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Taimo Peelo
 */
@Slf4j
@Component
public class MonitorServiceImpl implements MonitorService {
    private ConcurrentMap<Object, Callable<Integer>> monitors = new ConcurrentHashMap<>();

    @Override
    public void add(Object id, Callable<Integer> tracker) {
        monitors.put(id, tracker);
    }

    @Override
    public Callable<Integer> get(Object id) {
        return monitors.get(id);
    }

    @Override
    public Callable<Integer> remove(Object id) {
        return monitors.remove(id);
    }
}
