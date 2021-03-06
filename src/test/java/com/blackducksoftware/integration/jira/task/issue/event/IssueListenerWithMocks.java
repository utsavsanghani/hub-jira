/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.jira.task.issue.event;

import java.util.concurrent.ExecutorService;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.issue.Issue;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.jira.mocks.issue.ExecutorServiceMock;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class IssueListenerWithMocks extends IssueEventListener {
    private final HubServicesFactory hubServicesFactory;

    public IssueListenerWithMocks(final EventPublisher eventPublisher, final PluginSettingsFactory pluginSettingsFactory, final JiraServices jiraServices, final HubServicesFactory hubServicesFactory) {
        super(eventPublisher, pluginSettingsFactory, jiraServices);
        this.hubServicesFactory = hubServicesFactory;
    }

    @Override
    public ExecutorService createExecutorService() {
        return new ExecutorServiceMock();
    }

    @Override
    public IssueTrackerTask createTask(final Issue issue, final Long eventTypeID, final JiraServices jiraServices, final PluginSettings settings, final String propertyKey, final EntityProperty property) {
        return new IssueTrackerTaskWithMocks(issue, eventTypeID, jiraServices, settings, propertyKey, property, this.hubServicesFactory);
    }
}
