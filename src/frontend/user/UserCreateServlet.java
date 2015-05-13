package frontend.user;

import helper.CommonHelper;
import helper.ErrorMessages;
import helper.LoggerHelper;
import helper.Validator;
import mysql.MySqlConnect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static helper.ErrorMessages.*;
import static helper.LoggerHelper.resultUpdate;
import static main.JsonInterpreterFromRequest.getJSONFromRequest;

public class UserCreateServlet extends HttpServlet {
    private Logger logger = LogManager.getLogger(UserCreateServlet.class.getName());

    private MySqlConnect mySqlServer;

    public UserCreateServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(LoggerHelper.start());
        JSONObject req = getJSONFromRequest(request, "UserCreateServlet");
        boolean isAnonymous = false;

        if (req.containsKey("isAnonymous")) {
            isAnonymous = (boolean) req.get("isAnonymous");
        }

        short status = ErrorMessages.ok;
        String message = "";

        if (!Validator.userValidation(req)) {
            status = wrongData;
            message = wrongJSONData();
        }

        ResultSet resultSet = null;
        Statement statement = mySqlServer.getStatement();
        if (status == ErrorMessages.ok) {
            int result;
            StringBuilder query = new StringBuilder();
            query.append("insert into users set email='")
                    .append(req.get("email")).append("', ")
                    .append("username='").append(req.get("username")).append("', ")
                    .append("name='").append(req.get("name")).append("', ")
                    .append("isAnonymous=").append(isAnonymous ? "1" : "0").append(", ")
                    .append("about='").append(req.get("about"))
                    .append("';");
            result = mySqlServer.executeUpdate(query.toString());
            logger.info(resultUpdate(), result);
            if (result != 1) {
                status = suchUserAlreadyExist;
                message = userAlreadyExist();
            } else {
                String queryStr = "select * from users where email = '" + req.get("email") + "';";
                resultSet = mySqlServer.executeSelect(queryStr, statement);
            }
        }
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
        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        if (status == ErrorMessages.ok) {
            resultSet.next();
            data.put("isAnonymous", resultSet.getBoolean("isAnonymous"));
            data.put("email", resultSet.getString("email"));
            data.put("about", resultSet.getString("about"));
            data.put("name", resultSet.getString("name"));
            data.put("username", resultSet.getString("username"));
            data.put("id", resultSet.getInt("id"));
        } else {
            data.put("error", message);
        }
        obj.put("response", data);
        obj.put("code", status);
        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}