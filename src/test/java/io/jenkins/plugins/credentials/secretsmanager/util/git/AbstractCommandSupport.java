package io.jenkins.plugins.credentials.secretsmanager.util.git;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionHolder;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.logging.AbstractLoggingBean;
import org.apache.sshd.common.util.threads.ExecutorServiceCarrier;
import org.apache.sshd.common.util.threads.ThreadUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.session.ServerSessionHolder;

/**
 * Imported from a newer version of Apache Mina SSHD.
 *
 * See https://github.com/apache/mina-sshd/blob/sshd-2.0.0/sshd-core/src/main/java/org/apache/sshd/server/command/AbstractCommandSupport.java
 */
abstract class AbstractCommandSupport
        extends AbstractLoggingBean
        implements Command, Runnable, ExecutorServiceCarrier, SessionAware,
        SessionHolder<Session>, ServerSessionHolder {
    private final String command;
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private Future<?> cmdFuture;
    private Thread cmdRunner;
    private ExecutorService executorService;
    private boolean shutdownOnExit;
    private boolean cbCalled;
    private ServerSession serverSession;

    AbstractCommandSupport(String command, ExecutorService executorService, boolean shutdownOnExit) {
        this.command = command;

        if (executorService == null) {
            String poolName = GenericUtils.isEmpty(command) ? getClass().getSimpleName() : command.replace(' ', '_').replace('/', ':');
            this.executorService = ThreadUtils.newSingleThreadExecutor(poolName);
            this.shutdownOnExit = true;    // we always close the ad-hoc executor service
        } else {
            this.executorService = executorService;
            this.shutdownOnExit = shutdownOnExit;
        }
    }

    String getCommand() {
        return command;
    }

    @Override
    public Session getSession() {
        return getServerSession();
    }

    @Override
    public ServerSession getServerSession() {
        return serverSession;
    }

    @Override
    public void setSession(ServerSession session) {
        serverSession = session;
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public boolean isShutdownOnExit() {
        return shutdownOnExit;
    }

    InputStream getInputStream() {
        return in;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    OutputStream getOutputStream() {
        return out;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    OutputStream getErrorStream() {
        return err;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    private ExitCallback getExitCallback() {
        return callback;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(Environment env) throws IOException {
        try {
            ExecutorService executors = getExecutorService();
            cmdFuture = executors.submit(() -> {
                cmdRunner = Thread.currentThread();
                this.run();
            });
        } catch (RuntimeException e) {    // e.g., RejectedExecutionException
            log.error("Failed (" + e.getClass().getSimpleName() + ") to start command=" + command + ": " + e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public void destroy() {
        // if thread has not completed, cancel it
        boolean debugEnabled = log.isDebugEnabled();
        if ((cmdFuture != null) && (!cmdFuture.isDone()) && (cmdRunner != Thread.currentThread())) {
            boolean result = cmdFuture.cancel(true);
            // TODO consider waiting some reasonable (?) amount of time for cancellation
            if (debugEnabled) {
                log.debug("destroy() - cancel pending future=" + result);
            }
        }

        cmdFuture = null;

        ExecutorService executors = getExecutorService();
        if ((executors != null) && (!executors.isShutdown()) && isShutdownOnExit()) {
            Collection<Runnable> runners = executors.shutdownNow();
            if (debugEnabled) {
                log.debug("destroy() - shutdown executor service - runners count=" + runners.size());
            }
        }
        this.executorService = null;
    }

    void onExit(int exitValue) {
        onExit(exitValue, "");
    }

    void onExit(int exitValue, String exitMessage) {
        if (cbCalled) {
            if (log.isTraceEnabled()) {
                log.trace("onExit({}) ignore exitValue={}, message={} - already called",
                        this, exitValue, exitMessage);
            }
            return;
        }

        ExitCallback cb = getExitCallback();
        try {
            if (log.isDebugEnabled()) {
                log.debug("onExit({}) exiting - value={}, message={}", this, exitValue, exitMessage);
            }
            cb.onExit(exitValue, exitMessage);
        } finally {
            cbCalled = true;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getCommand() + "]";
    }
}