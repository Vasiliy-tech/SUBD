package frontend.forum;

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
import java.util.Map;

import static helper.ErrorMessages.wrongData;
import static helper.ErrorMessages.wrongJSONData;

public class ForumListThreadsServlet extends HttpServlet {
    private Logger logger = LogManager.getLogger(ForumListThreadsServlet.class.getName());

    private MySqlConnect mySqlServer;

    public ForumListThreadsServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(LoggerHelper.start());

        Map<String, String[]> paramMap = request.getParameterMap();
        String forum = paramMap.containsKey("forum") ? paramMap.get("forum")[0] : null;
        String asc = paramMap.containsKey("order") ? paramMap.get("order")[0] : null;
        String since = paramMap.containsKey("since") ? paramMap.get("since")[0] : null;
        String limit = paramMap.containsKey("limit") ? paramMap.get("limit")[0] : null;
        String[] related = paramMap.get("related");

        short status = ErrorMessages.ok;
        String message = "";

        int forumId = mySqlServer.getForumIdByShortName(forum);

        StringBuilder query = new StringBuilder();
        ResultSet resultSet;
        Statement statement = mySqlServer.getStatement();

        // TODO index forum: short_name, id || thread: forum_id, date_of_creating
        query.append("select id from thread t ")
                .append("where forum_id = '")
                .append(forumId)
                .append("' ");
        CommonHelper.appendDateAndAscAndLimit(query, since, asc, limit);
        resultSet = mySqlServer.executeSelect(query.toString(), statement);
        try {
            createResponse(response, status, message, resultSet, related);
        } catch (SQLException e) {
            logger.error(LoggerHelper.responseCreating());
            logger.error(e);
            e.printStackTrace();
        }
        mySqlServer.closeExecution(resultSet, statement);

        logger.info(LoggerHelper.finish());
    }

    private void createResponse(HttpServletResponse response, short status, String message, ResultSet resultSet, String[] related) throws IOException, SQLException {
        CommonHelper.setResponse(response);
        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();

        JSONArray listThreads = new JSONArray();

        if (status != ErrorMessages.ok || resultSet == null) {
            data.put("error", message);
            obj.put("response", data);
        } else {
            boolean user = false;
            boolean forum = false;
            if (related != null) {
                if (related != null) {
                    for (String aRelated : related) {
                        switch (aRelated) {
                            case "user":
                                user = true;
                                break;
                            case "forum":
                                forum = true;
                                break;
                            default:
                                status = wrongData;
                                message = wrongJSONData();
                        }
                    }
                }
            }
            if (status == ErrorMessages.ok) {
                while (resultSet.next()) {
                    listThreads.add(mySqlServer.getThreadDetailsById(resultSet.getInt("id"), user, forum));
                }
                obj.put("response", listThreads);
            } else {
                data.put("error", message);
                obj.put("response", data);
            }
        }
        obj.put("code", status);
        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}