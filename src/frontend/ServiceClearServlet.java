package frontend;

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

public class ServiceClearServlet extends HttpServlet {
    private final Logger logger = LogManager.getLogger(ServiceClearServlet.class.getName());

    private MySqlConnect mySqlServer;

    public ServiceClearServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info("Truncate All!");
        mySqlServer.executeUpdate("truncate table subscribtion;");
        mySqlServer.executeUpdate("truncate table follow;");
        mySqlServer.executeUpdate("delete from post;");
        mySqlServer.executeUpdate("delete from thread;");
        mySqlServer.executeUpdate("delete from forum;");
        mySqlServer.executeUpdate("delete from users;");
        try {
            createResponse(response);
        } catch (SQLException e) {
            logger.info("Error while creating response for truncate");
        }
        logger.info(LoggerHelper.finish());
    }

    private void createResponse(HttpServletResponse response) throws IOException, SQLException {
        response.setContentType("json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setStatus(HttpServletResponse.SC_OK);

        JSONObject obj = new JSONObject();
        obj.put("response", "OK");
        obj.put("code", 0);
        response.getWriter().write(obj.toString());
    }
}