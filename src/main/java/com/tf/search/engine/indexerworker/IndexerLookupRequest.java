package com.tf.search.engine.indexerworker;

import com.tf.search.core.Indexer;
import com.tf.search.engine.Engine;
import com.tf.search.engine.RankerReturnRequest;
import com.tf.search.engine.indexerworker.entry.IndexerLookupEntry;
import com.tf.search.engine.rankerworker.entry.RankerRankEntry;
import com.tf.search.engine.rankerworker.entry.RankerReturnEntry;
import com.tf.search.types.IndexedDocument;
import com.tf.search.types.RankOptions;
import com.tf.search.types.ScoredDocument;
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

                Indexer.SearchResult result;
                if (request.docIds == null) {
                    result = engine.indexers.get(shard).Lookup(request.tokens, request.labels, null, request.countDocsOnly);
                } else {
                    result = engine.indexers.get(shard).Lookup(request.tokens, request.labels, request.docIds, request.countDocsOnly);
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
