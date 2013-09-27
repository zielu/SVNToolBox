/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * <p></p>
 * <br/>
 * <p>Created on 27.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class SvnToolBoxBundle {
    private static Reference<ResourceBundle> ourBundle;

    @NonNls
    private static final String BUNDLE = "zielu.svntoolbox.SvnToolBoxBundle";

    private SvnToolBoxBundle() {
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return CommonBundle.message(getBundle(), key, params);
    }

    public static String getString(@PropertyKey(resourceBundle = BUNDLE) String key) {
        return getBundle().getString(key);
    }

    private static ResourceBundle getBundle() {
        ResourceBundle bundle = null;
        if (ourBundle != null) {
            bundle = ourBundle.get();
        }
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            ourBundle = new SoftReference<ResourceBundle>(bundle);
        }
        return bundle;
    }
}
