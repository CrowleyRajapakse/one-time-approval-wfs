package org.wso2.carbon.apimgt.ext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.workflow.ApplicationRegistrationSimpleWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.ApplicationRegistrationWSWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;

public class OneTimeApplicationRegistrationApproval extends ApplicationRegistrationWSWorkflowExecutor {
    private static final Log log = LogFactory.getLog(OneTimeApplicationRegistrationApproval.class);

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        ApplicationRegistrationWorkflowDTO appRegWorkflowDTO = (ApplicationRegistrationWorkflowDTO) workflowDTO;
        String subscriber = appRegWorkflowDTO.getUserName();

        if (ApprovalManager.isPriorApproved(subscriber, WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION)) {
            ApplicationRegistrationSimpleWorkflowExecutor simpleExecutor = new ApplicationRegistrationSimpleWorkflowExecutor();
            return simpleExecutor.execute(workflowDTO);
        }

        return super.execute(workflowDTO);
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        WorkflowResponse response = super.complete(workflowDTO);

        ApplicationRegistrationWorkflowDTO appRegWorkflowDTO = (ApplicationRegistrationWorkflowDTO) workflowDTO;
        String subscriber = appRegWorkflowDTO.getUserName();

        ApprovalManager.recordApproval(subscriber, WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);

        return response;
    }
}
