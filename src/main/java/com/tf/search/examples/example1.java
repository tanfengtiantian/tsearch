package com.tf.search.examples;

import com.tf.search.engine.Engine;
import com.tf.search.types.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class example1 {

    private static Engine searcher = new Engine();

    private static final String indexName = "xinwen";

    private static final SimpleFieldInfo field1 = new SimpleFieldInfo("Content",IdxType.IDX_TYPE_STRING_SEG);

    private static final SimpleFieldInfo field2 = new SimpleFieldInfo("tag",IdxType.IDX_TYPE_STRING);


    public static void main(String[] args) {

        EngineInitOptions options = new EngineInitOptions();
        options.SegmenterDictionaries = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/CoreNatureDictionary.mini.txt";
        options.StopTokenFile = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/stopwords.txt";

        //初始化
        searcher.Init(options);

        //构造Mapping
        searcher.IndexMapping(indexName,IndexMapping());
        //insert Document
        searcher.IndexInsertOrUpdate(indexName,IndexDocument(field1,"此次百度收购将成中国互联网最大并购",field2,"谭枫"));
        searcher.IndexInsertOrUpdate(indexName,IndexDocument(field1,"百度宣布拟全资收购91无线业务",field2,"大左"));
        searcher.IndexInsertOrUpdate(indexName,IndexDocument(field1,"百度是中国最大的搜索引擎",field2,"大彭"));

        //刷新索引 强制保存
        searcher.FlushIndex();

        SearchResponse output = searcher.Search(new SearchRequest(indexName,field1,"中国百度"));

        System.out.println("output.NumDocs="+ output.NumDocs);
        for (int i = 0; i < output.NumDocs; i++) {
            System.out.println("output.DocId=" + output.Docs[i].DocId +" BM25="+output.Docs[i].Scores);
        }




    }
    private static List<SimpleFieldInfo> IndexMapping() {
        List<SimpleFieldInfo> Fields = new ArrayList<>();
        Fields.add(field1);
        Fields.add(field2);
        return Fields;
    }


    private static Map<String,Object> IndexDocument(SimpleFieldInfo field1, String str1, SimpleFieldInfo field2, String str2){
        Map<String,Object> document = new HashMap<>();
        document.put(field1.FieldName,str1);
        document.put(field2.FieldName,str2);
        return document;
    }

}
