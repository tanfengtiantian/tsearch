package com.tf.search.types;

/**
 * 引擎初始化参数
 */
public class EngineInitOptions {
    // 是否使用分词器
    // 默认使用，否则在启动阶段跳过SegmenterDictionaries和StopTokenFile设置
    // 如果你不需要在引擎内分词，可以将这个选项设为true
    // 注意，如果你不用分词器，那么在调用IndexDocument时DocumentIndexData中的Content会被忽略
    public boolean NotUsingSegmenter;

    // 半角逗号分隔的字典文件，具体用法见
    // sego.Segmenter.LoadDictionary函数的注释
    public String SegmenterDictionaries;

    // 停用词文件
    public String StopTokenFile;
    // 分词器线程数
    public int NumSegmenterThreads;

    // 索引器和排序器的shard数目
    // 被检索/排序的文档会被均匀分配到各个shard中
    public int NumShards;

    // 索引器的信道缓冲长度
    public int IndexerBufferLength;

    // 索引器每个shard分配的线程数
    public int NumIndexerThreadsPerShard;

    // 排序器的信道缓冲长度
    public int RankerBufferLength;

    // 排序器每个shard分配的线程数
    public int NumRankerThreadsPerShard;

    // 索引器初始化选项
    public IndexerInitOptions IndexerInitOptions;

    // 默认的搜索选项
    public RankOptions DefaultRankOptions;

    // 是否使用持久数据库，以及数据库文件保存的目录和裂分数目
    public boolean UsePersistentStorage;
    public String PersistentStorageFolder;
    public Integer PersistentStorageShards;

    public void Init() {

        if (!NotUsingSegmenter) {
            if (SegmenterDictionaries == "") {
                System.err.println("字典文件不能为空");
                return;
            }
        }

        if (NumSegmenterThreads == 0) {
            NumSegmenterThreads = Runtime.getRuntime().availableProcessors();
        }

        if (NumShards == 0) {
            NumShards = 2;
        }

        if (IndexerBufferLength == 0) {
            IndexerBufferLength = Runtime.getRuntime().availableProcessors();
        }

        if (NumIndexerThreadsPerShard == 0) {
            NumIndexerThreadsPerShard = 1;//Runtime.getRuntime().availableProcessors();
        }

        if (RankerBufferLength == 0) {
            RankerBufferLength = Runtime.getRuntime().availableProcessors();
        }

        if (NumRankerThreadsPerShard == 0) {
            NumRankerThreadsPerShard = Runtime.getRuntime().availableProcessors();
        }

        if (IndexerInitOptions == null) {
            IndexerInitOptions = IndexerInitOptions.defaultIndexerInitOptions();
        }

        if (DefaultRankOptions == null) {
            DefaultRankOptions = RankOptions.defaultDefaultRankOptions();
        }
        if (DefaultRankOptions.ScoringCriteria == null) {
            DefaultRankOptions.ScoringCriteria = RankOptions.defaultDefaultScoringCriteria();
        }


        if (PersistentStorageShards == null) {
            PersistentStorageShards = 8;
        }
    }
}
