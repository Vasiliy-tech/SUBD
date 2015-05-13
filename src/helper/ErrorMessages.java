package helper;


public class ErrorMessages {

    final public static short ok = 0;

    final public static short noRequestedObject  = 1;

    final public static short notValidRequest = 2;

    final public static short wrongData = 3;

    final public static short unknownError = 4;

    final public static short suchUserAlreadyExist = 5;

    public static String noParent() {
        return "There is no a such parent!";
    }

    public static String noUser() {
        return "No such users.";
    }

    public static String noThread() {
        return "No such thread.";
    }

    public static String noForum() {
        return "No Forum with such id/shortName.";
    }

    public static String noPost() {
        return "No such post";
    }

    public static String wrongParamsOfRequest() {
        return "Wrong request params";
    }

    public static String wrongJSONData() {
        return "Wrong JSON data";
    }

    public static String userAlreadyExist() {
        return "Such user already exist!";
    }

    public static String forumCreateError() { return "No such user or forum with such slug already exist"; }
}
