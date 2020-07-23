package com.tf.search;

import com.tf.search.engine.Engine;
import com.tf.search.http.JettyBroker;
import com.tf.search.types.DocumentIndexData;
import com.tf.search.types.EngineInitOptions;

import java.util.Properties;

public class Bootstrap {

    public static void main(String[] args) {

        Engine engine = new Engine();
        EngineInitOptions options = new EngineInitOptions();
        options.SegmenterDictionaries = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/CoreNatureDictionary.mini.txt";
        options.StopTokenFile = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/stopwords.txt";
        engine.Init(options);

        engine.IndexDocument(1, new DocumentIndexData("此次百度收购将成中国互联网最大并购"), false);
        engine.IndexDocument(2, new DocumentIndexData("百度宣布拟全资收购91无线业务"), false);
        engine.IndexDocument(3, new DocumentIndexData("百度是中国最大的搜索引擎"), false);

        engine.FlushIndex();


        JettyBroker jettyBroker = new JettyBroker();
        jettyBroker.init(engine,new Properties());

        jettyBroker.start();

    }
}
