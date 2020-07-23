package com.tf.search.types;

import java.util.List;

public class IndexedDocument {

    public Long DocId;

    // BM25，仅当索引类型为FrequenciesIndex或者LocationsIndex时返回有效值
    public Float BM25;

    // 关键词在文档中的紧邻距离，紧邻距离的含义见computeTokenProximity的注释。
    // 仅当索引类型为LocationsIndex时返回有效值。
    public Integer TokenProximity;

    // 紧邻距离计算得到的关键词位置，和Lookup函数输入tokens的长度一样且一一对应。
    // 仅当索引类型为LocationsIndex时返回有效值。
    public List<Integer> TokenSnippetLocations;

    // 关键词在文本中的具体位置。
    // 仅当索引类型为LocationsIndex时返回有效值。
    public List<Integer[]> TokenLocations;
}
