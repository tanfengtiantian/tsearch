package com.tf.search.types;

public class IndexerInitOptions {

    // 仅存储文档的docId
    public static final int DocIdsIndex = 0;

    // 存储关键词的词频，用于计算BM25
    public static final int FrequenciesIndex = 1;

    // 存储关键词在文档中出现的具体字节位置（可能有多个）
    // 如果你希望得到关键词紧邻度数据，必须使用LocationsIndex类型的索引
    public static final int LocationsIndex = 2;

    // 默认插入索引表文档 CACHE SIZE
    public final int defaultDocCacheSize = 300;//300000

    // 索引表的类型，见上面的常数
    public int IndexType;

    // 待插入索引表文档 CACHE SIZE
    public int DocCacheSize;

    // BM25参数
    public BM25Parameters bm25Parameters;

    public IndexerInitOptions(){
        IndexType = FrequenciesIndex;
        bm25Parameters = new BM25Parameters();
    }


    public static IndexerInitOptions defaultIndexerInitOptions(){
        return new IndexerInitOptions();
    }

    public void Init() {
        if (DocCacheSize == 0) {
            DocCacheSize = defaultDocCacheSize;
        }
    }
}
