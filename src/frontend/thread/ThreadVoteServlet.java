package frontend.thread;

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

import static helper.LoggerHelper.resultUpdate;
import static main.JsonInterpreterFromRequest.getJSONFromRequest;

public class ThreadVoteServlet extends HttpServlet {
    private Logger logger = LogManager.getLogger(ThreadVoteServlet.class.getName());
    private MySqlConnect mySqlServer;

    public ThreadVoteServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(LoggerHelper.start());

        JSONObject req = getJSONFromRequest(request, "Thread_vote");

        short status = ErrorMessages.ok;
        String message = "";

        long threadId = 0;
        long vote = 0;
        if (req.containsKey("thread") && req.containsKey("vote")) {
            threadId = (long)req.get("thread");
            vote = (long)req.get("vote");
        }
        if (vote != 1 && vote != -1 || threadId == 0) {
            status = ErrorMessages.wrongData;
            message = ErrorMessages.wrongJSONData();
        }

        int result = 0;
        String query;

        if (status == ErrorMessages.ok) {
            String likes = vote > 0 ? "likes" : "dislikes";
            query = "update thread set " + likes + " = " + likes + " + 1" + " where id = " + threadId + ";";
            result = mySqlServer.executeUpdate(query);
            logger.info(resultUpdate(), result);
        }

        if (result == 0) {
            status = ErrorMessages.noRequestedObject;
            message = ErrorMessages.noPost();
        }

        try {
            createResponse(response, status, message, threadId);
        } catch (SQLException e) {
            logger.error(LoggerHelper.responseCreating());
            logger.error(e);
            e.printStackTrace();
        }
        logger.info(LoggerHelper.finish());
    }

    private void createResponse(HttpServletResponse response, short status, String message, long threadId) throws IOException, SQLException {
        CommonHelper.setResponse(response);
        JSONObject data = null;
        if (status == ErrorMessages.ok) {
            data = mySqlServer.getThreadDetailsById((int)threadId, false, false);
        }
        if (data == null) {
            status = ErrorMessages.noRequestedObject;
            message = ErrorMessages.noThread();
        }
        JSONObject obj = new JSONObject();
        if (status == ErrorMessages.ok) {
            obj.put("response", data);
        } else {
            obj.put("response", message);
        }
        obj.put("code", status);
        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}