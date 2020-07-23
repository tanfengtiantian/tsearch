package com.tf.search.http;

import com.tf.search.engine.Engine;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import java.util.Properties;

public class JettyBroker {

    private Server server;

    private int port;

    public void init(Engine engine, Properties props) {
        JettyProcessor jettyProcessor = new JettyProcessor(engine);
        this.port = Integer.valueOf(props.getProperty("serverPort", "8888"));
        server = new Server(createThreadPool());
        server.addConnector(createConnector(port));
        server.setHandler(createHandlers(jettyProcessor));
    }

    public void start() {
        try {
            server.start();
        } catch (Exception e) {
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
        }
    }


    private ThreadPool createThreadPool () {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(2);
        threadPool.setMaxThreads(5);
        return threadPool;
    }

    private NetworkConnector createConnector (Integer port) {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        return connector;
    }

    private HandlerCollection createHandlers (JettyProcessor jettyProcessor) {
        // 静态资源访问
        ContextHandler contextHandler = new ContextHandler("/");

        ResourceHandler handler = new ResourceHandler();
        handler.setDirectoriesListed(true);
        handler.setWelcomeFiles(new String[]{"index.html"});
        handler.setBaseResource(Resource.newResource(JettyBroker.class.getClassLoader().getResource("dist")));
        contextHandler.setHandler(handler);

        // 数据接口
        ServletContextHandler servletHandler = new ServletContextHandler();
        servletHandler.addServlet(new ServletHolder(jettyProcessor),"/_search");

        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.setHandlers(new Handler[] {contextHandler, servletHandler});
        return handlerCollection;
    }
}
