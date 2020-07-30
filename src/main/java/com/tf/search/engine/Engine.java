package com.tf.search.engine;

import com.hankcs.hanlp.seg.common.Term;
import com.tf.search.core.Indexer;
import com.tf.search.engine.indexerworker.entry.IndexerLookupEntry;
import com.tf.search.engine.indexerworker.IndexerAddDocumentRequest;
import com.tf.search.engine.indexerworker.IndexerLookupRequest;
import com.tf.search.engine.indexerworker.IndexerRemoveDocRequest;
import com.tf.search.engine.rankerworker.RankerAddDocRequest;
import com.tf.search.engine.rankerworker.RankerRankRequest;
import com.tf.search.engine.rankerworker.RankerRemoveDocRequest;
import com.tf.search.engine.rankerworker.entry.RankerReturnEntry;
import com.tf.search.engine.segment.Segmenter;
import com.tf.search.engine.segment.SegmenterRequest;
import com.tf.search.engine.segment.StopTokens;
import com.tf.search.engine.segment.entry.SegmenterEntry;
import com.tf.search.types.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
/**
 * 检索引擎
 */
public class Engine {

    private final long NumNanosecondsInAMillisecond = 1000000;

    private final String PersistentStorageFilePrefix = "tanfeng";

    // 计数器，用来统计有多少文档被索引等信息
    public AtomicLong numDocumentsIndexed = new AtomicLong(0);
    public AtomicLong numDocumentsRemoved = new AtomicLong(0);
    public AtomicLong numDocumentsForceUpdated = new AtomicLong(0);
    public AtomicLong numIndexingRequests = new AtomicLong(0);
    public AtomicLong numRemovingRequests = new AtomicLong(0);
    public AtomicLong numForceUpdatingRequests = new AtomicLong(0);
    public AtomicLong numTokenIndexAdded = new AtomicLong(0);
    public AtomicLong numDocumentsStored = new AtomicLong(0);

    // 记录初始化参数
    public EngineInitOptions initOptions;

    private volatile boolean initialized;

    public List<Map<String,Indexer>> idxManagers = new ArrayList<>();

    public Map<String, Map<String,SimpleFieldInfo>> indexFields = new HashMap<>();

    public Map<String,AtomicLong> indexDocids = new HashMap<>();

    public List<Map<String,Ranker>> ranManagers = new ArrayList<>();

    public Segmenter segmenter = new Segmenter();

    public StopTokens stopTokens = new StopTokens();

    // 建立索引器使用的通信通道
    private SegmenterRequest segmenterChannel;
    public List<IndexerAddDocumentRequest> indexerAddDocChannels;
    public List<IndexerRemoveDocRequest> indexerRemoveDocChannels;
    public List<RankerAddDocRequest> rankerAddDocChannels;

    // 建立排序器使用的通信通道
    public List<IndexerLookupRequest> indexerLookupChannels;
    public List<RankerRankRequest> rankerRankChannels;
    public List<RankerRemoveDocRequest> rankerRemoveDocChannels;


    public void Init(EngineInitOptions options){

        // 验证参数有
        options.Init();

        this.initOptions = options;

        this.initialized = true;


        if (!options.NotUsingSegmenter) {
            // 载入分词器词典
            segmenter.LoadDictionary(options.SegmenterDictionaries);

            // 初始化停用词
            stopTokens.Init(options.StopTokenFile);
        }

        // 初始化索引器和排序器
        for (int shard = 0; shard < options.NumShards; shard++) {

            idxManagers.add(new HashMap<>());

            //indexers.add(new Indexer());
            //indexers.get(shard).Init(options.IndexerInitOptions);

            ranManagers.add(new HashMap<>());
            //rankers.add(new Ranker());
            //rankers.get(shard).Init(options.IndexerInitOptions);
        }

        // 初始化分词器通道
        segmenterChannel = new SegmenterRequest(this, options.NumSegmenterThreads);

        // 初始化索引器通道
        indexerAddDocChannels = new ArrayList<>(options.NumShards);
        indexerRemoveDocChannels = new ArrayList<>(options.NumShards);
        indexerLookupChannels = new ArrayList<>(options.NumShards);
        for (int shard = 0; shard < options.NumShards ; shard++) {
            indexerAddDocChannels.add(new IndexerAddDocumentRequest(this,options.IndexerBufferLength));
            indexerRemoveDocChannels.add(new IndexerRemoveDocRequest(this,options.IndexerBufferLength));
            indexerLookupChannels.add(new IndexerLookupRequest(this,options.IndexerBufferLength));
        }

        // 初始化排序器通道
        rankerAddDocChannels = new ArrayList<>(options.NumShards);
        rankerRankChannels = new ArrayList<>(options.NumShards);
        rankerRemoveDocChannels = new ArrayList<>(options.NumShards);
        for (int shard = 0; shard < options.NumShards ; shard++) {
            rankerAddDocChannels.add(new RankerAddDocRequest(this,options.RankerBufferLength));
            rankerRankChannels.add(new RankerRankRequest(this,options.RankerBufferLength));
            rankerRemoveDocChannels.add(new RankerRemoveDocRequest(this,options.RankerBufferLength));
        }
        // 初始化持久化存储通道

        // 存储通道 end

        // 启动分词器
        for (int iThread = 0; iThread < options.NumSegmenterThreads; iThread++) {
            // go
            segmenterChannel.segmenterWorker(iThread);
        }


        // 启动索引器和排序器
        for (int shard = 0; shard < options.NumShards; shard++) {
            // go
            indexerAddDocChannels.get(shard).indexerAddDocumentWorker(shard);
            indexerRemoveDocChannels.get(shard).indexerRemoveDocWorker(shard);
            rankerAddDocChannels.get(shard).rankerAddDocWorker(shard);
            rankerRemoveDocChannels.get(shard).rankerRemoveDocWorker(shard);
            for (int i = 0; i < options.NumIndexerThreadsPerShard; i++) {
                indexerLookupChannels.get(shard).indexerLookupWorker(shard);
            }
            for (int i = 0; i < options.NumIndexerThreadsPerShard; i++) {
                rankerRankChannels.get(shard).rankerRankWorker(shard);
            }
        }
        // 启动持久化存储工作协程

        // 存储通道 end
        numDocumentsStored.getAndAdd(numIndexingRequests.get());
    }

    public boolean IndexMapping(String IndexName, List<SimpleFieldInfo> Fields) {
        boolean ok = checkIndexMap(IndexName);
        if(!ok) return false;

        //FieldInfo
        Map<String,SimpleFieldInfo> map = new HashMap<>();
        Fields.forEach(f-> map.put(f.FieldName,f));
        indexFields.put(IndexName,map);

        //docid
        indexDocids.put(IndexName,new AtomicLong(1));

        // 初始化
        for (int shard = 0; shard < initOptions.NumShards; shard++) {
            //索引
            Indexer indexer = new Indexer();
            indexer.Name = IndexName;
            indexer.Init(initOptions.IndexerInitOptions);
            idxManagers.get(shard).put(IndexName,indexer);
            Fields.forEach(f-> indexer.Fields.put(f.FieldName,f));

            //排序
            Ranker ranker = new Ranker();
            ranker.Init(initOptions.IndexerInitOptions);
            ranManagers.get(shard).put(IndexName,ranker);
        }
        return true;
    }

    public void IndexInsertOrUpdate(String indexName, Map<String,Object> document) {
        Map<String,SimpleFieldInfo> fieldInfos = indexFields.get(indexName);
        //doc 自增
        long docId = indexDocids.get(indexName).getAndIncrement();
        document.forEach((field,value) -> {
            SimpleFieldInfo fieldInfo = fieldInfos.get(field);
            if(fieldInfo != null) {
                switch (fieldInfo.FieldType){
                    case IDX_TYPE_STRING:
                       //字符型索引[全词匹配] 不处理
                        IndexSegmenterDocument(indexName, fieldInfo, docId, new DocumentIndexData(String.valueOf(value)),false,false);
                        break;
                    case IDX_TYPE_STRING_SEG:
                        //字符型索引[切词匹配，全文索引,hash存储倒排]
                        IndexSegmenterDocument(indexName, fieldInfo, docId, new DocumentIndexData(String.valueOf(value)),false,true);
                        break;
                    case IDX_TYPE_STRING_LIST:
                        //字符型索引[列表类型，分号切词，直接切分,hash存储倒排]
                        break;
                    case IDX_TYPE_STRING_SINGLE:
                        //字符型索引[单字切词] 不处理
                        break;
                    case IDX_TYPE_NUMBER:
                        //数字型索引，只支持整数，数字型索引只建立正排 不处理
                    case IDX_TYPE_DATE:
                        //日期型索引 '2015-11-11 00:11:12'，日期型只建立正排，转成时间戳存储 不处理
                        return;
                }
            }
        });
    }

    /**
     * 默认添加分词任务
     * @param indexName
     * @param field
     * @param docId
     * @param data
     * @param forceUpdate
     */
    public void IndexSegmenterDocument(String indexName, SimpleFieldInfo field, Long docId, DocumentIndexData data, boolean forceUpdate,boolean Segmenter) {
        boolean ok = checkIndexMap(indexName);
        if(ok) return;

        // 分词器通道 ->	NumSegmenterThreads 分词线程数   segmenterWorker 工作线程
        // 构建索引器和排序器
        internalIndexDocument(indexName, field, docId, data, forceUpdate,Segmenter);
        int hash =  (String.valueOf(docId).hashCode() & Integer.MAX_VALUE) % initOptions.PersistentStorageShards;
        if (initOptions.UsePersistentStorage && docId != 0) {
            //写入文件
            //engine.persistentStorageIndexDocumentChannels[hash] <- persistentStorageIndexDocumentRequest{docId: docId, data: data}
        }
    }

    private void internalIndexDocument(String indexName, SimpleFieldInfo field, Long docId, DocumentIndexData data, boolean forceUpdate, boolean segmenter) {
        if (!initialized) {
            System.err.println("必须先初始化引擎");
            return;
        }

        if (docId != 0) {
            numIndexingRequests.incrementAndGet();
        }
        if (forceUpdate) {
            numForceUpdatingRequests.incrementAndGet();
        }

        int hash = (docId + data.Content).hashCode() & Integer.MAX_VALUE;
        if(segmenter) segmenterChannel.registerWork(new SegmenterEntry(indexName,field,docId,hash,data,forceUpdate));
        else segmenterChannel.registerNoSegmenterWork(new SegmenterEntry(indexName,field,docId,hash,data,forceUpdate));

    }

    // 阻塞等待直到所有索引添加完毕
    public void FlushIndex() {
        for(;;) {
            // 让出当前cpu时间片给其他线程
            Thread.currentThread().yield();
            if (numIndexingRequests.get() == numDocumentsIndexed.get() &&
                    numRemovingRequests.get() * initOptions.NumShards == numDocumentsRemoved.get() &&
                    (!initOptions.UsePersistentStorage || numIndexingRequests.get() == numDocumentsStored.get())) {
                // 保证 CHANNEL 中 REQUESTS 全部被执行完
                break;
            }
        }

        // 强制更新，保证其为最后的请求
        internalIndexDocument("",null,0L, new DocumentIndexData(""), true,true);

        for(;;) {
            // 让出当前cpu时间片给其他线程
            Thread.currentThread().yield();
            // 同步请求更新数量 * Shard == 文档实际更新数量
            if(numForceUpdatingRequests.get() * initOptions.NumShards == numDocumentsForceUpdated.get()){
                break;
            }
        }
        System.out.println("indexer cache force");


        idxManagers.get(0).values().forEach(index-> index.f_tableLock.forEach((k,v)->{
            System.out.println("FieldName: " +k);
            v.table.forEach((t,key)->{
                System.out.println("shard0--"+t+"--"+key.docIds);
            });
        }));

        idxManagers.get(1).values().forEach(index-> index.f_tableLock.forEach((k,v)->{
            System.out.println("FieldName: " +k);
            v.table.forEach((t,key)->{
                System.out.println("shard1--"+t+"--"+key.docIds);
            });
        }));

    }

    // 查找满足搜索条件的文档，此函数线程安全
    public SearchResponse Search(SearchRequest request) {

        if (!initialized) {
            System.err.println("必须先初始化引擎");
            return null;
        }
        SearchResponse output = new SearchResponse();
        //初始化排序规则
        RankOptions rankOptions;
        if (request.RankOptions == null) {
            rankOptions = initOptions.DefaultRankOptions;
        } else {
            rankOptions = request.RankOptions;
        }
        if (rankOptions.ScoringCriteria == null) {
            rankOptions.ScoringCriteria = initOptions.DefaultRankOptions.ScoringCriteria;
        }

        List<String> tokens = new ArrayList<>();
        // 收集关键词
        if(request.Text != "") {
            List<Term> querySegments = segmenter.Segment(request.Text);
            querySegments.forEach(s->{
                String token = s.word;
                if (!stopTokens.IsStopToken(token)) {
                    tokens.add(token);
                }
            });
        } else {
            if(request.Labels != null) {
                request.Labels.forEach(t -> tokens.add(t));

            }
        }

        // 建立排序器返回的通信通道----->回调函数
        //rankerReturnChannel := make(chan rankerReturnRequest, engine.initOptions.NumShards)
        // 生成查找请求
        IndexerLookupEntry lookupRequest = new IndexerLookupEntry();
        lookupRequest.IndexName = request.IndexName;
        lookupRequest.field = request.field;
        lookupRequest.countDocsOnly = request.CountDocsOnly;
        lookupRequest.tokens = tokens;
        lookupRequest.labels = request.Labels;
        lookupRequest.docIds = request.DocIds;
        lookupRequest.options = rankOptions;
        lookupRequest.rankerReturnRequest = new RankerReturnRequest(initOptions.NumShards);
        lookupRequest.orderless = request.Orderless;


        // 向索引器发送查找请求
        for (int shard = 0; shard < initOptions.NumShards; shard++) {
            indexerLookupChannels.get(shard).addLookupWork(lookupRequest);
        }

        // 从通信通道读取排序器的输出
        int numDocs = 0;
        List<ScoredDocument> rankOutput = new ArrayList<>();
        int timeout = request.Timeout;
        boolean isTimeout = false;
        if (timeout <= 0) {
            // 不设置超时
            for (int shard = 0; shard < initOptions.NumShards; shard++) {
                RankerReturnEntry rankerOutput = lookupRequest.rankerReturnRequest.result();
                if (!request.CountDocsOnly && rankerOutput.docs != null) {
                    for (int i = 0; i <rankerOutput.docs.length ; i++) {
                        rankOutput.add(rankerOutput.docs[i]);
                    }
                }
                numDocs += rankerOutput.numDocs;
            }
        } else {
            // 设置超时
        }

        // 再排序
        if (!request.CountDocsOnly && !request.Orderless) {
            if (rankOptions.ReverseOrder) {
                rankOutput.sort(Comparator.comparing(o -> o.DocId));
            } else {//倒序
                rankOutput.sort((o1,o2)->o2.DocId.compareTo(o1.DocId));
            }
        }

        // 准备输出
        output.Tokens = tokens.toArray(new String[0]);
        // 仅当CountDocsOnly为false时才充填output.Docs
        if (!request.CountDocsOnly) {
            if (request.Orderless) {
                // 无序状态无需对Offset截断
                output.Docs = rankOutput.toArray(new ScoredDocument[0]);
            } else {
                //排序
                output.Docs = rankOutput.toArray(new ScoredDocument[0]);
            }
        }
        output.NumDocs = numDocs;
        output.Timeout = isTimeout;

        return output;
    }

    private boolean checkIndexMap(String indexName) {
        if(indexFields.containsKey(indexName)){
            return false;
        }
        return true;
    }

    /**
     * 从文本hash得到要分配到的shard
     * @param hash
     * @return
     */
    public int getShard(int hash) {
        return 0;
        //return hash - hash/initOptions.NumShards * initOptions.NumShards;
    }

}
