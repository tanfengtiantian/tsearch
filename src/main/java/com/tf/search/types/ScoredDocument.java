package com.tf.search.types;

import java.util.List;

public class ScoredDocument {

    public Long DocId;

    // 文档的打分值
    // 搜索结果按照Scores的值排序，先按照第一个数排，如果相同则按照第二个数排序，依次类推。
    public List<Float> Scores;

    // 用于生成摘要的关键词在文本中的字节位置，该切片长度和SearchResponse.Tokens的长度一样
    // 只有当IndexType == LocationsIndex时不为空
    public List<Integer> TokenSnippetLocations;

    // 关键词出现的位置
    // 只有当IndexType == LocationsIndex时不为空
    public List<Integer[]> TokenLocations;
}
