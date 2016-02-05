package jetbrains.buildServer.issueTracker.github.health;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.issueTracker.IssueProvidersManager;
import jetbrains.buildServer.issueTracker.github.GitHubIssueProviderType;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.healthStatus.BuildTypeSuggestedItem;
import jetbrains.buildServer.vcs.VcsRootInstance;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.suggestions.BuildTypeSuggestion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class IssueTrackerSuggestion extends BuildTypeSuggestion {

  private static final String GIT_VCS_NAME = "jetbrains.git";
  private static final String GIT_FETCH_URL_PROPERTY = "url";

  /* Matches github ssh urls of format git@github.com:owner/repo.git */
  private static final Pattern sshPattern = Pattern.compile("git@github\\.com:(.+)/(.+)(?:\\.git)");

  /* Matches github http and https urls of format https://github.com/owner/repo.git */
  private static final Pattern httpsPattern = Pattern.compile("http[s]?://github\\.com/(.*)/(.*)(?:\\.git)");

  @NotNull
  private final String myViewUrl;

  @NotNull
  private final IssueProvidersManager myIssueProvidersManager;

  @NotNull
  private final GitHubIssueProviderType myType;

  public IssueTrackerSuggestion(@NotNull final PluginDescriptor descriptor,
                                @NotNull PagePlaces pagePlaces,
                                @NotNull final IssueProvidersManager issueProvidersManager,
                                @NotNull final GitHubIssueProviderType type) {
    super("addGitHubIssueTracker", "Suggest to add a GitHub Issue Tracker", pagePlaces);
    myType = type;
    myViewUrl = descriptor.getPluginResourcesPath("health/addGitHubIssueTracker.jsp");
    myIssueProvidersManager = issueProvidersManager;
  }

  @NotNull
  @Override
  public List<BuildTypeSuggestedItem> getSuggestions(@NotNull SBuildType buildType) {
    final SProject project = buildType.getProject();
    final String type = myType.getType();
    boolean alreadyUsed = myIssueProvidersManager.getProviders(project).values().stream().anyMatch(it -> it.getType().equals(type));
    final List<BuildTypeSuggestedItem> result = new ArrayList<>();
    if (!alreadyUsed) {
      final List<Map<String, Object>> results = buildType.getVcsRootInstances().stream()
              .filter(it -> GIT_VCS_NAME.equals(it.getVcsName()))
              .map(this::toSuggestion)
              .filter(Objects::nonNull)
              .collect(Collectors.toList());
      if (!results.isEmpty()) {
        result.add(new BuildTypeSuggestedItem(getType(), buildType, Collections.singletonMap("suggestedTrackers", results)));
      }
    }
    return result;
  }

  @Nullable
  private Map<String, Object> toSuggestion(@NotNull final VcsRootInstance instance) {
    Map<String, Object> result = null;
    final String fetchUrl = instance.getProperty(GIT_FETCH_URL_PROPERTY);
    if (!StringUtil.isEmptyOrSpaces(fetchUrl)) {
      Matcher m;
      boolean matched = false;
      m = sshPattern.matcher(fetchUrl);
      if (m.matches()) {
        result = getSuggestionMap(instance, m.group(1), m.group(2));
        matched = true;
      }
      if (!matched) {
        m = httpsPattern.matcher(fetchUrl);
        if (m.matches()) {
          result = getSuggestionMap(instance, m.group(1), m.group(2));
        }
      }
    }
    return result;
  }

  @NotNull
  private Map<String, Object> getSuggestionMap(@NotNull final VcsRootInstance instance,
                                               @NotNull final String owner,
                                               @NotNull final String repo) {
    final Map<String, Object> result = new HashMap<>();
    result.put("vcsRoot", instance);
    result.put("type", myType.getType());
    result.put("suggestedName", owner + "/" + repo);
    result.put("repoUrl", getRepoUrl(owner, repo));
    return result;
  }

  @NotNull
  private String getRepoUrl(@NotNull final String owner, @NotNull final String repo) {
    return "https://github.com/" + owner + "/" + repo;
  }

  @NotNull
  @Override
  public String getViewUrl() {
    return myViewUrl;
  }
}