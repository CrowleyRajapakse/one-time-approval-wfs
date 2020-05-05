/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.ext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notification.NotifierConstants;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.SubscriptionCreationSimpleWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.SubscriptionCreationWSWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;

import java.sql.SQLException;


public class OneTimeSubscriptionApproval extends SubscriptionCreationWSWorkflowExecutor {
    private static final Log log = LogFactory.getLog(OneTimeSubscriptionApproval.class);

    private String fromEmailAddress;
    private  String fromEmailPassword;

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        SubscriptionWorkflowDTO subsWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        String subscriber = subsWorkflowDTO.getSubscriber();

        try {
            if (ApprovalManager.isPriorApproved(subscriber, WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION)) {
                SubscriptionCreationSimpleWorkflowExecutor simpleExecutor = new SubscriptionCreationSimpleWorkflowExecutor();
                return simpleExecutor.execute(workflowDTO);
            }
        } catch (SQLException e) {
            throw new WorkflowException("Error while checking if user is prior approved for subscription in DB", e);
        }

        return super.execute(workflowDTO);
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        WorkflowResponse response = super.complete(workflowDTO);

        SubscriptionWorkflowDTO subsWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        String subscriber = subsWorkflowDTO.getSubscriber();

        try {
            ApprovalManager.recordApproval(subscriber, WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
        } catch (SQLException e) {
            throw new WorkflowException("Error while recording that user is approved for subscription in DB", e);
        }

        String toEmail = getSubscriberEmail(subscriber);

        if (toEmail != null && !toEmail.isEmpty()) {
            String api = subsWorkflowDTO.getApiName() + "-" + subsWorkflowDTO.getApiVersion();
            String subject = "You have successfully subscribed to " + api;
            StringBuilder content = new StringBuilder();
            content.append(subject)
                    .append("\n\n")
                    .append("Please access the developer portal to generate keys in order to start consuming the API")
                    .append("\n\n")
                    .append("To report issues or engage with the community please https://github.com/" + api);
            EmailSender.sendEmail(toEmail, getfromEmailAddress(), getFromEmailPassword(), subject, content.toString());
        }

        return response;
    }



    private String getSubscriberEmail(String username) {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();

        JWTConfigurationDto jwtConfigurationDto = configuration.getJwtConfigurationDto();

        try {
            ClaimsRetriever claimsRetriever = (ClaimsRetriever) APIUtil.getClassForName(
                    jwtConfigurationDto.getClaimRetrieverImplClass()).newInstance();
            claimsRetriever.init();
            return claimsRetriever.getClaims(username).get(NotifierConstants.EMAIL_CLAIM);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | APIManagementException e) {
            log.error("Error while getting subscriber email", e);
        }

        return "";
    }

    public String getfromEmailAddress() {
        return fromEmailAddress;
    }
    public void setFromEmailAddress(String emailAddress) {
        this.fromEmailAddress = emailAddress;
    }
    public String getFromEmailPassword() {
        return fromEmailPassword;
    }
    public void setFromEmailPassword(String emailPassword) {
        this.fromEmailPassword = emailPassword;
    }
}
