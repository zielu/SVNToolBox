/* 
 * $Id$
 */
package zielu.svntoolbox.util;

import java.util.concurrent.TimeUnit;

import com.intellij.openapi.diagnostic.Logger;

/**
 * <p></p>
 * <br/>
 * <p>Created on 23.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public abstract class LogStopwatch {
    protected final Logger LOG;
    private final String name;
    private long start;
    private long lastTick;
    
    protected LogStopwatch(Logger log, String name) {
        LOG = log;
        this.name = name;
    }
    
    public LogStopwatch start() {
        if (isEnabled()) {
            startInternal();                        
        }
        return this;
    }
    
    private void startInternal() {
        start = System.nanoTime();        
    }
    
    public void tick(String message) {
        if (isEnabled()) {
            long time = tickInternal();                     
            log("[T:" + name + "] " + message + " [" + TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS) + " ms]");
        }
    }
    
    private long tickInternal() {
        long tick = System.nanoTime();
        long time;
        if (lastTick == 0) {
            time = tick - start;                
        } else {
            time = tick - lastTick;    
        }
        lastTick = tick;
        return time;
    }
    
    public void stop() {
        if (isEnabled()) {
            long time = stopInternal();
            log("[" + name + "] stopped [" + TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS) + " ms]");            
        }
    }

    private long stopInternal() {
        long time = System.nanoTime() - start;
        start = 0;
        lastTick = 0;
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
