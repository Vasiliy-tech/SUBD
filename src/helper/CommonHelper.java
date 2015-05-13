package helper;

import javax.servlet.http.HttpServletResponse;

//
public class CommonHelper {
    public static void appendSinceId(StringBuilder query, String sinceId) {
        if (sinceId != null) {
            query
                    .append("and id > '")
                    .append(sinceId)
                    .append("' ");
        }
    }
    public static void appendDateAndAscAndLimit(StringBuilder query, String since, String asc, String limit) {
        if (since != null) {
            query
                    .append("and date_of_creating > '")
                    .append(since)
                    .append("' ");
        }
        query.append("order by date_of_creating ");
        appendLimitAndAsc(query, limit, asc);
    }

    public static void appendLimitAndAsc(StringBuilder query, String limit, String asc) {
        if (asc == null) {
            query.append("desc ");
        } else {
            query.append(asc).append(" ");
        }
        if (limit != null) {
            query.append("limit ").append(limit);
        }
        query.append(";");
    }

    public static void setResponse(HttpServletResponse response) {
        response.setContentType("json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
