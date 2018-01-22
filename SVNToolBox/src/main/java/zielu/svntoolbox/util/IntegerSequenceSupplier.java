/* 
 * $Id$
 */
package zielu.svntoolbox.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * <p></p>
 * <br/>
 * <p>Created on 11.03.14</p>
 *
 * @author Lukasz Zielinski
 */
public class IntegerSequenceSupplier implements Supplier<Integer> {
    private final AtomicInteger sequence = new AtomicInteger();
    
    @Override
    public Integer get() {
        return sequence.incrementAndGet();
    }
}
