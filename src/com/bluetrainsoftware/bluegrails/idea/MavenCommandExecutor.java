package com.bluetrainsoftware.bluegrails.idea;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.xmlb.XmlSerializer;

import java.util.Collections;
import java.util.Map;
import javax.swing.JComponent;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenExternalParameters;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenGeneralConfigurable;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.plugins.grails.runner.GrailsRunConfiguration;
import org.jetbrains.plugins.groovy.grails.GrailsCommandExecutor;

public class MavenCommandExecutor extends GrailsCommandExecutor {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.grails.MavenCommandExecutor");
  
  public boolean isApplicable(@NotNull Module module) {
    MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(module.getProject());

    MavenProject mavenProject = mavenProjectsManager.findProject(module);

    if (mavenProject != null) {
      return mavenProject.findPlugin("com.bluetrainsoftware.bluegrails", "grails-maven-plugin") != null;
    }

    return false;
  }

  public JavaParameters createJavaParameters(@NotNull Module module, boolean forCreation, @Nullable String jvmParams, @NotNull String command, @NotNull String[] args)
    throws ExecutionException {
    return createJavaParameters(module, jvmParams, null, command, args);
  }

  private static JavaParameters createJavaParameters(@NotNull Module module, @Nullable String jvmParams, @Nullable MavenGeneralSettings generalSettings, @NotNull String command, @NotNull String[] args)
    throws ExecutionException {
    MavenProjectsManager projectsManager = MavenProjectsManager.getInstance(module.getProject());
    MavenProject mavenProject = projectsManager.findProject(module);

    MavenRunnerParameters parameters = new MavenRunnerParameters(true, mavenProject.getDirectory(), Collections.singletonList("grails:exec"), projectsManager.getExplicitProfiles());

    MavenRunnerSettings settings = MavenRunner.getInstance(module.getProject()).getSettings().clone();
    settings.getMavenProperties().put("command", command);

    // go and find all grails plugins we have installed in the IDE and tell the Maven plugin about them.
    // This will cause it to indicate the installed location for that plugin is our project rather than unarchiving it
    // from the Maven dependencies.
    //
    // Plugins are inlined by the maven plugin as Grails 2.x deletes them on install as they are not in Ivy
    LOG.info("Looking for Grails Plugins to add to command line - total projects = " + projectsManager.getProjects().size());
    for( MavenProject proj : projectsManager.getProjects() ) {
      if ("grails-plugin".equals(proj.getPackaging())) {
        Map<String,String> model = proj.getModelMap();
        LOG.info("found " + model.get("groupId") + ":" + model.get("artifactId"));
        settings.getMavenProperties().put(model.get("groupId") + ":" + model.get("artifactId"), proj.getDirectory());
      }
    }

    if (args.length > 0) {
      ParametersList parametersList = new ParametersList();
      parametersList.addAll(args);

      settings.getMavenProperties().put("args", parametersList.getParametersString());
    }

    //  ideally if we are printing the grails settings we want to NOT use the Agent, it is important the plugin provides the feedback about where the plugins are located.

    if (jvmParams != null) {
      settings.setVmOptions(jvmParams);
    }

    JavaParameters res = MavenExternalParameters.createJavaParameters(module.getProject(), parameters, generalSettings == null ? projectsManager.getGeneralSettings() : generalSettings, settings);

    addCommonJvmOptions(res);

    return res;
  }

  public JavaParameters createJavaParametersForRun(@NotNull Module module, boolean classpathFromDependencies, @Nullable String jvmParams, @Nullable Object additionalConfiguration, @NotNull String command, @NotNull String[] args)
    throws ExecutionException {
    return createJavaParameters(module, jvmParams, (MavenGeneralSettings) additionalConfiguration, command, args);
  }

  public void addListener(JavaParameters params, String listener) {
    super.addListener(params, listener);
    addAgentJar(params);
  }

  public Pair<String, SettingsEditor<GrailsRunConfiguration>> createSettingsEditor(@NotNull Project project) {
    return null;
//    return new Pair("Maven Settings", new MavenSettingsEditor(project, this));
  }

  public Object readAdditionalConfiguration(@NotNull Element element) {
    Element e = element.getChild(MavenGeneralSettings.class.getSimpleName());
    if (e != null) {
      return XmlSerializer.deserialize(e, MavenGeneralSettings.class);
    }

    return null;
  }

  public void writeAdditionalConfiguration(@NotNull Object cfg, @NotNull Element element) {
    MavenGeneralSettings settings = (MavenGeneralSettings) cfg;
    element.addContent(XmlSerializer.serialize(settings));
  }

  public Object cloneAdditionalConfiguration(@NotNull Object cfg) {
    return ((MavenGeneralSettings) cfg).clone();
  }
}
