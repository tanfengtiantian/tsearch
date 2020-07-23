package com.tf.search.examples;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;
import com.tf.search.engine.Engine;
import com.tf.search.types.DocumentIndexData;
import com.tf.search.types.EngineInitOptions;
import com.tf.search.types.SearchRequest;

import java.util.List;

public class example3 {

    private static Engine searcher = new Engine();
    private static final String text1 = "在苏黎世的FIFA颁奖典礼上，巴萨球星、阿根廷国家队队长梅西赢得了生涯第5个金球奖，继续创造足坛的新纪录";
    private static final String text2 = "12月6日，网上出现照片显示国产第五代战斗机歼-20的尾翼已经涂上五位数部队编号";

    public static void main(String[] args) {

        EngineInitOptions options = new EngineInitOptions();
        options.SegmenterDictionaries = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/CoreNatureDictionary.mini.txt";
        options.StopTokenFile = "/Users/zxcs/Desktop/tf/HanLP-1.x/data/dictionary/stopwords.txt";

        searcher.Init(options);

        List<Term> list = NLPTokenizer.segment("中国队女排夺北京奥运会金牌重返巅峰，观众欢呼女排女排女排！");
        list.forEach(term -> {
            System.out.println("word="+ term.word+" offset="+ term.offset);
            System.out.println("Frequency="+ term.getFrequency());

        });

    }

}
