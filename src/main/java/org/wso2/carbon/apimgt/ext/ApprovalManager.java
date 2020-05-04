package org.wso2.carbon.apimgt.ext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ApprovalManager {
    private static final Log log = LogFactory.getLog(ApprovalManager.class);

    static boolean isPriorApproved(String username, String wfType) {
        final String sqlQuery = "SELECT 1 FROM AM_ONE_TIME_APPROVALS WHERE USER_NAME = ? AND WF_TYPE = ?";
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, username);
            statement.setString(2, wfType);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            log.error("Error while checking prior approved status in DB for username: " + username +
                    " , workFlowType: " + wfType, e);
        }

        return false;
    }

    static void recordApproval(String username, String wfType) {
        final String sqlQuery = "INSERT INTO AM_ONE_TIME_APPROVALS (USER_NAME, WF_TYPE)  VALUES (?,?)";

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, username);
            statement.setString(2, wfType);

            statement.executeUpdate();

        } catch (SQLException e) {
            log.error("Error while recording approval in DB for username: " + username +
                    " , workFlowType: " + wfType, e);
        }
    }
}
