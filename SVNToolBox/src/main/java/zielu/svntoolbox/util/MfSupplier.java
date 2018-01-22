/* 
 * $Id$
 */
package zielu.svntoolbox.util;

import java.text.MessageFormat;
import java.util.function.Supplier;

/**
 * <p></p>
 * <br/>
 * <p>Created on 10.03.14</p>
 *
 * @author Lukasz Zielinski
 */
public class MfSupplier implements Supplier<String> {
    private final String myFormat;
    private final Object[] myParams;

    public MfSupplier(String format, Object... params) {
        myFormat = format;
        myParams = params;
    }

    @Override
    public String get() {
        return MessageFormat.format(myFormat, myParams);
    }
}
