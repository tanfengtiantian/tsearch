package com.tf.search.http;

import com.tf.search.engine.Engine;
import com.tf.search.types.SearchRequest;
import com.tf.search.types.SearchResponse;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JettyProcessor extends HttpServlet {

    private final Engine engine;

    public JettyProcessor(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // servlet init
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String query =req.getParameter("query");

        SearchResponse output = engine.Search(new SearchRequest(query));
        //SearchResponse output = searcher.Search(new SearchRequest("中国"));

        PrintWriter writer = resp.getWriter();

        writer.print("\n** -------------------output.NumDocs="+output.NumDocs+"------------------ **\n");
        for (int i = 0; i < output.NumDocs; i++) {
            writer.print("\n** -------------------output.DocId="+output.Docs[i].DocId+" BM25="+output.Docs[i].Scores+"------------------ **\n");
            System.out.println("output.DocId=" + output.Docs[i].DocId +" BM25="+output.Docs[i].Scores);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String cmd = req.getParameter("cmd");
        String data = req.getParameter("data");
        //logger.info("【Metric 查询请求】cmd = {} data = {} time = {}", cmd, data, LocalDateTime.now());
        String response = dispatch(cmd, data);
        resp.setContentType("applicaion/json;charset=utf-8");
        PrintWriter writer = resp.getWriter();
        writer.print(response);
    }

    private String dispatch (String cmd, String data) {
        String response = null;
        switch (cmd) {
            case "metric/dashboard":
                //response = queryDashboard();
                break;
            case "metric/logging":
                //response = queryLogging();
                break;
            case "metric/cluster":
                //response = queryCluster();
                break;
            case "metric/java-properties":
                //response = queryJavaProperties();
                break;
            case "metric/thread-dump":
                //response = queryThreadDump();
                break;
            case "metric/broker-config":
                //response = queryBrokerConfig();
                break;
            case "metric/topics":
                //response = queryTopics();
                break;
            case "metric/deleteTopic":
                //response = doDeleteTopic(data);
                break;
            default:
                //response = "404 Not found";
        }
        return response;
    }

}
