package com.tf.search.engine.rankerworker;

import com.tf.search.engine.Engine;
import com.tf.search.engine.indexerworker.entry.IndexerAddEntry;
import com.tf.search.engine.rankerworker.entry.RankerAddEntry;
import com.tf.search.utils.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RankerAddDocRequest implements Runnable , Closeable {

    private BlockingQueue<RankerAddEntry> works;

    private final Engine engine;

    private volatile boolean isRuntime = false;

    private int shard;

    public RankerAddDocRequest(Engine engine, int rankerBufferLength) {
        this.engine = engine;
        works = new ArrayBlockingQueue<>(rankerBufferLength);
    }

    public void rankerAddDocWorker(int shard) {
        this.shard = shard;
        Utils.newThread("RankerAddDoc-shard-" + shard, this, true).start();
    }

    public boolean addWork(RankerAddEntry entry) {
        try {
            works.put(entry);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        isRuntime = false;
    }

    @Override
    public void run() {
        isRuntime = true;
        while (isRuntime) {
            try {
                RankerAddEntry request = works.take();
                engine.ranManagers.get(shard).get(request.IndexName).AddDoc(request.docId, request.fields);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
