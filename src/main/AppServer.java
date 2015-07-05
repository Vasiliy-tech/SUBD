package main;

import frontend.ServiceClearServlet;
import frontend.ServiceStatusServlet;
import frontend.forum.*;
import frontend.post.*;
import frontend.thread.*;
import frontend.user.*;
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

        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        String entity;
        context.addServlet(new ServletHolder(new ServiceClearServlet()), "/db/api/clear/");
        context.addServlet(new ServletHolder(new ServiceStatusServlet()), "/db/api/status/");

        entity = "user";
        addServletToContext(context, new UserListServlet(), entity, "getAll");
        addServletToContext(context, new UserListFollowerServlet(), entity, "listFollowers");
        addServletToContext(context, new UserListFolloweeServlet(), entity, "listFollowing");
        addServletToContext(context, new UserCreateServlet(), entity, "create");
        addServletToContext(context, new UserDetailsServlet(), entity, "details");
        addServletToContext(context, new UserFollowServlet(), entity, "follow");
        addServletToContext(context, new UserUnfollowServlet(), entity, "unfollow");
        addServletToContext(context, new UserUpdateServlet(), entity, "updateProfile");
        addServletToContext(context, new UserListPostsServlet(), entity, "listPosts");

        entity = "forum";
        addServletToContext(context, new ForumCreateServlet(), entity, "create");
        addServletToContext(context, new ForumListUsersServlet(), entity, "listUsers");
        addServletToContext(context, new ForumDetailsServlet(), entity, "details");
        addServletToContext(context, new ForumListThreadsServlet(), entity, "listThreads");
        addServletToContext(context, new ForumListPostsServlet(), entity, "listPosts");

        entity = "thread";
        addServletToContext(context, new ThreadCreateServlet(), entity, "create");
        addServletToContext(context, new ThreadDetailsServlet(), entity, "details");
        addServletToContext(context, new ThreadCloseServlet(), entity, "close");
        addServletToContext(context, new ThreadListServlet(), entity, "list");
        addServletToContext(context, new ThreadListPostsServlet(), entity, "listPosts");
        addServletToContext(context, new ThreadOpenServlet(), entity, "open");
        addServletToContext(context, new ThreadRemoveServlet(), entity, "remove");
        addServletToContext(context, new ThreadRestoreServlet(), entity, "restore");
        addServletToContext(context, new ThreadSubscribeServlet(), entity, "subscribe");
        addServletToContext(context, new ThreadUnsubscribeServlet(), entity, "unsubscribe");
        addServletToContext(context, new ThreadUpdateServlet(), entity, "update");
        addServletToContext(context, new ThreadVoteServlet(), entity, "vote");


        entity = "post";
        addServletToContext(context, new PostCreateServlet(), entity, "create");
        addServletToContext(context, new PostDetailsServlet(), entity, "details");
        addServletToContext(context, new PostListServlet(), entity, "list");
        addServletToContext(context, new PostRemoveServlet(), entity, "remove");
        addServletToContext(context, new PostRestoreServlet(), entity, "restore");
        addServletToContext(context, new PostUpdateServlet(), entity, "update");
        addServletToContext(context, new PostVoteServlet(), entity, "vote");

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
