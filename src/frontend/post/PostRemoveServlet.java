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

import static main.JsonInterpreterFromRequest.getJSONFromRequest;

public class PostRemoveServlet extends HttpServlet {
    private Logger logger = LogManager.getLogger(PostRemoveServlet.class.getName());
    private MySqlConnect mySqlServer;

    public PostRemoveServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(LoggerHelper.start());
        JSONObject req = getJSONFromRequest(request, "PostRemove");

        short status = ErrorMessages.ok;
        String message = "";

        long postId = 0;
        if (req.containsKey("post")) {
            postId = (long)req.get("post");
        } else {
            status = ErrorMessages.wrongData;
            message = ErrorMessages.wrongJSONData();
        }

        int result = 0;
        String query;

        if (status == ErrorMessages.ok) {
            query = "update post set isDeleted = 1 where id = " + postId + ";";
            result = mySqlServer.executeUpdate(query);
            logger.info(LoggerHelper.resultUpdate(), result);

            if (result == 1) {
                query = "update thread set amountOfPost = amountOfPost - 1 where id = (select thread from post where id = " + postId + ");";
                result = mySqlServer.executeUpdate(query.toString());
            }
        }
        if (result == 0) {
            status = ErrorMessages.noRequestedObject;
            message = ErrorMessages.noPost();
        }
        try {
            createResponse(response, status, message, postId);
        } catch (SQLException e) {
            logger.error(LoggerHelper.responseCreating());
            logger.error(e);
            e.printStackTrace();
        }
        logger.info(LoggerHelper.finish());
    }

    private void createResponse(HttpServletResponse response, short status, String message, long postId) throws IOException, SQLException {
        CommonHelper.setResponse(response);

        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        if (status == ErrorMessages.ok) {
            data.put("post", postId);
        }
        obj.put("response", status == ErrorMessages.ok? data: message);
        obj.put("code", status);
        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}