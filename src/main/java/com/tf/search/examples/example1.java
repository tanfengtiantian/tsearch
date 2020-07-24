package com.tf.search.examples;

import com.tf.search.engine.Engine;
import com.tf.search.types.*;
import org.eclipse.jetty.util.Fields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class example1 {

    private static Engine searcher = new Engine();
    //private static final String text1 = "在苏黎世的FIFA颁奖典礼上，巴萨球星、阿根廷国家队队长梅西赢得了生涯第5个金球奖，继续创造足坛的新纪录";
    //private static final String text2 = "12月6日，网上出现照片显示国产第五代战斗机歼-20的尾翼已经涂上五位数部队编号";

    private static final String indexName = "xinwen";

    private static final String fields = "Content";


    public static void main(String[] args) {

        EngineInitOptions options = new EngineInitOptions();
        options.SegmenterDictionaries = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/CoreNatureDictionary.mini.txt";
        options.StopTokenFile = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/stopwords.txt";

        //初始化
        searcher.Init(options);

        //构造Mapping
        searcher.IndexMapping(indexName,IndexMapping());
        //insert Document
        searcher.IndexInsertOrUpdate(indexName,IndexDocument(fields,"此次百度收购将成中国互联网最大并购"));
        searcher.IndexInsertOrUpdate(indexName,IndexDocument(fields,"百度宣布拟全资收购91无线业务"));
        searcher.IndexInsertOrUpdate(indexName,IndexDocument(fields,"百度是中国最大的搜索引擎"));

        //刷新索引 强制保存
        searcher.FlushIndex();

        SearchResponse output = searcher.Search(new SearchRequest(indexName,"中国百度"));

        System.out.println("output.NumDocs="+ output.NumDocs);
        for (int i = 0; i < output.NumDocs; i++) {
            System.out.println("output.DocId=" + output.Docs[i].DocId +" BM25="+output.Docs[i].Scores);
        }

    }
    private static List<SimpleFieldInfo> IndexMapping() {
        List<SimpleFieldInfo> Fields = new ArrayList<>();
        SimpleFieldInfo content = new SimpleFieldInfo();
        content.FieldType = IdxType.IDX_TYPE_STRING_SEG;
        content.FieldName = fields;
        Fields.add(content);
        return Fields;
    }


    private static Map<String,Object> IndexDocument(String field, String content){
        Map<String,Object> document = new HashMap<>();
        document.put(field,content);
        return document;
    }

}
