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

public class PostVoteServlet extends HttpServlet {
    private Logger logger = LogManager.getLogger(PostVoteServlet.class.getName());
    private MySqlConnect mySqlServer;

    public PostVoteServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(LoggerHelper.start());
        JSONObject req = getJSONFromRequest(request, "PostCreate");

        short status = ErrorMessages.ok;
        String message = "";

        long postId = 0;
        long vote = 0;
        postId = (long)req.get("post");
        vote = (long)req.get("vote");

        if (vote != 1 && vote != -1 || postId == 0) {
            status = ErrorMessages.wrongData;
            message = ErrorMessages.wrongJSONData();
        }

        if (status == ErrorMessages.ok) {
            String likes = vote > 0 ? "likes" : "dislikes";
            final String query = "update post set " + likes + " = " + likes + " + 1" + " where id = " + postId + ";";
            int result = mySqlServer.executeUpdate(query);
            logger.info(LoggerHelper.resultUpdate(), result);
            if (result == 0) {
                status = ErrorMessages.noRequestedObject;
                message = ErrorMessages.noPost();
            }
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
        JSONObject data = null;
        if (status == ErrorMessages.ok) {
            data = mySqlServer.getPostDetails((int) postId, false, false, false);
            if (data == null) {
                status = ErrorMessages.noRequestedObject;
                message = ErrorMessages.noPost();
            }
        }
        if (status == ErrorMessages.ok) {
            obj.put("response", data);
        } else {
            obj.put("error", message);
        }
        obj.put("code", status);
        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}