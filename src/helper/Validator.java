package helper;

import org.json.simple.JSONObject;


public class Validator {
    public static boolean userValidation(JSONObject user) {
        if (user.get("email") == null) {
            return false;
        }
        if (user.get("name") == null) {
            user.remove("name");
            user.put("name", "");
        }
        if (user.get("about") == null) {
            user.remove("about");
            user.put("about", "");
        }
        if (user.get("username") == null) {
            user.remove("username");
            user.put("username", "");
        }
        return true;
    }
}
