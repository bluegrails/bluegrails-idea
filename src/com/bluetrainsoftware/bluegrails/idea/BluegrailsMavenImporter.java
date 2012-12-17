package com.bluetrainsoftware.bluegrails.idea;

import com.intellij.facet.FacetManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.util.Consumer;
import com.intellij.util.containers.CollectionFactory;
import gnu.trove.THashSet;

import java.util.*;

import org.jetbrains.idea.maven.importing.MavenImporter;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectChanges;
import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
import org.jetbrains.idea.maven.project.MavenProjectsTree;
import org.jetbrains.idea.maven.server.MavenEmbedderWrapper;
import org.jetbrains.idea.maven.server.NativeMavenProjectHolder;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;
import org.jetbrains.plugins.grails.config.GrailsFramework;
import org.jetbrains.plugins.grails.util.GrailsFacetProvider;

public class BluegrailsMavenImporter extends MavenImporter {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.grails.BluegrailsMavenImporter");

  public BluegrailsMavenImporter() {
    super("com.bluetrainsoftware.bluegrails", "grails-maven-plugin");
  }

  
  
  public void resolve(Project project, MavenProject mavenProject, NativeMavenProjectHolder nativeMavenProject, MavenEmbedderWrapper embedder)
    throws MavenProcessCanceledException
  {
    // there is nothing to do here, what is imported is correct
  }

  public void preProcess(Module module, MavenProject mavenProject, MavenProjectChanges changes, MavenModifiableModelsProvider modifiableModelsProvider)
  {
  }

  public void process(MavenModifiableModelsProvider modifiableModelsProvider, Module module, MavenRootModelAdapter rootModel, MavenProjectsTree mavenModel, MavenProject mavenProject, MavenProjectChanges changes, Map<MavenProject, String> mavenProjectToModuleName, List<MavenProjectsProcessorTask> postTasks)
  {
    FacetManager facetManager = FacetManager.getInstance(module);

    List<Runnable> actions = new ArrayList<Runnable>();

    for (GrailsFacetProvider provider : (GrailsFacetProvider[])GrailsFacetProvider.EP_NAME.getExtensions()) {
      provider.addFacets(actions, facetManager, module, Collections.singletonList(mavenProject.getDirectoryFile()));
    }

    for (Runnable action : actions)
      action.run();
  }

  public void collectSourceFolders(MavenProject mavenProject, List<String> result)
  {
    Collections.addAll(result, GrailsFramework.GRAILS_SOURCE_FOLDERS);
  }

  public void collectTestFolders(MavenProject mavenProject, List<String> result)
  {
    Collections.addAll(result, GrailsFramework.GRAILS_TEST_FOLDERS);
  }
}
