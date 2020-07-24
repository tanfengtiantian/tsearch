package com.tf.search.types;

import java.util.List;
import java.util.Map;

public class SearchRequest {
    //索引名称
    public String IndexName;

    // 搜索的短语（必须是UTF-8格式），会被分词
    // 当值为空字符串时关键词会从下面的Tokens读入
    public String Text;

    // 关键词（必须是UTF-8格式），当Text不为空时优先使用Text
    // 通常你不需要自己指定关键词，除非你运行自己的分词程序
    public List<String> Tokens;

    // 文档标签（必须是UTF-8格式），标签不存在文档文本中，但也属于搜索键的一种
    public List<String> Labels;

    // 当不为nil时，仅从这些DocIds包含的键中搜索（忽略值）
    public Map<Long,Boolean> DocIds;

    // 排序选项
    public RankOptions RankOptions;
    // 超时，单位毫秒（千分之一秒）。此值小于等于零时不设超时。
    // 搜索超时的情况下仍有可能返回部分排序结果。
    public int Timeout;

    // 设为true时仅统计搜索到的文档个数，不返回具体的文档
    public boolean CountDocsOnly;

    // 不排序，对于可在引擎外部（比如客户端）排序情况适用
    // 对返回文档很多的情况打开此选项可以有效节省时间
    public boolean Orderless;

    public SearchRequest(String IndexName, String text) {
        this.IndexName = IndexName;
        this.Text = text;
    }

}
