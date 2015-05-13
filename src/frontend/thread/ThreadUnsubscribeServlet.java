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

import static helper.ErrorMessages.noRequestedObject;
import static helper.ErrorMessages.noThread;
import static helper.LoggerHelper.resultUpdate;
import static main.JsonInterpreterFromRequest.getJSONFromRequest;

public class ThreadUnsubscribeServlet extends HttpServlet {
    private Logger logger = LogManager.getLogger(ThreadUnsubscribeServlet.class.getName());

    private MySqlConnect mySqlServer;

    public ThreadUnsubscribeServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(LoggerHelper.start());

        JSONObject req = getJSONFromRequest(request, "ThreadUnsubscribeServlet");

        short status = ErrorMessages.ok;
        String message = "";

        long threadId= 0;
        String email = (String)req.get("user");

        if (req.containsKey("thread") && email != null) {
            threadId = (long)req.get("thread");
        } else {
            status = ErrorMessages.notValidRequest;
            message = ErrorMessages.wrongParamsOfRequest();
        }

        int result = 0;
        String query;

        int userId = mySqlServer.getUserIdByEmail(email);
        if (userId < 1) {
            status = ErrorMessages.noRequestedObject;
            message = ErrorMessages.noUser();
        }

        if (status == ErrorMessages.ok) {
            query = "delete subscribtion from subscribtion where thread_id = " + threadId + " and user_id = " + userId + ";";
            result = mySqlServer.executeUpdate(query);
            logger.info(resultUpdate(), result);
            if (result == 0) {
                status = noRequestedObject;
                message = noThread();
            }
        }

        try {
            createResponse(response, status, message, threadId, email);
        } catch (SQLException e) {
            logger.error(LoggerHelper.responseCreating());
            logger.error(e);
            e.printStackTrace();
        }

        logger.info(LoggerHelper.finish());
    }

    private void createResponse(HttpServletResponse response, short status, String message, long threadId, String email) throws IOException, SQLException {
        CommonHelper.setResponse(response);
        JSONObject obj = new JSONObject();
        if (status == ErrorMessages.ok) {
            JSONObject data = new JSONObject();
            data.put("thread", threadId);
            data.put("user", email);
            obj.put("response", data);
        } else {
            obj.put("response", message);
        }
        obj.put("code", status);
        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}