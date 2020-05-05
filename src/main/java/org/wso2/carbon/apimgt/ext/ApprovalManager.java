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

    static boolean isPriorApproved(String username, String wfType) throws SQLException {
        final String sqlQuery = "SELECT 1 FROM AM_ONE_TIME_APPROVALS WHERE USER_NAME = ? AND WF_TYPE = ?";
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            connection.setAutoCommit(false);
            connection.commit();

            statement.setString(1, username);
            statement.setString(2, wfType);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    static void recordApproval(String username, String wfType) throws SQLException {
        final String sqlQuery = "INSERT INTO AM_ONE_TIME_APPROVALS (USER_NAME, WF_TYPE)  VALUES (?,?)";

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            connection.setAutoCommit(false);

            statement.setString(1, username);
            statement.setString(2, wfType);

            statement.executeUpdate();

            connection.commit();
        }
    }
}
