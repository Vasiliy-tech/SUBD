package main;

import frontend.AdminServlet;
import frontend.ServiceClearServlet;
import frontend.ServiceShutDownServlet;
import frontend.ServiceStatusServlet;
import frontend.forum.*;
import frontend.post.*;
import frontend.thread.*;
import frontend.user.*;
import mysql.MySqlConnect;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.Servlet;

public class AppServer {
    private Server server;
    public AppServer(int port) {

        MySqlConnect mySqlServer = new mysql.MySqlConnect();

        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        String entity = "service";
        addServletToContext(context, new AdminServlet(), entity, "admin");
        context.addServlet(new ServletHolder(new ServiceClearServlet(mySqlServer)), "/db/api/clear/");
        context.addServlet(new ServletHolder(new ServiceShutDownServlet()), "/shutDown");
        context.addServlet(new ServletHolder(new ServiceStatusServlet(mySqlServer)), "/db/api/status/");

        entity = "user";
        addServletToContext(context, new UserListServlet(mySqlServer), entity, "getAll");
        addServletToContext(context, new UserListFollowerServlet(mySqlServer), entity, "listFollowers");
        addServletToContext(context, new UserListFolloweeServlet(mySqlServer), entity, "listFollowing");
        addServletToContext(context, new UserCreateServlet(mySqlServer), entity, "create");
        addServletToContext(context, new UserDetailsServlet(mySqlServer), entity, "details");
        addServletToContext(context, new UserFollowServlet(mySqlServer), entity, "follow");
        addServletToContext(context, new UserUnfollowServlet(mySqlServer), entity, "unfollow");
        addServletToContext(context, new UserUpdateServlet(mySqlServer), entity, "updateProfile");
        addServletToContext(context, new UserListPostsServlet(mySqlServer), entity, "listPosts");

        entity = "forum";
        addServletToContext(context, new ForumCreateServlet(mySqlServer), entity, "create");
        addServletToContext(context, new ForumListUsersServlet(mySqlServer), entity, "listUsers");
        addServletToContext(context, new ForumDetailsServlet(mySqlServer), entity, "details");
        addServletToContext(context, new ForumListThreadsServlet(mySqlServer), entity, "listThreads");
        addServletToContext(context, new ForumListPostsServlet(mySqlServer), entity, "listPosts");

        entity = "thread";
        addServletToContext(context, new ThreadCreateServlet(mySqlServer), entity, "create");
        addServletToContext(context, new ThreadDetailsServlet(mySqlServer), entity, "details");
        addServletToContext(context, new ThreadCloseServlet(mySqlServer), entity, "close");
        addServletToContext(context, new ThreadListServlet(mySqlServer), entity, "list");
        addServletToContext(context, new ThreadListPostsServlet(mySqlServer), entity, "listPosts");
        addServletToContext(context, new ThreadOpenServlet(mySqlServer), entity, "open");
        addServletToContext(context, new ThreadRemoveServlet(mySqlServer), entity, "remove");
        addServletToContext(context, new ThreadRestoreServlet(mySqlServer), entity, "restore");
        addServletToContext(context, new ThreadSubscribeServlet(mySqlServer), entity, "subscribe");
        addServletToContext(context, new ThreadUnsubscribeServlet(mySqlServer), entity, "unsubscribe");
        addServletToContext(context, new ThreadUpdateServlet(mySqlServer), entity, "update");
        addServletToContext(context, new ThreadVoteServlet(mySqlServer), entity, "vote");


        entity = "post";
        addServletToContext(context, new PostCreateServlet(mySqlServer), entity, "create");
        addServletToContext(context, new PostDetailsServlet(mySqlServer), entity, "details");
        addServletToContext(context, new PostListServlet(mySqlServer), entity, "list");
        addServletToContext(context, new PostRemoveServlet(mySqlServer), entity, "remove");
        addServletToContext(context, new PostRestoreServlet(mySqlServer), entity, "restore");
        addServletToContext(context, new PostUpdateServlet(mySqlServer), entity, "update");
        addServletToContext(context, new PostVoteServlet(mySqlServer), entity, "vote");

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setResourceBase("public_html");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource_handler, context});
        server.setHandler(handlers);
    }

    private void addServletToContext(ServletContextHandler context, Servlet servlet, String entity, String method){
        String url = "/db/api/" + entity + "/" + method + "/";
        context.addServlet(new ServletHolder(servlet), url);
    }

    public void start(){
        try {
            server.start();
            server.join();
        } catch (Exception e) { System.out.append("There is an error in Server.Start()"); System.exit(1); }
    }

    public void stop(){
        try {
            System.exit(0);
        } catch (Exception e) { System.out.append("There is an error in Server.Stop()"); System.exit(1); }
    }
}
