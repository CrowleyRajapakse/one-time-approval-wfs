# One time approval work flows

One time approval workflows are a hybrid of WS Workflow executors, and Simple Workflow executors 
that are executed conditionally. The initial call to the respective workflow, by a given user, 
will require approval. Subsequent calls to the respective workflow by the same user will be completed
without requiring approval. The initial approval will be recorded in a custom database table 
for a given user/wf type combination. Currently, this is supported for the 
API Subscription and API Key Generation(App registration) work flows, in the following classes,

    OneTimeSubscriptionApproval
    OneTimeApplicationRegistrationApproval

# Pre-requisites
- Assuming the database used is MySQL, execute the following statement to create the
 AM_ONE_TIME_APPROVALS table in the AM Database.

```
CREATE TABLE IF NOT EXISTS AM_ONE_TIME_APPROVALS (
            USER_NAME VARCHAR(45),
            WF_TYPE VARCHAR(45),
            PRIMARY KEY (USER_NAME, WF_TYPE)
)ENGINE INNODB;
```

- Run mvn clean install and copy the `one-time-approval-wfs-1.0.0.jar` to <APIM_HOME>/repository/components/lib

- For configuring the one time API Subscription workflow configure as below in /_system/governance/apimgt/applicationdata/workflow-extensions.xml

```
<WorkFlowExtensions>
    ...
        <SubscriptionCreation executor="org.wso2.carbon.apimgt.impl.workflow.SubscriptionCreationWSWorkflowExecutor">
             <Property name="serviceEndpoint">http://localhost:9765/services/SubscriptionApprovalWorkFlowProcess/</Property>
             <Property name="username">admin</Property>
             <Property name="password">admin</Property>
             <Property name="callbackURL">https://localhost:8243/services/WorkflowCallbackService</Property>
             <Property name="fromEmailAddress">wsocep@gmail.com</Property>
             <Property name="fromEmailPassword">wso2cep_poc</Property>
        </SubscriptionCreation>
    ...
</WorkFlowExtensions>
```

- For configuring the one time Key Generation(App Registration) workflow configure as below in /_system/governance/apimgt/applicationdata/workflow-extensions.xml

```
<WorkFlowExtensions>
    ...
    <ProductionApplicationRegistration executor="org.wso2.carbon.apimgt.ext.OneTimeApplicationRegistrationApproval">
        <Property name="serviceEndpoint">http://localhost:9765/services/ApplicationRegistrationWorkFlowProcess/</Property>
        <Property name="username">admin</Property>
        <Property name="password">admin</Property>
        <Property name="callbackURL">https://localhost:8248/services/WorkflowCallbackService</Property>
    </ProductionApplicationRegistration>
    ...
</WorkFlowExtensions>

```