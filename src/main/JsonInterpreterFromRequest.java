package main;

import helper.LoggerHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;


public class JsonInterpreterFromRequest {
    static private Logger logger = LogManager.getLogger(JsonInterpreterFromRequest.class.getName());

    static public JSONObject getJSONFromRequest(HttpServletRequest request, String servletName) {
        JSONObject jsonObj = null;
        StringBuilder jb = new StringBuilder();
        String line;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
            jb.append(line);
            String test = jb.toString();
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(test);
            jsonObj = (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(LoggerHelper.errorWhileReadingJSON(), servletName);
        }
        logger.info(LoggerHelper.jsonFromRequest(), jsonObj.toString());
        return jsonObj;
    }
}
