/* 
 * $Id$
 */
package zielu.svntoolbox.branch;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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
public class TrunkSpecification {
    private final String trunkUrl;    
    private final Multimap<String, Module> branches = HashMultimap.create();
    private final Set<Module> modules = Sets.newHashSet();
    
    public TrunkSpecification(String trunkUrl) {
        this.trunkUrl = trunkUrl;        
    }
    
    public String getTrunkUrl() {
        return trunkUrl;
    }
    
    public void add(Module module, SvnBranchConfigurationNew branchConfig) {
        for (String branch : branchConfig.getBranchUrls()) {
            branches.put(branch, module);
        }
        modules.add(module);
    }
    
    public List<String> getBranchUrlsSharedByAllModules() {
        List<String> branchUrls = Lists.newArrayListWithCapacity(branches.size());
        for (Map.Entry<String, Collection<Module>> entry : branches.asMap().entrySet()) {
            if (entry.getValue().containsAll(modules)) {
                branchUrls.add(entry.getKey());
            }
        }
        return branchUrls;
    }
}
