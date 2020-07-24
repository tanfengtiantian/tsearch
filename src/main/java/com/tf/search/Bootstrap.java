package com.tf.search;

import com.tf.search.engine.Engine;
import com.tf.search.http.JettyBroker;
import com.tf.search.types.EngineInitOptions;

import java.util.Properties;

public class Bootstrap {

    public static void main(String[] args) {

        Engine engine = new Engine();
        EngineInitOptions options = new EngineInitOptions();
        options.SegmenterDictionaries = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/CoreNatureDictionary.mini.txt";
        options.StopTokenFile = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/stopwords.txt";
        engine.Init(options);

        JettyBroker jettyBroker = new JettyBroker();
        jettyBroker.init(engine,new Properties());

        jettyBroker.start();

    }
}
