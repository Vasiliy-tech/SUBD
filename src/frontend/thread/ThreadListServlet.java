package frontend.thread;

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
import java.sql.SQLException;
import java.sql.Statement;

import static helper.ErrorMessages.ok;
import static helper.LoggerHelper.*;

public class ThreadListServlet extends HttpServlet {

    private Logger logger = LogManager.getLogger(ThreadListServlet.class.getName());
    private MySqlConnect mySqlServer;

    public ThreadListServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(start());

        short status = ok;
        String message = "";

        String forum = request.getParameter("forum");
        String email = request.getParameter("user");
        String asc = request.getParameter("order");
        String since = request.getParameter("since");
        String limit = request.getParameter("limit");

        if (forum == null && email == null) {
            status = ErrorMessages.wrongData;
            message = ErrorMessages.wrongJSONData();
        }

        StringBuilder query = new StringBuilder();
        ResultSet resultSet;
        Statement statement = mySqlServer.getStatement();
        query
                .append("select id from thread ");
        if (forum != null) {
            int forumId = mySqlServer.getForumIdByShortName(forum);
            query.append("where forum_id = ").append(forumId).append(" ");
        } else {
            int authorId = mySqlServer.getUserIdByEmail(email);
            query.append("where founder_id = ").append(authorId).append(" ");
        }
        CommonHelper.appendDateAndAscAndLimit(query, since, asc, limit);

        resultSet = mySqlServer.executeSelect(query.toString(), statement);
        try {
            createResponse(response, status, message, resultSet);
        } catch (SQLException e) {
            logger.error(responseCreating());
            logger.error(e);
            e.printStackTrace();
        }

        mySqlServer.closeExecution(resultSet, statement);
        logger.info(finish());
    }

    private void createResponse(HttpServletResponse response, short status, String message, ResultSet resultSet) throws IOException, SQLException {
        CommonHelper.setResponse(response);
        JSONObject obj = new JSONObject();

        JSONArray listThreads = new JSONArray();

        if (status != ErrorMessages.ok || resultSet == null) {
            obj.put("response", message);
        } else {
            while (resultSet.next()) {
                listThreads.add(mySqlServer.getThreadDetailsById(resultSet.getInt("id"), false, false));
            }
            obj.put("response", listThreads);
        }
        obj.put("code", status);
        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}