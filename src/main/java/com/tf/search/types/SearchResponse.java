package com.tf.search.types;

public class SearchResponse {

    // 搜索用到的关键词
    public String[] Tokens;

    // 搜索到的文档，已排序
    public ScoredDocument[] Docs;

    // 搜索是否超时。超时的情况下也可能会返回部分结果
    public boolean Timeout;

    // 搜索到的文档个数。注意这是全部文档中满足条件的个数，可能比返回的文档数要大
    public int NumDocs;
}
