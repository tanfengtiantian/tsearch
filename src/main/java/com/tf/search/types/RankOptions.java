package com.tf.search.types;

public class RankOptions {
    // 文档的评分规则，值为nil时使用Engine初始化时设定的规则
    public ScoringCriteria ScoringCriteria;

    // 默认情况下（ReverseOrder=false）按照分数从大到小排序，否则从小到大排序
    public boolean ReverseOrder;

    // 从第几条结果开始输出
    public int OutputOffset;

    // 最大输出的搜索结果数，为0时无限制
    public int MaxOutputs;

    public static RankOptions defaultDefaultRankOptions(){
        return new RankOptions();
    }

    public static ScoringCriteria defaultDefaultScoringCriteria(){
        return new RankByBM25();
    }
}
