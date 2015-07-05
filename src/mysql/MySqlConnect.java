package mysql;



import helper.ErrorMessages;
import helper.LoggerHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.*;

public class MySqlConnect {
    private static Logger logger = LogManager.getLogger(MySqlConnect.class.getName());

    private static ConnectionPool connectionPool = new ConnectionPool();
    private Connection connection;
    //private PreparedStatement postDetailsPrepStatement = null;

    public MySqlConnect() {

    }

    public void init() {
        this.connection = connectionPool.getConnection();
    }

    public void close() {
        try {
            connection.close();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public Statement getStatement() {
        try {
            return connection.createStatement();
        } catch (Exception e) {
            return null;
        }
    }

    public ResultSet executeSelect(String query, Statement statement) {
        ResultSet resultSet = null;
        try {
            if (statement == null) {
                statement = connection.createStatement();
            }
            logger.info(LoggerHelper.query(), query);
            resultSet = statement.executeQuery(query);
        } catch (SQLException ex) {
            logger.error("query:\n{}", query);
            logger.error(ex);
        }
        return resultSet;
    }




    public int executeUpdate(String query){
        int result = 0;
        try {
            Statement statement = connection.createStatement();
            logger.info(LoggerHelper.query(), query);
            result = statement.executeUpdate(query);
            statement.close();
        } catch (SQLException ex) {
            logger.error(ex);
        }
        return result;
    }

    public void closeExecution(ResultSet resultSet, Statement statement){
        try {
            if (statement != null) {
                statement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public String getParentPathByParentId(long parentId) {
        ResultSet resultSet1;
        Statement statement1 = getStatement();

        final String query = "select parent from post where id = " + parentId + ";";
        resultSet1 = executeSelect(query, statement1);

        String parent = null;
        try {
            if(resultSet1 != null && resultSet1.next()) {
                parent = resultSet1.getString("parent");
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
        closeExecution(resultSet1, statement1);
        return parent;
    }

    public String getForumNameById(int id) {
        ResultSet resultSet1 = null;
        Statement statement1 = getStatement();
        String query = "select name from forum where id = " + id + ";";
        resultSet1 = executeSelect(query, statement1);
        String forumName = null;
        try {
            if(resultSet1 != null && resultSet1.next()) {
                forumName = resultSet1.getString("name");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        closeExecution(resultSet1, statement1);
        return forumName;
    }

    public int getUserIdByEmail(String email) {
        ResultSet preSet = null;
        Statement preStatement = getStatement();
        String query = "select id from users where email = '" + email + "';";
        preSet = executeSelect(query, preStatement);
        int userId = -1;

        try {
            if (preSet != null && preSet.next()) {
                userId = preSet.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        } finally {
            closeExecution(preSet, preStatement);
        }
        return userId;
    }

    public int getForumIdByShortName(String forum) {
        int forumId = -1;
        ResultSet preSet;
        Statement preStatement = getStatement();
        String query = "select id from forum where short_name = '" + forum + "';";
        preSet = executeSelect(query, preStatement);

        try {
            if (preSet != null && preSet.next()) {
                forumId = preSet.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        } finally {
            closeExecution(preSet, preStatement);
        }
        return forumId;
    }

    public JSONObject getUserDetail(String email) throws IOException, SQLException {
        String query;
        ResultSet resultSet;
        Statement statement = getStatement();

        ResultSet followee = null, follower = null, subscription = null;
        Statement statementFollowee = getStatement(), statementFollower = getStatement(), statementSubscription = getStatement();
        query = "select * from users where email = '" + email + "';";
        JSONObject data = new JSONObject();
        resultSet = executeSelect(query, statement);
        try {
            if(resultSet != null && resultSet.next()) {
                data.put("isAnonymous", resultSet.getBoolean("isAnonymous"));
                data.put("email", resultSet.getString("email"));
                data.put("about", resultSet.getString("about").equals("")? null : resultSet.getString("about"));
                data.put("name", resultSet.getString("name").equals("")? null : resultSet.getString("name"));
                data.put("username", resultSet.getString("username").equals("")? null : resultSet.getString("username") );
                data.put("id", resultSet.getInt("id"));

                query = "select email from users use index (id__email) join follow on followee_id = id where follower_id = " + resultSet.getInt("id") + ";";
                followee = executeSelect(query, statementFollowee);

                query = "select email from users use index (id__email) join follow on follower_id = id where followee_id = " + resultSet.getInt("id") + ";";
                follower = executeSelect(query, statementFollower);

                query = "select thread_id from users join subscribtion on user_id = id where id = " + resultSet.getInt("id") + ";";
                subscription = executeSelect(query, statementSubscription);
            } else {
                data = null;
            }
        } catch (SQLException e) {
            logger.error(LoggerHelper.userDetailError());
            logger.error(e);
            e.printStackTrace();
        }
        if (data != null) {
            JSONArray toFollow = new JSONArray();
            JSONArray iAmFollowed = new JSONArray();
            JSONArray subscribed = new JSONArray();

            while(followee != null && followee.next()) {
                toFollow.add(followee.getString(1));
            }
            while(follower != null && follower.next()) {
                iAmFollowed.add(follower.getString(1));
            }
            while(subscription != null && subscription.next()) {
                subscribed.add(subscription.getInt(1));
            }
            data.put("following", toFollow);
            data.put("followers", iAmFollowed);
            data.put("subscriptions", subscribed);
            logger.info(LoggerHelper.userDetailJSON(), data.toJSONString());
        }
        closeExecution(resultSet, statement);
        closeExecution(followee, statementFollowee);
        closeExecution(follower, statementFollower);
        closeExecution(subscription, statementSubscription);
        return data;
    }


    public JSONObject getUserDetail(int id) throws IOException, SQLException {
        String query, message = "";
        int status = ErrorMessages.ok;
        ResultSet resultSet;
        Statement statement = getStatement();

        ResultSet followee = null, follower = null, subscription = null;
        Statement statement_followee = getStatement(), statement_follower = getStatement(), statement_subscription = getStatement();
        query = "select * from users where id = '" + id + "';";
        resultSet = executeSelect(query, statement);
        try {
            if(resultSet != null && resultSet.next()) {
                query = "select email from users use index (id__email) join follow on followee_id = id where follower_id = " + resultSet.getInt("id") + ";";
                followee = executeSelect(query, statement_followee);
                query = "select email from users use index (id__email) join follow on follower_id = id where followee_id = " + resultSet.getInt("id") + ";";
                follower = executeSelect(query, statement_follower);
                query = "select thread_id from users join subscribtion on user_id = id where id = " + resultSet.getInt("id") + ";";
                subscription = executeSelect(query, statement_subscription);
            }
            else {
                resultSet = null;
                status = ErrorMessages.noRequestedObject;
                message = ErrorMessages.noUser();
                logger.error(message);
            }
        } catch (SQLException e) {
            logger.error(LoggerHelper.userDetailError());
        }
        JSONObject data = new JSONObject();
        JSONArray toFollow = new JSONArray();
        JSONArray iAmFollowed = new JSONArray();
        JSONArray subscribed = new JSONArray();

        if (status != ErrorMessages.ok ) {
            data.put("error", message);
        } else {
            data.put("isAnonymous", resultSet.getBoolean("isAnonymous"));
            data.put("email", resultSet.getString("email"));
            data.put("about", resultSet.getString("about").equals("") ? null : resultSet.getString("about"));
            data.put("name", resultSet.getString("name").equals("") ? null : resultSet.getString("name"));
            data.put("username", resultSet.getString("username").equals("")? null : resultSet.getString("username") );
            data.put("id", resultSet.getInt("id"));
            while(followee != null && followee.next()) {
                toFollow.add(followee.getString(1));
            }
            while(follower != null && follower.next()) {
                iAmFollowed.add(follower.getString(1));
            }
            while(subscription != null && subscription.next()) {
                subscribed.add(subscription.getInt(1));
            }
            data.put("following", toFollow);
            data.put("followers", iAmFollowed);
            data.put("subscriptions", subscribed);
        }
        closeExecution(resultSet, statement);
        closeExecution(followee, statement_followee);
        closeExecution(follower, statement_follower);
        closeExecution(subscription, statement_subscription);
        logger.info(LoggerHelper.userDetailJSON(), data.toString());
        return data;
    }



    public JSONObject getForumDetailsById(int id) throws IOException, SQLException {
        ResultSet resultSet;
        Statement statement = getStatement();
        // todo убрать join
        String query = "select forum.id, forum.name, short_name, email from forum " +
                "join users on founder_id = users.id " +
                "where forum.id = '" + id +"';";
        resultSet = executeSelect(query, statement);

        JSONObject data = new JSONObject();
        if (resultSet.next()) {
            data.put("user", resultSet.getString(4));
            data.put("name", resultSet.getString(2));
            data.put("id", resultSet.getString(1));
            data.put("short_name", resultSet.getString(3));
            logger.info(LoggerHelper.forumDetailJSON(), data.toString());
        } else {
            data = null;
        }
        closeExecution(resultSet, statement);
        return data;
    }

    PreparedStatement forumStatement;
    public void prepareStatementsForForumDetails() {
        try {
            forumStatement = connection.prepareStatement("select id, founder_id, name, short_name from forum where forum.id = ?;");
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public void closeStatementsForForumDetails() {
        try {
            forumStatement.close();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public JSONObject getForumDetailsByIdWithPreparedStatements(int id) throws IOException, SQLException {
        ResultSet resultSet;
        forumStatement.setInt(1, id);
        resultSet = forumStatement.executeQuery();
        JSONObject data = new JSONObject();
        if (resultSet != null && resultSet.next()) {
            data.put("user", getEmailById(resultSet.getInt(2)));
            data.put("name", resultSet.getString(3));
            data.put("id", resultSet.getString(1));
            data.put("short_name", resultSet.getString(4));
            logger.info(LoggerHelper.forumDetailJSON(), data.toString());
            resultSet.close();
        } else {
            data = null;
        }
        return data;
    }

    private PreparedStatement threadStatement;
    private PreparedStatement threadEmailStatement;
    private PreparedStatement threadShortForumStatement;

    public void prepareStatementsForThreadDetails(boolean user, boolean forum) {
        try {
            threadStatement = connection.prepareStatement("select forum_id, founder_id, date_of_creating as date, id, isClosed, isDeleted, message, slug, title, likes, dislikes, amountOfPost from thread where id = ?;");
            if (!user) {
                threadEmailStatement = connection.prepareStatement("select email from users use index (id__email) where id = ?;");
            }
            if (forum) {
                prepareStatementsForForumDetails();
            } else {
                threadShortForumStatement = connection.prepareStatement("select short_name from forum where id = ?;");
            }
        } catch (Exception e) {
            logger.info(e);
            e.printStackTrace();
        }
    }

    public void closeStatementsForThreadDetails(boolean user, boolean forum) {
        try {
            threadStatement.close();
            if (!user) {
                threadEmailStatement.close();
            }
            if (forum) {
                closeStatementsForForumDetails();
            } else {
                threadShortForumStatement.close();
            }
        } catch (Exception e) {
            logger.info(e);
            e.printStackTrace();
        }
    }

    public JSONObject getThreadDetailsByIdWithPreparedStatements(int id, boolean user, boolean forum) throws IOException, SQLException {
        threadStatement.setInt(1, id);
        ResultSet resultSet = threadStatement.executeQuery();

        JSONObject data = new JSONObject();

        if (resultSet != null && resultSet.next()) {

            data.put("date", resultSet.getString(3).substring(0, 19));
            data.put("dislikes", resultSet.getInt(11));
            if (forum) {
                data.put("forum", getForumDetailsByIdWithPreparedStatements(resultSet.getInt(1)));
            } else {
                threadShortForumStatement.setInt(1, resultSet.getInt(1));
                ResultSet shortNameResultSet= threadShortForumStatement.executeQuery();
                if (shortNameResultSet != null && shortNameResultSet.next()) {
                    data.put("forum", shortNameResultSet.getString(1));
                }
                shortNameResultSet.close();
            }
            data.put("id", resultSet.getInt(4));
            data.put("isClosed", resultSet.getBoolean(5));
            data.put("isDeleted", resultSet.getBoolean(6));
            data.put("likes", resultSet.getInt(10));
            data.put("message", resultSet.getString(7));
            data.put("points", resultSet.getInt(10) - resultSet.getInt(11) );
            data.put("posts", resultSet.getInt(12));
            data.put("slug", resultSet.getString(8));
            data.put("title", resultSet.getString(9));
            if (user) {
                data.put("user", getUserDetail(resultSet.getInt(2)));
            } else {
                threadEmailStatement.setInt(1, resultSet.getInt(2));
                ResultSet emailResultSet= threadEmailStatement.executeQuery();
                if (emailResultSet != null && emailResultSet.next()) {
                    data.put("user", emailResultSet.getString(1));
                }
                emailResultSet.close();
            }
        } else {
            String message = "There is no thread with such id!";
            data.put("error", message);
            logger.error(message);
        }

        resultSet.close();
        logger.info(LoggerHelper.threadDetailJSON(), data.toString());
        return data;
    }

    public JSONObject getForumDetails(String short_name, String related) throws IOException, SQLException {
        ResultSet resultSet;
        Statement statement = getStatement();
        //TODO: убрать join

        String query = "select forum.id, founder_id, forum.name, short_name, email from forum " +
                "join users on founder_id = users.id " +
                "where short_name = '" + short_name +"';";
        resultSet = executeSelect(query, statement);

        JSONObject data = new JSONObject();
        if (resultSet != null && resultSet.next()) {
            if (related != null) {
                data.put("user", getUserDetail(resultSet.getInt(2)));
            } else {
                data.put("user", resultSet.getString(5));
            }
            data.put("name", resultSet.getString(3));
            data.put("id", resultSet.getString(1));
            data.put("short_name", resultSet.getString(4));
            logger.info(LoggerHelper.forumDetailJSON(), data.toString());
        } else {
            data = null;
        }
        closeExecution(resultSet, statement);
        return data;
    }

    public String getEmailById(int id) {
        String query = "select email from users use index (id__email) where id = " + id + ";";
        Statement statement = getStatement();

        ResultSet resultSet = executeSelect(query, statement);
        String email = null;
        try {
            if (resultSet != null && resultSet.next()) {
                email =  resultSet.getString(1);
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        } finally {
            closeExecution(resultSet, statement);
        }
        closeExecution(resultSet, statement);
        return email;
    }


    public JSONObject getThreadDetailsById(int id, boolean user, boolean forum) throws IOException, SQLException {

        ResultSet resultSetCount;
        Statement statementCount = getStatement();

        String query;

        ResultSet resultSet = null;
        Statement statement = getStatement();


        query = "select thread.date_of_creating as date, forum.short_name as forum, short_name, thread.id, isClosed, isDeleted, message, slug, title, email as user, likes, dislikes, amountOfPost " +
                "from thread " +
                "join users on founder_id = users.id " +
                "join forum on forum.id = forum_id " +
                "where thread.id = " + id  + ";";

        resultSet = executeSelect(query, statement);
        JSONObject data = new JSONObject();

        if (resultSet != null && resultSet.next()) {
            data.put("date", resultSet.getString("date").substring(0, 19));
            data.put("dislikes", resultSet.getInt("dislikes"));
            if (forum) {
                data.put("forum", getForumDetails(resultSet.getString("short_name"), null));
            } else {
                data.put("forum", resultSet.getString("forum"));
            }
            data.put("id", resultSet.getInt("id"));
            data.put("isClosed", resultSet.getBoolean("isClosed"));
            data.put("isDeleted", resultSet.getBoolean("isDeleted"));
            data.put("likes", resultSet.getInt("likes"));
            data.put("message", resultSet.getString("message"));
            data.put("points", resultSet.getInt("likes") - resultSet.getInt("dislikes") );
            data.put("posts", resultSet.getInt("amountOfPost"));
            data.put("slug", resultSet.getString("slug"));
            data.put("title", resultSet.getString("title"));
            if (user) {
                data.put("user", getUserDetail(resultSet.getString("user")));
            } else {
                data.put("user", resultSet.getString("user"));
            }
        } else {
            String message = "There is no thread with such id!";
            data.put("error", message);
            logger.error(message);
        }

        closeExecution(resultSet, statement);
        logger.info(LoggerHelper.threadDetailJSON(), data.toString());
        return data;
    }

    public boolean isThreadDeleted(int id) {
        boolean result = false;
        String query = "select isDeleted from thread where id = " + id + ";";
        Statement statement = getStatement();
        ResultSet resultSet = executeSelect(query, statement);
        try {
            if (resultSet != null && resultSet.next()) {
                result = resultSet.getBoolean("isDeleted");
            }
        } catch (Exception e) {
            logger.error("Error while getting info about thread");
            logger.error(e);
            e.printStackTrace();
        } finally {
            closeExecution(resultSet, statement);
        }
        return result;
    }


    public String getNameById(int id) {
        String name = null;
        String query = "select name from users where id = " + id + ";";
        Statement statement = getStatement();
        ResultSet resultSet = executeSelect(query, statement);
        try {
            if (resultSet != null && resultSet.next()) {
                name = resultSet.getString("name");
            }
        } catch (Exception e) {
            logger.error("Error while getting info about thread");
            logger.error(e);
            e.printStackTrace();
        } finally {
            closeExecution(resultSet, statement);
        }
        return name;
    }



    private PreparedStatement postDetailsStatement;
    private PreparedStatement postDetailsForumStatement;
    public void prepareStatementsForPostDetails() {
        try {
            postDetailsStatement = connection.prepareStatement("select id, author_id, forum_id, date_of_creating as date, likes, dislikes, isApproved, isDeleted, isEdited, isSpam, isHighlighted, message, parent, thread from post where post.id = ?;");
            postDetailsForumStatement = connection.prepareStatement("select short_name from forum where id = ?;");
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public void closePrepareStatementForPostDetails() {
        try {
            postDetailsStatement.close();
            postDetailsForumStatement.close();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public JSONObject getPostDetailsWithPrepareStatement(int id, boolean user, boolean thread, boolean forum) throws IOException, SQLException {

        ResultSet resultSet = null;
        postDetailsStatement.setInt(1, id);
        resultSet = postDetailsStatement.executeQuery();

        JSONObject data = new JSONObject();
        if (resultSet != null && resultSet.next()) {
            data.put("date", resultSet.getString(4).substring(0, 19));
            int forumId = resultSet.getInt(3);
            if (forum) {
                data.put("forum", getForumDetailsById(forumId));
            } else {
                postDetailsForumStatement.setInt(1, forumId);
                ResultSet forumResultSet = postDetailsForumStatement.executeQuery();
                try {
                    if (forumResultSet != null && forumResultSet.next()) {
                        data.put("forum", forumResultSet.getString(1));
                    }
                    else {
                        data.put("forum", null);
                    }
                } catch (Exception e) {
                    data.put("forum", null);
                    e.printStackTrace();
                    logger.error(e);
                    logger.error("Error in getting post details");
                } finally {
                    forumResultSet.close();
                }
            }
            data.put("id", resultSet.getInt(1));
            data.put("isApproved", resultSet.getBoolean(7));
            data.put("isHighlighted", resultSet.getInt(11) == 1 ? true : false);
            data.put("isEdited", resultSet.getBoolean(9));
            data.put("isSpam", resultSet.getBoolean(10));
            data.put("isDeleted", resultSet.getBoolean(8));
            data.put("message", resultSet.getString(12));
            data.put("likes", resultSet.getInt(5));
            data.put("dislikes", resultSet.getInt(6));
            data.put("points", resultSet.getInt(5) - resultSet.getInt(6));
            String temp = resultSet.getString(13);
            if (temp.equals("")) {
                data.put("parent", null);
            }else {
                int indexLast = temp.lastIndexOf("_");
                data.put("parent", Integer.parseInt(temp.substring(indexLast + 1)));
            }
            if (thread) {
                data.put("thread", getThreadDetailsById(resultSet.getInt(14), false, false));
            } else {
                data.put("thread", resultSet.getInt(14));
            }

            if (user) {
                data.put("user", getUserDetail(resultSet.getInt(2)));
            } else {
                data.put("user", getEmailById(resultSet.getInt(2)));
            }
            logger.info(LoggerHelper.postDetailJSON(), data.toString());
        } else {
            data = null;
        }
        resultSet.close();
        return data;
    }


    public JSONObject getPostDetails(int id, boolean user, boolean thread, boolean forum) throws IOException, SQLException {
        ResultSet resultSet = null;
        Statement statement = getStatement();
        String query = "select id, author_id, forum_id, date_of_creating as date, likes, dislikes, isApproved, isDeleted, isEdited, isSpam, isHighlighted, message, parent, thread " +
                "from post where post.id = " +id +";";
        resultSet = executeSelect(query, statement);

        JSONObject data = new JSONObject();
        if (resultSet != null && resultSet.next()) {
            data.put("date", resultSet.getString("date").substring(0, 19));
            if (forum) {
                data.put("forum", getForumDetailsById(resultSet.getInt("forum_id")));
            } else {
                String queryForum = "select short_name from forum where id = " + resultSet.getInt("forum_id") + ";";
                ResultSet forumResultSet;
                Statement forumStatement = getStatement();
                forumResultSet = executeSelect(queryForum, forumStatement);

                try {
                    if (forumResultSet != null && forumResultSet.next())
                        data.put("forum", forumResultSet.getString("short_name"));
                    else {
                        data.put("forum", null);
                    }
                } catch (Exception e) {
                    data.put("forum", null);
                    e.printStackTrace();
                    logger.error(e);
                    logger.error("Error in getting post details");
                } finally {
                    closeExecution(forumResultSet, forumStatement);
                }
            }
            data.put("id", resultSet.getInt("id"));
            data.put("isApproved", resultSet.getBoolean("isApproved"));
            data.put("isHighlighted", resultSet.getInt("isHighlighted") == 1 ? true : false);
            data.put("isEdited", resultSet.getBoolean("isEdited"));
            data.put("isSpam", resultSet.getBoolean("isSpam"));
            data.put("isDeleted", resultSet.getBoolean("isDeleted"));
            data.put("message", resultSet.getString("message"));
            data.put("likes", resultSet.getInt("likes"));
            data.put("dislikes", resultSet.getInt("dislikes"));
            data.put("points", resultSet.getInt("likes") - resultSet.getInt("dislikes"));
            String temp = resultSet.getString("parent");
            if (temp.equals("")) {
                data.put("parent", null);
            }else {
                int indexLast = temp.lastIndexOf("_");
                data.put("parent", Integer.parseInt(temp.substring(indexLast + 1)));
            // change
            }
            if (thread) {
                data.put("thread", getThreadDetailsById(resultSet.getInt("thread"), false, false));
            } else {
                data.put("thread", resultSet.getInt("thread"));
            }

            if (user) {
                data.put("user", getUserDetail(resultSet.getInt("author_id")));
            } else {
                data.put("user", getEmailById(resultSet.getInt("author_id")));
            }
            logger.info(LoggerHelper.postDetailJSON(), data.toString());
        } else {
            data = null;
        }
        resultSet.close();
        closeExecution(resultSet, statement);
        return data;
    }
}