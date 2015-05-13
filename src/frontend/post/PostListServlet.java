package frontend.post;

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

public class PostListServlet extends HttpServlet {
    private Logger logger = LogManager.getLogger(PostListServlet.class.getName());

    private MySqlConnect mySqlServer;

    public PostListServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(LoggerHelper.start());

        short status = ErrorMessages.ok;
        String message = "";

        String forum = request.getParameter("forum");
        String thread_str = request.getParameter("thread");
        String asc = request.getParameter("order");
        String since = request.getParameter("since");
        String limit = request.getParameter("limit");

        if(forum == null && thread_str == null) {
            status = ErrorMessages.wrongData;
            message = ErrorMessages.wrongJSONData();
        }

        StringBuilder query = new StringBuilder();
        ResultSet resultSet = null;
        Statement statement = mySqlServer.getStatement();
        if (status == ErrorMessages.ok) {
            if(forum == null) {
                query
                        .append("select id from post ")
                        .append("where thread = ")
                        .append(Integer.parseInt(thread_str)).append(" ");
                CommonHelper.appendDateAndAscAndLimit(query, since, asc, limit);

                resultSet = mySqlServer.executeSelect(query.toString(), statement);
            } else {
                int forumId = mySqlServer.getForumIdByShortName(forum);
                if (forumId > 0) {
                    query.delete(0, query.length());
                    query.append("select id from post ")
                            .append("where forum_id = ")
                            .append(forumId).append(" ");
                    CommonHelper.appendDateAndAscAndLimit(query, since, asc, limit);
                    resultSet = mySqlServer.executeSelect(query.toString(), statement);
                } else {
                    status = ErrorMessages.noRequestedObject;
                    message = ErrorMessages.noForum();
                }
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
        JSONArray listPosts = new JSONArray();

        if (status != ErrorMessages.ok || resultSet == null) {
            obj.put("response", message);
        } else {
            while (resultSet.next()) {
                listPosts.add(mySqlServer.getPostDetails(resultSet.getInt("id"), false, false, false));
            }
            obj.put("response", listPosts);
        }
        obj.put("code", status);
        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}