package com.tf.search.engine.indexerworker;

import com.tf.search.engine.Engine;
import com.tf.search.engine.indexerworker.entry.IndexerAddEntry;
import com.tf.search.utils.Utils;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class IndexerAddDocumentRequest implements Runnable , Closeable {

    private BlockingQueue<IndexerAddEntry> works;

    private final Engine engine;

    private volatile boolean isRuntime = false;

    private int shard;

    public IndexerAddDocumentRequest(Engine engine, int indexerBufferLength) {
        this.engine = engine;
        works = new ArrayBlockingQueue<>(indexerBufferLength);
    }

    public boolean addWork(IndexerAddEntry entry) {
        try {
            works.put(entry);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 启动新增索引线程
     * @param shard
     */
    public void indexerAddDocumentWorker(int shard) {
        this.shard = shard;
        Utils.newThread("IndexerAddDocument-shard-" + shard, this, true).start();
    }

    @Override
    public void run() {
        isRuntime = true;
        while (isRuntime) {
            try {
                IndexerAddEntry request = works.take();
                engine.indexers.get(shard).AddDocumentToCache(request.document, request.forceUpdate);
                // 计数器新增
                if (request.document != null) {
                    engine.numTokenIndexAdded.getAndAdd(request.document.Keywords.size());
                    engine.numDocumentsIndexed.getAndIncrement();
                }
                if (request.forceUpdate) {
                    engine.numDocumentsForceUpdated.getAndIncrement();
                }
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
