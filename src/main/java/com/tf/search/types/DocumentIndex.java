package com.tf.search.types;


import java.util.List;

public class DocumentIndex {

    // 文本的DocId
    public Long DocId;

    // 文本的关键词长
    public Float TokenLength;

    // 加入的索引键
    public List<KeywordIndex> Keywords;

    public DocumentIndex(Long DocId, Float TokenLength, List<KeywordIndex> Keywords) {
        this.DocId = DocId;
        this.TokenLength = TokenLength;
        this.Keywords = Keywords;
    }

    public static class KeywordIndex{

        // 搜索键的UTF-8文本
        public String Text;

        // 搜索键词频
        public Float Frequency;

        // 搜索键在文档中的起始字节位置，按照升序排列
        public List<Integer> Starts;

        public KeywordIndex(String Text,Float Frequency,List<Integer> Starts) {
            this.Text = Text;
            this.Frequency = Frequency;
            this.Starts = Starts;

        }

    }
}
