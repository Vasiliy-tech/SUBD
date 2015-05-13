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

public class ThreadOpenServlet extends HttpServlet {
    private Logger logger = LogManager.getLogger(ThreadOpenServlet.class.getName());

    private MySqlConnect mySqlServer;

    public ThreadOpenServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(LoggerHelper.start());

        JSONObject req = getJSONFromRequest(request, "PostCreate");

        short status = ErrorMessages.ok;
        String message = "";

        long threadId= 0;
        if (req.containsKey("thread")) {
            threadId = (long)req.get("thread");
        } else {
            status = ErrorMessages.wrongData;
            message = ErrorMessages.wrongJSONData();
        }

        int result = 0;
        String query;

        if (status == ErrorMessages.ok) {
            query = "update thread set isClosed = 0 where id = " + threadId + ";";
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
        JSONObject obj = new JSONObject();
        if (status == ErrorMessages.ok) {
            JSONObject data = new JSONObject();
            data.put("thread", threadId);
            obj.put("response", data);
        } else {
            obj.put("response", message);
        }
        obj.put("code", status);
        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}