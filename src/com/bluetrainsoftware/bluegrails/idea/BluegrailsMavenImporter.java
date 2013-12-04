package com.bluetrainsoftware.bluegrails.idea;

import com.intellij.facet.FacetManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.util.PairConsumer;
import org.jetbrains.idea.maven.importing.MavenImporter;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.server.MavenEmbedderWrapper;
import org.jetbrains.idea.maven.server.NativeMavenProjectHolder;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;
import org.jetbrains.plugins.grails.config.GrailsFramework;
import org.jetbrains.plugins.grails.util.GrailsFacetProvider;

import java.util.*;

public class BluegrailsMavenImporter extends MavenImporter {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.grails.BluegrailsMavenImporter");

  public BluegrailsMavenImporter() {
    super("com.bluetrainsoftware.bluegrails", "grails-maven-plugin");
  }


  @Override
  public void getSupportedDependencyTypes(Collection<String> result, SupportedRequestType type) {
    result.add("jar");
    result.add("grails-plugin2");
  }

	public void resolve(final Project project, final MavenProject mavenProject, final NativeMavenProjectHolder nativeMavenProject, final MavenEmbedderWrapper embedder, final ResolveContext context) throws MavenProcessCanceledException {
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

	public void collectSourceRoots(final MavenProject mavenProject, final PairConsumer<String, JpsModuleSourceRootType<?>> result) {
		for (final Map.Entry<JpsModuleSourceRootType<?>, Collection<String>> entry : GrailsFramework.GRAILS_SOURCE_FOLDERS.entrySet()) {
			final JpsModuleSourceRootType<?> type = entry.getKey();
			for (final String path : entry.getValue()) {
				result.consume(path, type);
			}
		}
	}

//  public void collectTestFolders(MavenProject mavenProject, List<String> result)
//  {
//    Collections.addAll(result, GrailsFramework.GRAILS_TEST_FOLDERS);
//  }
}
