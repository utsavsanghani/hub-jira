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
package com.blackducksoftware.integration.jira.task.setup;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class HubAvatars {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private Map<String, Avatar> avatars = new HashMap<>();

    private final JiraServices jiraServices;

    private final ApplicationUser jiraUser;

    public HubAvatars(JiraServices jiraServices, ApplicationUser jiraUser) {
        this.jiraServices = jiraServices;
        this.jiraUser = jiraUser;
    }

    public Long getAvatarId(String issueTypeName) {
        logger.debug("Getting avatar id for issue type: " + issueTypeName);
        Avatar blackDuckAvatar;
        if (avatars.containsKey(issueTypeName)) {
            Long avatarId = avatars.get(issueTypeName).getId();
            logger.debug("Returning avatar ID from cache: " + avatarId);
            return avatarId;
        }
        try {
            blackDuckAvatar = createBlackDuckAvatar(issueTypeName);
        } catch (DataAccessException | IOException e) {
            logger.error("Error creating Black Duck avatar. ", e);
            return jiraServices.getAvatarManager().getAnonymousAvatarId();
        }
        logger.debug("Successfully created Black Duck Avatar with ID: " + blackDuckAvatar.getId());
        avatars.put(issueTypeName, blackDuckAvatar);
        return blackDuckAvatar.getId();
    }

    private Avatar createBlackDuckAvatar(String issueTypeName) throws DataAccessException, IOException {
        logger.debug("Creating avatar for issue type: " + issueTypeName);

        String avatarImagePath = getAvatarImagePath(issueTypeName);
        String avatarImageFilename = getAvatarImageFilename(issueTypeName);

        logger.debug("Loading Black Duck avatar from " + avatarImagePath);

        logger.debug("Creating avatar template");
        final Avatar avatarTemplate = jiraServices.createIssueTypeAvatarTemplate(
                avatarImageFilename,
                "image/png", jiraUser.getKey());
        if (avatarTemplate == null) {
            logger.debug("jiraServices.createIssueTypeAvatarTemplate() returned null");
            return null;
        }

        InputStream is = ClassLoaderUtils.getResourceAsStream(avatarImagePath, this.getClass());
        final Avatar duckyAvatar = jiraServices.getAvatarManager().create(avatarTemplate,
                is, null);

        if (duckyAvatar == null) {
            throw new DataAccessException("AvatarManager().create() returned null");
        }
        logger.debug("Created Avatar " + duckyAvatar.getFileName() + " with ID " + duckyAvatar.getId());
        return duckyAvatar;
    }

    private String getAvatarImagePath(String issueTypeName) {
        if (HubJiraConstants.HUB_VULNERABILITY_ISSUE.equals(issueTypeName)) {
            return HubJiraConstants.BLACKDUCK_AVATAR_IMAGE_PATH_VULNERABILITY;
        } else {
            return HubJiraConstants.BLACKDUCK_AVATAR_IMAGE_PATH_POLICY;
        }
    }

    private String getAvatarImageFilename(String issueTypeName) {
        if (HubJiraConstants.HUB_VULNERABILITY_ISSUE.equals(issueTypeName)) {
            return HubJiraConstants.BLACKDUCK_AVATAR_IMAGE_FILENAME_VULNERABILITY;
        } else {
            return HubJiraConstants.BLACKDUCK_AVATAR_IMAGE_FILENAME_POLICY;
        }
    }
}
