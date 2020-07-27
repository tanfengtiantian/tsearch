package com.tf.search.engine.indexerworker;

import com.tf.search.core.Indexer;
import com.tf.search.engine.Engine;
import com.tf.search.engine.indexerworker.entry.IndexerLookupEntry;
import com.tf.search.engine.rankerworker.entry.RankerRankEntry;
import com.tf.search.engine.rankerworker.entry.RankerReturnEntry;
import com.tf.search.types.*;
import com.tf.search.utils.Utils;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class IndexerLookupRequest implements Runnable , Closeable {

    private BlockingQueue<IndexerLookupEntry> works;

    private final Engine engine;

    private volatile boolean isRuntime = false;

    private int shard;

    public IndexerLookupRequest(Engine engine, int indexerBufferLength) {
        this.engine = engine;
        works = new ArrayBlockingQueue<>(indexerBufferLength);
    }

    public void indexerLookupWorker(int shard) {
        this.shard = shard;
        Utils.newThread("IndexerLookup-shard-" + shard, this, true).start();
    }

    public boolean addLookupWork(IndexerLookupEntry entry) {
        try {
            works.put(entry);
            return true;
        } catch (InterruptedException e) {
            return false;
        }

    }

    @Override
    public void run() {
        isRuntime = true;
        while (isRuntime) {
            try {
                IndexerLookupEntry request = works.take();
                Indexer.SearchResult result = null;
                SimpleFieldInfo field = request.field;
                if(field != null) {
                    switch (field.FieldType){
                        case IDX_TYPE_STRING:
                            //字符型索引[全词匹配] 不处理
                            break;
                        case IDX_TYPE_STRING_SEG:
                            //字符型索引[切词匹配，全文索引,hash存储倒排]
                            if (request.docIds == null) {
                                result = engine.idxManagers.get(shard).get(request.IndexName).Lookup(field, request.tokens, request.labels, null, request.countDocsOnly);
                            } else {
                                result = engine.idxManagers.get(shard).get(request.IndexName).Lookup(field, request.tokens, request.labels, request.docIds, request.countDocsOnly);
                            }
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


                if(result == null) {
                    request.rankerReturnRequest.addRankerReturn(new RankerReturnEntry());
                    continue;
                }
                List<IndexedDocument> docs = result.docs;
                int numDocs = result.numDocs;
                // 返回总数
                if (request.countDocsOnly) {
                    RankerReturnEntry entry = new RankerReturnEntry();
                    entry.numDocs = numDocs;
                    request.rankerReturnRequest.addRankerReturn(entry);
                    continue;
                }
                // 返回空结果
                if (docs.size() == 0) {
                    request.rankerReturnRequest.addRankerReturn(new RankerReturnEntry());
                    continue;
                }
                // 不排序直接返回结果
                if (request.orderless) {
                    List<ScoredDocument> outputDocs = new ArrayList<>();
                    docs.forEach(d->{
                        ScoredDocument scored = new ScoredDocument();
                        scored.DocId = d.DocId;
                        scored.TokenLocations = d.TokenLocations;
                        scored.TokenSnippetLocations = d.TokenSnippetLocations;
                        outputDocs.add(scored);
                    });
                    RankerReturnEntry entry = new RankerReturnEntry();
                    entry.docs = outputDocs.toArray(new ScoredDocument[0]);
                    entry.numDocs = outputDocs.size();
                    request.rankerReturnRequest.addRankerReturn(entry);
                    continue;
                }
                // send 排序器处理->返回结果
                /*
                List<ScoredDocument> outputDocs = new ArrayList<>();
                docs.forEach(d->{
                    ScoredDocument scored = new ScoredDocument();
                    scored.DocId = d.DocId;
                    scored.TokenLocations = d.TokenLocations;
                    scored.TokenSnippetLocations = d.TokenSnippetLocations;
                    outputDocs.add(scored);
                });
                RankerReturnRequest.Entry returnEntry = new RankerReturnRequest.Entry();
                returnEntry.docs = outputDocs.toArray(new ScoredDocument[0]);
                returnEntry.numDocs = outputDocs.size();
                 */
                //
                RankerRankEntry rankEntry = new RankerRankEntry();
                rankEntry.IndexName = request.IndexName;
                rankEntry.countDocsOnly = request.countDocsOnly;
                rankEntry.docs = docs;
                rankEntry.options = request.options == null ? RankOptions.defaultDefaultRankOptions() : request.options;
                rankEntry.rankerReturnRequest = request.rankerReturnRequest;

                engine.rankerRankChannels.get(shard).addRankWorker(rankEntry);


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void close() throws IOException {
        isRuntime = false;
    }

}
