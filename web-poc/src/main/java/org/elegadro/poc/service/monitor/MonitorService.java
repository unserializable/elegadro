package org.elegadro.poc.service.monitor;

import java.util.concurrent.Callable;

/**
 * @author Taimo Peelo
 */
public interface MonitorService {
    void add(Object id, Callable<Integer> tracker);
    Callable<Integer> get(Object id);
    Callable<Integer> remove(Object id);
}
