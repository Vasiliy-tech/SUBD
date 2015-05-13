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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static main.JsonInterpreterFromRequest.getJSONFromRequest;

public class PostCreateServlet extends HttpServlet {
    private Logger logger = LogManager.getLogger(PostCreateServlet.class.getName());
    private MySqlConnect mySqlServer;

    public PostCreateServlet(MySqlConnect mySqlServer) {
        this.mySqlServer = mySqlServer;
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(LoggerHelper.start());
        JSONObject req = getJSONFromRequest(request, "PostCreateServlet");
        short status = ErrorMessages.ok;
        String message = "";

        //TODO Сюда нужен еще один валидатор

        boolean isDeleted = false;
        boolean isApproved = false;
        boolean isHighlighted = false;
        boolean isEdited = false;
        boolean isSpam = false;

        if (req.containsKey("isDeleted")) {
            isDeleted = (boolean) req.get("isDeleted");
        }
        if (req.containsKey("isApproved")) {
            isApproved = (boolean) req.get("isApproved");
        }
        if (req.containsKey("isHighlighted")) {
            isHighlighted = (boolean) req.get("isHighlighted");
        }
        if (req.containsKey("isEdited")) {
            isEdited = (boolean) req.get("isEdited");
        }
        if (req.containsKey("isSpam")) {
            isSpam = (boolean) req.get("isSpam");
        }


        long parentId = 0;
        if (req.containsKey("parent")) {
            parentId = req.get("parent") == null ? 0 : (long)req.get("parent") ;
        }

        String shortName = (String)req.get("forum");
        String email = (String)req.get("user");
        String messagePost = (String)req.get("message");
        long thread =  (long)req.get("thread");
        String date = (String)req.get("date");
        int result;

        String matPath = "";
        if (parentId != 0) {
            String parent = mySqlServer.getParentPathByParentId(parentId);
            if (parent == null) {
                status = ErrorMessages.noRequestedObject;
                message = ErrorMessages.noParent();
            } else {
                if (parent.equals("")) {
                    matPath = String.format("%03d", parentId);
                } else {
                    matPath = parent + String.format("_%03d", parentId);
                }
            }
        }

        StringBuilder query = new StringBuilder();
        ResultSet resultSet = null;
        Statement statement = mySqlServer.getStatement();
        if (status == ErrorMessages.ok) {
            int authorId = mySqlServer.getUserIdByEmail(email);
            int forumId = mySqlServer.getForumIdByShortName(shortName);
            if (forumId > 0) {
                if (authorId > 0) {
                    String forumName = mySqlServer.getForumNameById(forumId);
                    query
                            .append("insert into post set ")
                            .append("thread = ").append(thread).append(", ")
                            .append("message = '").append(messagePost).append("', ")
                            .append("author_id = ").append(authorId).append(", ")
                            .append("date_of_creating = '").append(date).append("', ")
                            .append("forum_id = ").append(forumId).append(", ")
                            .append("parent = '").append(matPath).append("', ")
                            .append("isApproved = ").append(isApproved ? 1 : 0).append(", ")
                            .append("isHighlighted = ").append(isHighlighted ? 1 : 0).append(", ")
                            .append("isEdited = ").append(isEdited ? 1 : 0).append(", ")
                            .append("isSpam = ").append(isSpam ? 1 : 0).append(", ")
                            .append("isDeleted = ").append(isDeleted ? 1 : 0)
                            .append(";");
                    result = mySqlServer.executeUpdate(query.toString());
                    logger.info(LoggerHelper.resultUpdate(), result);

                    query.delete(0, query.length());

                    query
                            .append("select post.id, post.date_of_creating as date, '")
                            .append(forumName)
                            .append("' as forum, isApproved, isDeleted, isEdited, isSpam, isHighlighted, message, parent, thread, '").append(email)
                            .append("' as user ")
                            .append("from post ")
                            .append("where author_id = ")
                            .append(authorId)
                            .append(" and ")
                            .append("post.date_of_creating = '")
                            .append(date)
                            .append("';");
                    resultSet = mySqlServer.executeSelect(query.toString(), statement);
                } else {
                    status = ErrorMessages.noRequestedObject;
                    message = ErrorMessages.noUser();
                    logger.info(LoggerHelper.noUserOrForum());
                }
            } else {
                status = ErrorMessages.noRequestedObject;
                message = ErrorMessages.noForum();
                logger.info(LoggerHelper.noUserOrForum());
            }
        }
        try {
            createResponse(response, status, message, resultSet);
        } catch (SQLException e) {
            logger.error(LoggerHelper.responseCreating());
            logger.error(e);
            e.printStackTrace();
        }
        mySqlServer.closeExecution(resultSet, statement);
        logger.info(LoggerHelper.finish());
    }

    private void createResponse(HttpServletResponse response, short status, String message, ResultSet resultSet) throws IOException, SQLException {
        CommonHelper.setResponse(response);
        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        if (status != ErrorMessages.ok) {
            data.put("error", message);
        } else {
            if (resultSet.next()) {
                data.put("date", resultSet.getString("date").substring(0, 19));
                data.put("forum", resultSet.getString("forum"));
                data.put("id", resultSet.getInt("id"));
                data.put("isApproved", resultSet.getBoolean("isApproved"));
                data.put("isHighlighted", resultSet.getBoolean("isHighlighted"));
                data.put("isEdited", resultSet.getBoolean("isEdited"));
                data.put("isSpam", resultSet.getBoolean("isSpam"));
                data.put("isDeleted", resultSet.getBoolean("isDeleted"));
                data.put("message", resultSet.getString("message"));
                String temp = resultSet.getString("parent");
                if (temp.equals("")) {
                    data.put("parent", null);
                }else {
                    data.put("parent", Integer.parseInt(temp.substring(temp.length() - 3)));
                }
                data.put("thread", resultSet.getInt("thread"));
                data.put("user", resultSet.getString("user"));

            } else {
                status = ErrorMessages.noRequestedObject;
                data.put("error", "Error while PostCreate");
            }
        }
        obj.put("response", data);
        obj.put("code", status);
        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}