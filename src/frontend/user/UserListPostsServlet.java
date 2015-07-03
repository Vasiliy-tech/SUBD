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
import java.sql.SQLException;
import java.sql.Statement;

import static helper.ErrorMessages.ok;
import static helper.LoggerHelper.*;


public class UserListPostsServlet extends HttpServlet {
    private Logger logger = LogManager.getLogger(UserListPostsServlet.class.getName());

    private MySqlConnect mySqlServer;

    public UserListPostsServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(start());

        short status = ok;
        String message = "";
        ResultSet resultSet = null;
        Statement statement = mySqlServer.getStatement();
        String date = request.getParameter("since");
        String limit = request.getParameter("limit");
        String asc = request.getParameter("order");
        long userId = mySqlServer.getUserIdByEmail(request.getParameter("user"));
        String query = "select post.id from post " +
                "where forum_id = forum_id and author_id = " + userId + " " +
                (date != null ? ("and date_of_creating > '" + date + "' ") : "") +
                "order by date_of_creating " +
                (asc == null ? ("desc ") : asc + " ") +
                (limit != null ? ("limit " + limit) : "") +
                ";";
        resultSet = mySqlServer.executeSelect(query, statement);
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
        JSONArray postList = new JSONArray();
        JSONObject post;
        if (resultSet != null) {
            while (resultSet.next()) {
                post = mySqlServer.getPostDetails(resultSet.getInt("id"), false, false, false);
                postList.add(post);
            }
        } else {
            status = ErrorMessages.noRequestedObject;
            message = ErrorMessages.noPost();
        }
        JSONObject obj = new JSONObject();

        obj.put("response", status == ErrorMessages.ok? postList: message);
        obj.put("code", status);

        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}