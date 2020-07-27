package com.tf.search.examples;

import com.tf.search.engine.Engine;
import com.tf.search.http.JettyBroker;
import com.tf.search.types.EngineInitOptions;
import com.tf.search.types.IdxType;
import com.tf.search.types.SimpleFieldInfo;

import java.util.*;

public class example2 {
    private static Engine engine = new Engine();
    private static final String indexName = "xinwen";
    private static final String fields = "Content";
    public static void main(String[] args) {

        EngineInitOptions options = new EngineInitOptions();
        options.SegmenterDictionaries = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/CoreNatureDictionary.mini.txt";
        options.StopTokenFile = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/stopwords.txt";

        //初始化
        engine.Init(options);

        //构造Mapping
        engine.IndexMapping(indexName,IndexMapping());
        //insert Document
        engine.IndexInsertOrUpdate(indexName,IndexDocument(fields,"此次百度收购将成中国互联网最大并购"));
        engine.IndexInsertOrUpdate(indexName,IndexDocument(fields,"百度宣布拟全资收购91无线业务"));
        engine.IndexInsertOrUpdate(indexName,IndexDocument(fields,"百度是中国最大的搜索引擎"));

        //刷新索引 强制保存
        engine.FlushIndex();


        JettyBroker jettyBroker = new JettyBroker();
        jettyBroker.init(engine,new Properties());

        jettyBroker.start();


    }

    private static List<SimpleFieldInfo> IndexMapping() {
        List<SimpleFieldInfo> Fields = new ArrayList<>();
        SimpleFieldInfo content = new SimpleFieldInfo(fields,IdxType.IDX_TYPE_STRING_SEG);
        Fields.add(content);
        return Fields;
    }


    private static Map<String,Object> IndexDocument(String field, String content){
        Map<String,Object> document = new HashMap<>();
        document.put(field,content);
        return document;
    }
}
