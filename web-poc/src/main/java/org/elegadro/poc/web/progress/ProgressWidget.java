package org.elegadro.poc.web.progress;

import org.elegadro.poc.service.monitor.MonitorService;
import org.elegadro.poc.web.base.BaseAppUIWidget;
import lombok.extern.slf4j.Slf4j;
import org.araneaframework.InputData;
import org.araneaframework.OutputData;
import org.araneaframework.Path;
import org.araneaframework.core.StandardActionListener;
import org.araneaframework.http.HttpOutputData;
import org.araneaframework.uilib.util.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Writer;
import java.util.concurrent.Callable;

/**
 * @author Taimo Peelo
 */
@Slf4j
public class ProgressWidget extends BaseAppUIWidget {
    @Autowired
    private MonitorService monitorService;

    private ProgressReporter progressReporter;

    private Event initEvent = null;

    public ProgressWidget() {}

    public ProgressWidget(Callable<Integer> progressTracker) {
        initEvent = () -> start(progressTracker);
    }

    @Override
    protected void init() throws Exception {
        setViewSelector("web/progress/progress");
        if (initEvent != null) {
            initEvent.run();
            initEvent = null;
        }
    }

    public void start(Callable<Integer> progressTracker) {
        progressReporter = new ProgressReporter(progressTracker);
        addActionListener("pr", progressReporter);
    }

    public void restart(Callable<Integer> progressTracker) {
        stop();
        start(progressTracker);
    }

    public void stop() {
        // due to lack of destroy() call on listeners, we make one explicitly
        removeActionListener(progressReporter);
        progressReporter.destroy();
        progressReporter = null;
    }

    public int getProgress() {
        return isRunning() ? progressReporter.currentProgress() : 0;
    }

    public boolean isRunning() {
        return progressReporter != null;
    }

    private class ProgressReporter extends StandardActionListener {
        public ProgressReporter(Callable<Integer> progressTracker) {
            monitorService.add(getMonitorId(), progressTracker);
        }

        @Override
        public void processAction(String actionId, String actionParam, InputData input, OutputData output) throws Exception {
            Writer out = ((HttpOutputData) output).getWriter();
            out.write("{ \"done\" : " + currentProgress() + "}");
        }

        private int currentProgress() {
            try {
                Integer cr = getTracker().call();
                return cr.intValue();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Callable<Integer> getTracker() {
            return monitorService.get(getMonitorId());
        }

        private void destroy() {
            monitorService.remove(getMonitorId());
        }

        private String getMonitorId() {
            Path path = getScope().toPath();
            return path.toString();
        }
    }
}
