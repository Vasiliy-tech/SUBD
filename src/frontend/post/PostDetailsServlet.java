package frontend.post;

import helper.CommonHelper;
import helper.ErrorMessages;
import helper.LoggerHelper;
import mysql.MySqlConnect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class PostDetailsServlet extends HttpServlet {
    private Logger logger = LogManager.getLogger(PostDetailsServlet.class.getName());

    private MySqlConnect mySqlServer;

    public PostDetailsServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(LoggerHelper.start());
        Map<String, String[]> paramMap = request.getParameterMap();
        int id = Integer.parseInt(paramMap.containsKey("post") ? paramMap.get("post")[0] : "0");
        String[] related = paramMap.get("related");
        try {
            createResponse(response, id, related);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(LoggerHelper.responseCreating());
            logger.error(e);
            e.printStackTrace();
        }

        logger.info(LoggerHelper.finish());
    }

    private void createResponse(HttpServletResponse response, int id, String[] related) throws IOException, SQLException {
        CommonHelper.setResponse(response);
        JSONObject obj = new JSONObject();
        boolean user = false;
        boolean forum = false;
        boolean thread = false;
        String message = "";
        short status = ErrorMessages.ok;

        if (id == 0) {
            status = ErrorMessages.wrongData;
            message = ErrorMessages.wrongJSONData();
        }

        if (related != null) {
            for (String aRelated : related) {
                switch (aRelated) {
                    case "user":
                        user = true;
                        break;
                    case "forum":
                        forum = true;
                        break;
                    case "thread":
                        thread = true;
                        break;
                    default:
                        status = ErrorMessages.wrongData;
                        message = ErrorMessages.wrongJSONData();
                }
            }
        }
        JSONObject data;
        data = mySqlServer.getPostDetails(id, user, thread, forum);
        if (data == null) {
            status = ErrorMessages.noRequestedObject;
            message = ErrorMessages.noPost();
        }
        obj.put("response", status == ErrorMessages.ok ? data: message);
        obj.put("code", status);
        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}