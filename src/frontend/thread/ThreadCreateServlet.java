package frontend.thread;

import helper.CommonHelper;
import helper.ErrorMessages;
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

import static helper.ErrorMessages.ok;
import static helper.LoggerHelper.*;
import static main.JsonInterpreterFromRequest.getJSONFromRequest;

public class ThreadCreateServlet extends HttpServlet {
    private final Logger logger = LogManager.getLogger(ThreadCreateServlet.class.getName());

    private MySqlConnect mySqlServer;

    public ThreadCreateServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(start());
        JSONObject req = getJSONFromRequest(request, "ThreadCreate");
        boolean isDeleted = false;
        if (req.containsKey("isDeleted")) {
            isDeleted = (boolean) req.get("isDeleted");
        }
        String short_name = (String) req.get("forum");
        String title = (String) req.get("title");
        boolean isClosed = false;
        if (req.containsKey("isClosed")) {
            isClosed = (boolean) req.get("isClosed");
        }
        String user = (String) req.get("user");
        String messageThread = (String) req.get("message");
        String slug = (String) req.get("slug");
        String date = (String) req.get("date");
        short status = ok;
        String message = "";
        int result;
        StringBuilder query = new StringBuilder();
        int forumId = mySqlServer.getForumIdByShortName(short_name);
        query
                .append("insert into thread set forum_id = ").append(forumId).append(", ")
                .append("title = '").append(title).append("', ")
                .append("isClosed = ").append(isClosed ? 1 : 0).append(", ")
                .append("founder_id = (select id from users where email = '").append(user).append("'), ")
                .append("date_of_creating = '").append(date).append("', ")
                .append("message = '").append(messageThread).append("', ")
                .append("slug = '").append(slug).append("' ")
                .append(isDeleted ? ", isDeleted = 1;" : ";");
        result = mySqlServer.executeUpdate(query.toString());
        logger.info(resultUpdate(), result);
        ResultSet resultSet;
        Statement statement = mySqlServer.getStatement();
        query.delete(0, query.length());
        query
                .append("select thread.date_of_creating as date, forum.name as forum, thread.id, isClosed, isDeleted, message, slug, title, email as user ")
                .append("from thread ")
                .append("join users on founder_id = users.id ")
                .append("join forum on forum.id = forum_id ")
                .append("where slug = '").append(slug).append("' and ")
                .append("forum.short_name = '").append(short_name).append("';");
        resultSet = mySqlServer.executeSelect(query.toString(), statement);
        try {
            createResponse(response, status, message, resultSet);
        } catch (SQLException e) {
            logger.error(responseCreating());
        }
        mySqlServer.closeExecution(resultSet, statement);
        logger.info(finish());
    }

    private void createResponse(HttpServletResponse response, short status, String message, ResultSet resultSet) throws IOException, SQLException {
        CommonHelper.setResponse(response);
        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        if (status != ErrorMessages.ok) {
            data.put("error", message);
        } else {
            if (resultSet.next()) {
                data.put("forum", resultSet.getString("forum"));
                data.put("id", resultSet.getInt("id"));
                data.put("isClosed", resultSet.getBoolean("isClosed"));
                data.put("isDeleted", resultSet.getBoolean("isDeleted"));
                data.put("message", resultSet.getString("message"));
                data.put("slug", resultSet.getString("slug"));
                data.put("title", resultSet.getString("title"));
                data.put("user", resultSet.getString("user"));
                data.put("date", resultSet.getString("date").substring(0,19));
            } else {
                status = ErrorMessages.noRequestedObject;
                data.put("error", ErrorMessages.noThread());
            }
        }
        obj.put("response", data);
        obj.put("code", status);
        response.getWriter().write(obj.toString());
    }
}