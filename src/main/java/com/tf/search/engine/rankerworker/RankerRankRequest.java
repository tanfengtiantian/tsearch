package com.tf.search.engine.rankerworker;

import com.tf.search.engine.Engine;
import com.tf.search.engine.RankerReturnRequest;
import com.tf.search.engine.rankerworker.entry.RankerRankEntry;
import com.tf.search.engine.rankerworker.entry.RankerReturnEntry;
import com.tf.search.utils.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RankerRankRequest implements Runnable , Closeable {

    private BlockingQueue<RankerRankEntry> works;

    private final Engine engine;

    private volatile boolean isRuntime = false;

    private int shard;

    public RankerRankRequest(Engine engine, int rankerBufferLength) {
        this.engine = engine;
        works = new ArrayBlockingQueue<>(rankerBufferLength);
    }

    public void rankerRankWorker(int shard) {
        this.shard = shard;
        Utils.newThread("RankerRankRequest-shard-" + shard, this, true).start();
    }


    public boolean addRankWorker(RankerRankEntry entry) {
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
                RankerRankEntry request = works.take();
                //排序
                if (request.options.MaxOutputs != 0) {
                    request.options.MaxOutputs += request.options.OutputOffset;
                }
                request.options.OutputOffset = 0;
                RankerReturnEntry entry= engine.rankers.get(shard).Rank(request.docs, request.options, request.countDocsOnly);
                //排序end
                request.rankerReturnRequest.addRankerReturn(entry);

            } catch (Exception e) {

            } finally {

            }
        }
    }

    @Override
    public void close() throws IOException {
        isRuntime = false;
    }

}
