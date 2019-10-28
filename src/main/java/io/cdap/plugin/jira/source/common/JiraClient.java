/*
 * Copyright © 2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.jira.source.common;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.collect.ImmutableSet;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * A class which is used to communicate to Jira API based on the plugin configurations.
 */
public class JiraClient implements Closeable {
  /**
   * You have to always query these fields or else the searchJql method will fail.
   */
  private static final ImmutableSet<String> MINIMAL_FIELDS_SET = ImmutableSet.of("project", "summary", "issuetype",
                                                                                 "created", "updated", "status");

  private final JiraRestClient restClient;
  private final BaseJiraSourceConfig config;

  public JiraClient(BaseJiraSourceConfig config) {
    this.config = config;

    AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
    try {
      URI jiraUrl = new URI(config.getJiraUrl());

      if (config.useBasicAuthentication()) {
        restClient = factory.createWithBasicHttpAuthentication(jiraUrl, config.getUsername(), config.getPassword());
      } else {
        restClient = factory.create(jiraUrl, new AnonymousAuthenticationHandler());
      }
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(String.format("Invalid URI '%s'", config.getJiraUrl()), e);
    }
  }

  public SearchResult getSearchResult(int startAt) {
    return restClient.getSearchClient().searchJql(getJQLQuery(), config.getMaxIssuesPerRequest(),
                                                startAt, null).claim();
  }

  public int getResultsCount() {
    return restClient.getSearchClient().searchJql(getJQLQuery(), null, null,
                                                  MINIMAL_FIELDS_SET).claim().getTotal();
  }

  public String getJQLQuery() {
    switch (config.getFilterMode()) {
      case JQL:
        return config.getJqlQuery();
      case JIRA_FILTER_ID:
        return restClient.getSearchClient().getFilter(config.getJiraFilterId()).claim().getJql();
      case BASIC:
        return getBasicQuery();
      default:
        throw new IllegalArgumentException(String.format("Unsupported filter mode: '%s'", config.getFilterMode()));
    }
  }

  private String getBasicQuery() {
    StringBuilder sb = new StringBuilder();

    List<String> projects = config.getProjects();
    if (projects != null) {
      sb.append("project IN ");
      sb.append(listToString(projects));
      sb.append(" AND ");
    }

    List<String> issueTypes = config.getIssueTypes();
    if (issueTypes != null) {
      sb.append("issuetype IN ");
      sb.append(listToString(issueTypes));
      sb.append(" AND ");
    }

    List<String> statuses = config.getStatuses();
    if (statuses != null) {
      sb.append("status IN ");
      sb.append(listToString(statuses));
      sb.append(" AND ");
    }

    List<String> priorities = config.getPriorities();
    if (priorities != null) {
      sb.append("priority IN ");
      sb.append(listToString(priorities));
      sb.append(" AND ");
    }

    List<String> reporters = config.getReporters();
    if (reporters != null) {
      sb.append("reporter IN ");
      sb.append(listToString(reporters));
      sb.append(" AND ");
    }

    List<String> assignees = config.getAssignees();
    if (assignees != null) {
      sb.append("assignee IN ");
      sb.append(listToString(assignees));
      sb.append(" AND ");
    }

    List<String> fixVersions = config.getFixVersions();
    if (fixVersions != null) {
      sb.append("fixVersion IN ");
      sb.append(listToString(fixVersions));
      sb.append(" AND ");
    }

    List<String> affectedVersions = config.getAffectedVersions();
    if (affectedVersions != null) {
      sb.append("affectedVersion IN ");
      sb.append(listToString(affectedVersions));
      sb.append(" AND ");
    }

    List<String> labels = config.getLabels();
    if (labels != null) {
      sb.append("labels IN ");
      sb.append(listToString(labels));
      sb.append(" AND ");
    }

    String lastUpdateStartDate = config.getLastUpdateStartDate();
    if (lastUpdateStartDate != null) {
      sb.append("updatedDate >= '");
      sb.append(lastUpdateStartDate);
      sb.append("'");
      sb.append(" AND ");
    }

    String lastUpdateEndDate = config.getLastUpdateEndDate();
    if (lastUpdateEndDate != null) {
      sb.append("updatedDate <= '");
      sb.append(lastUpdateEndDate);
      sb.append("'");
      sb.append(" AND ");
    }

    if (sb.length() != 0) {
      sb.setLength(sb.length() - 5); // remove last ' AND '
    }

    return sb.toString();
  }

  private String listToString(List<String> list) {
    StringBuilder sb = new StringBuilder();

    sb.append("(");
    for (String item : list) {
      sb.append("'");
      sb.append(item);
      sb.append("',");
    }

    if (!list.isEmpty()) {
      sb.setLength(sb.length() - 1); // remove last comma
    }
    sb.append(")");

    return sb.toString();
  }

  @Override
  public void close() {
    try {
      restClient.close();
    } catch (IOException e) {
      throw new RuntimeException("Cannot close jira client", e);
    }
  }
}
