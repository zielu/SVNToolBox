/*
 * $Id$
 */
package zielu.svntoolbox.util;

import static com.intellij.openapi.diagnostic.Logger.getInstance;

import com.google.common.base.Stopwatch;
import com.intellij.openapi.diagnostic.Logger;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

/**
 * <p></p>
 * <br/>
 * <p>Created on 23.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public abstract class LogStopwatch {
  private static final Logger LOG = getInstance("#zielu.svntoolbox.perf");
  ;
  private final Supplier<String> myNameSupplier;
  @Nullable
  private final Supplier<Integer> mySequence;
  private final Stopwatch myStopwatch = Stopwatch.createUnstarted();

  private String myName;

  protected LogStopwatch(@Nullable Supplier<Integer> sequence, Supplier<String> nameSupplier) {
    myNameSupplier = nameSupplier;
    mySequence = sequence;
  }

  public static LogStopwatch debugStopwatch(Supplier<String> nameSupplier) {
    return new DebugStopwatch(null, nameSupplier);
  }

  public static LogStopwatch debugStopwatch(Supplier<Integer> sequence, Supplier<String> nameSupplier) {
    return new DebugStopwatch(sequence, nameSupplier);
  }

  public LogStopwatch start() {
    if (isEnabled()) {
      myStopwatch.start();
      myName = myNameSupplier.get();
    }
    return this;
  }

  private String prepareName(String prefix) {
    return "[" + prefix + ":" + myName + (mySequence != null ? "|" + mySequence.get() : "") + "]";
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

    protected DebugStopwatch(@Nullable Supplier<Integer> sequence, Supplier<String> nameSupplier) {
      super(sequence, nameSupplier);
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
}
