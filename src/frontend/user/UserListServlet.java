package frontend.user;

import helper.CommonHelper;
import helper.ErrorMessages;
import helper.LoggerHelper;
import mysql.MySqlConnect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;


public class UserListServlet extends HttpServlet {
    private Logger logger = LogManager.getLogger(UserListServlet.class.getName());

    private MySqlConnect mySqlServer;

    public UserListServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(LoggerHelper.start());
        short status = ErrorMessages.ok;
        String message = "";
        ResultSet resultSet = null;
        Statement statement = mySqlServer.getStatement();
        resultSet = mySqlServer.executeSelect("select * from users;", statement);
        try {
            createResponse(response, status, message, resultSet);
        } catch (SQLException e) {
            logger.error(LoggerHelper.responseCreating());
            logger.error(e);
            e.printStackTrace();
        }
        mySqlServer.closeExecution(resultSet, statement);
        logger.info(LoggerHelper.finish());
    }

    private void createResponse(HttpServletResponse response, short status, String message, ResultSet resultSet) throws IOException, SQLException {
        CommonHelper.setResponse(response);
        JSONArray userList = new JSONArray();
        JSONObject user;

        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while(resultSet.next()) {
            user = new JSONObject();

            int numberOfColumns = resultSetMetaData.getColumnCount();
            for (int i = 1; i <= numberOfColumns; ++i)
            {
                user.put(resultSetMetaData.getColumnName(i), resultSet.getString(i));
            }
            userList.add(user);
        }

        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        if (status != ErrorMessages.ok) {
            data.put("error", message);
        } else {
            data.put("userList", userList);
        }
        obj.put("response", data);
        obj.put("code", status);
        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}