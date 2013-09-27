/* 
 * $Id$
 */
package zielu.svntoolbox.util;

import com.intellij.openapi.diagnostic.Logger;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * <p></p>
 * <br/>
 * <p>Created on 23.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public abstract class LogStopwatch {
    protected final Logger LOG;
    private final String myName;
    private long myStart;
    private long myLastTick;

    protected LogStopwatch(Logger log, String name) {
        LOG = log;
        this.myName = name;
    }

    public LogStopwatch start() {
        if (isEnabled()) {
            startInternal();
        }
        return this;
    }

    private void startInternal() {
        myStart = System.nanoTime();
    }

    public void tick(String message, Object... args) {
        if (isEnabled()) {
            long time = tickInternal();
            String formattedMessage = MessageFormat.format(message, args);
            log("[T:" + myName + "] " + formattedMessage + " [" + TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS) + " ms]");
        }
    }

    private long tickInternal() {
        long tick = System.nanoTime();
        long time;
        if (myLastTick == 0) {
            time = tick - myStart;
        } else {
            time = tick - myLastTick;
        }
        myLastTick = tick;
        return time;
    }

    public void stop() {
        if (isEnabled()) {
            long time = stopInternal();
            log("[" + myName + "] stopped [" + TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS) + " ms]");
        }
    }

    private long stopInternal() {
        long time = System.nanoTime() - myStart;
        myStart = 0;
        myLastTick = 0;
        return time;
    }

    protected abstract boolean isEnabled();

    protected abstract void log(String message);

    public static LogStopwatch debugStopwatch(Logger log, String name) {
        return new LogStopwatch(log, name) {
            @Override
            protected boolean isEnabled() {
                return LOG.isDebugEnabled();
            }

            @Override
            protected void log(String message) {
                LOG.debug(message);
            }
        };
    }
}
