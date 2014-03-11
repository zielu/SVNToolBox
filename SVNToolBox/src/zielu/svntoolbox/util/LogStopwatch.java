/* 
 * $Id$
 */
package zielu.svntoolbox.util;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
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
    private final Supplier<String> myNameSupplier;
    private final Optional<Supplier<Integer>> mySequence;
    private final Stopwatch myStopwatch = new Stopwatch();
    
    private String myName;
    
    protected LogStopwatch(Logger log, Optional<Supplier<Integer>> sequence, Supplier<String> nameSupplier) {
        LOG = log;
        myNameSupplier = nameSupplier;
        mySequence = sequence;
    }

    public LogStopwatch start() {
        if (isEnabled()) {
            myStopwatch.start();
            myName = myNameSupplier.get();
        }
        return this;
    }

    private String prepareName(String prefix) {
        return "["+prefix+":" + myName + (mySequence.isPresent() ? "|"+mySequence.get().get() : "") +"]";            
    }
    
    public void tick(String message, Object... args) {
        if (isEnabled()) {
            long time = myStopwatch.elapsed(TimeUnit.MILLISECONDS);
            String formattedMessage = MessageFormat.format(message, args);
            log(prepareName("T") + " " + formattedMessage + " [" + time + " ms]");
        }
    }

    public void stop() {
        if (isEnabled()) {
            myStopwatch.stop();
            log(prepareName("S") + " stopped [" + myStopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms]");
        }
    }

    protected abstract boolean isEnabled();

    protected abstract void log(String message);

    private static class DebugStopwatch extends LogStopwatch {

        protected DebugStopwatch(Logger log, Optional<Supplier<Integer>> sequence, Supplier<String> nameSupplier) {
            super(log, sequence, nameSupplier);
        }

        @Override
        protected boolean isEnabled() {
            return LOG.isDebugEnabled();
        }

        @Override
        protected void log(String message) {
            LOG.debug(message);
        }
    }
    
    public static LogStopwatch debugStopwatch(Logger log, Supplier<String> nameSupplier) {
        return new DebugStopwatch(log, Optional.<Supplier<Integer>>absent(), nameSupplier);
    }
    
    public static LogStopwatch debugStopwatch(Logger log, Supplier<Integer> sequence, Supplier<String> nameSupplier) {
        return new DebugStopwatch(log, Optional.of(sequence), nameSupplier); 
    }
}
