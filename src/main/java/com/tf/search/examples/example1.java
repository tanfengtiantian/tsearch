package com.tf.search.examples;

import com.tf.search.engine.Engine;
import com.tf.search.types.DocumentIndexData;
import com.tf.search.types.EngineInitOptions;
import com.tf.search.types.SearchRequest;
import com.tf.search.types.SearchResponse;

public class example1 {

    private static Engine searcher = new Engine();
    //private static final String text1 = "在苏黎世的FIFA颁奖典礼上，巴萨球星、阿根廷国家队队长梅西赢得了生涯第5个金球奖，继续创造足坛的新纪录";
    //private static final String text2 = "12月6日，网上出现照片显示国产第五代战斗机歼-20的尾翼已经涂上五位数部队编号";

    public static void main(String[] args) {

        EngineInitOptions options = new EngineInitOptions();
        options.SegmenterDictionaries = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/CoreNatureDictionary.mini.txt";
        options.StopTokenFile = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/stopwords.txt";

        searcher.Init(options);
        //searcher.IndexDocument(1, new DocumentIndexData("中国队女排夺北京奥运会金牌重返巅峰，观众欢呼女排女排女排"), false);
        searcher.IndexDocument(1, new DocumentIndexData("此次百度收购将成中国互联网最大并购"), false);
        searcher.IndexDocument(2, new DocumentIndexData("百度宣布拟全资收购91无线业务"), false);
        searcher.IndexDocument(3, new DocumentIndexData("百度是中国最大的搜索引擎"), false);


        searcher.FlushIndex();

        SearchResponse output = searcher.Search(new SearchRequest("中国百度"));
        //SearchResponse output = searcher.Search(new SearchRequest("中国"));

        System.out.println("output.NumDocs="+ output.NumDocs);
        for (int i = 0; i < output.NumDocs; i++) {
            System.out.println("output.DocId=" + output.Docs[i].DocId +" BM25="+output.Docs[i].Scores);
        }


        //searcher.Search(new SearchRequest("百度中国引擎"));

    }

}
