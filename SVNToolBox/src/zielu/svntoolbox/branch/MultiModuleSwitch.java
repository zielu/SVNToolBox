/* 
 * $Id$
 */
package zielu.svntoolbox.branch;

import java.util.Collection;

import com.google.common.base.Preconditions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.branchConfig.SvnBranchConfigurationNew;
import zielu.svntoolbox.FileStatusCalculator;
import zielu.svntoolbox.util.Svn;

/**
 * <p></p>
 * <br/>
 * <p>Created on 16.01.14</p>
 *
 * @author Lukasz Zielinski
 */
public class MultiModuleSwitch {
    
    public void getSwitchSpecification(@NotNull Project project, Collection<Module> modules) throws Exception {
        Preconditions.checkNotNull(project);
        Preconditions.checkNotNull(modules);

        FileStatusCalculator statusCalc = new FileStatusCalculator();
        SwitchSpecification spec = new SwitchSpecification();
        for (Module module : modules) {
            ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
            for (VirtualFile contentRoot : rootManager.getContentRoots()) {
                if (statusCalc.filesUnderSvn(project, contentRoot)) {
                    SvnBranchConfigurationNew config = Preconditions.checkNotNull(Svn.getBranchConfig(project, contentRoot));                    
                    spec.add(module, config.copy());
                }
            }
        }
    }         
    
    private class Branch2Config {
        private final Module module;
        private final SvnBranchConfigurationNew config;

        private Branch2Config(Module module, SvnBranchConfigurationNew config) {
            this.module = module;
            this.config = config;
        }
    }
    
    
}
