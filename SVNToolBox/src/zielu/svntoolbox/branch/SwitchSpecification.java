/* 
 * $Id$
 */
package zielu.svntoolbox.branch;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.openapi.module.Module;
import org.jetbrains.idea.svn.branchConfig.SvnBranchConfigurationNew;

/**
 * <p></p>
 * <br/>
 * <p>Created on 16.01.14</p>
 *
 * @author Lukasz Zielinski
 */
public class SwitchSpecification {
    private final Map<String, TrunkSpecification> trunks = Maps.newHashMap(); 
    private final Set<Module> modules = Sets.newHashSet();
    
    public TrunkSpecification add(Module module, SvnBranchConfigurationNew branchConfig) {
        TrunkSpecification trunkSpec = trunks.get(branchConfig.getTrunkUrl());
        if (trunkSpec == null) {
            trunkSpec = new TrunkSpecification(branchConfig.getTrunkUrl());
            trunks.put(trunkSpec.getTrunkUrl(), trunkSpec);
        }
        trunkSpec.add(module, branchConfig);
        modules.add(module);
        return trunkSpec;
    }
    
     
}
