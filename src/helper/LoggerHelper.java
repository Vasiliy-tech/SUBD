package helper;


public class LoggerHelper {

    public static String start() {
        return "Start";
    }

    public static String finish() {
        return "Finish";
    }

    public static String responseCreating() {
        return "Error while creating response";
    }

    public static String resultUpdate() {
        return "Strings affected: {}";
    }

    public static String query() {
        return "Query to send: {}";
    }

    public static String responseJSON() {
        return "JSON to write in response: {}";
    }

    public static String connection() {
        return "Connected to {}";
    }

    public static String userDetailError() {
        return "User Details error";
    }

    public static String userDetailJSON() {
        return "User Details JSON: ";
    }

    public static String forumDetailError() {
        return "Forum Details error";
    }

    public static String forumDetailJSON() {
        return "Forum Details JSON: ";
    }

    public static String threadDetailError() {
        return "Thread Details error";
    }

    public static String threadDetailJSON() {
        return "Thread Details JSON: ";
    }

    public static String postDetailError() {
        return "Post Details error";
    }

    public static String postDetailJSON() {
        return "Post Details JSON: ";
    }

    public static String jsonFromRequest() {
        return "JSON from request: {}";
    }

    public static String noUserOrForum() {
        return "No such user or forum";
    }

    public static String errorWhileReadingJSON() {
        return "Error while reading the JSON in {}";
    }
}
