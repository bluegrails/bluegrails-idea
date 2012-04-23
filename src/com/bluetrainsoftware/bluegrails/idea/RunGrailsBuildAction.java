package com.bluetrainsoftware.bluegrails.idea;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenDataKeys;
import org.jetbrains.idea.maven.utils.actions.MavenAction;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import java.util.List;
import java.util.Map;

public class RunGrailsBuildAction extends MavenAction {
  @Override
  protected boolean isAvailable(AnActionEvent e) {
    return super.isAvailable(e) && checkOrPerform(e.getDataContext(), false);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    checkOrPerform(e.getDataContext(), true);
  }

  private static boolean checkOrPerform(DataContext context, boolean perform) {
    final MavenProject project = MavenActionUtil.getMavenProject(context);
    if (project == null) return false;

    final List<String> goals = MavenDataKeys.MAVEN_GOALS.getData(context);
    if (goals == null || goals.isEmpty()) return false;

    if (!perform) return true;

    final MavenRunnerParameters params = new MavenRunnerParameters(true,
      project.getDirectory(),
      goals,
      MavenActionUtil.getProjectsManager(context).getExplicitProfiles());

    Project ideaProject = MavenActionUtil.getProject(context);
    MavenProjectsManager projectsManager = MavenProjectsManager.getInstance(ideaProject);

    MavenRunnerSettings settings = MavenRunner.getInstance(ideaProject).getSettings().clone();

    for( MavenProject proj : projectsManager.getProjects() ) {
      if ("grails-plugin".equals(proj.getPackaging())) {
        Map<String,String> model = proj.getModelMap();
        settings.getMavenProperties().put(model.get("groupId") + ":" + model.get("artifactId"), proj.getDirectory());
      }
    }

    MavenRunConfigurationType.runConfiguration(ideaProject, params, MavenProjectsManager.getInstance(ideaProject).getGeneralSettings(), settings, null);

    return true;
  }
}
