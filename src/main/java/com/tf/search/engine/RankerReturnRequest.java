package com.tf.search.engine;


import com.tf.search.engine.rankerworker.entry.RankerReturnEntry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RankerReturnRequest {

    public BlockingQueue<RankerReturnEntry> rankerReturn;

    public RankerReturnRequest(int numShards) {
        rankerReturn = new ArrayBlockingQueue<>(numShards);
    }

    public boolean addRankerReturn(RankerReturnEntry entry){
        try {
            rankerReturn.put(entry);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public RankerReturnEntry result() {
        try {
            return rankerReturn.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

}
