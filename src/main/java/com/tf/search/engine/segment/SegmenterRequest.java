package com.tf.search.engine.segment;

import com.hankcs.hanlp.seg.common.Term;
import com.tf.search.engine.Engine;
import com.tf.search.engine.indexerworker.entry.IndexerAddEntry;
import com.tf.search.engine.rankerworker.entry.RankerAddEntry;
import com.tf.search.engine.segment.entry.SegmenterEntry;
import com.tf.search.types.DocumentIndex;
import com.tf.search.types.DocumentIndexData;
import com.tf.search.utils.Utils;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SegmenterRequest {
    /**
     * 分词处理器
     */
    private SegmenterProcessor[] processors;

    private final AtomicInteger currentProcessor = new AtomicInteger(0);

    private final Engine engine;
    private final int numSegmenterThreads;

    public SegmenterRequest(Engine engine,int numSegmenterThreads) {
        this.engine = engine;
        this.numSegmenterThreads = numSegmenterThreads;
        this.processors = new SegmenterProcessor[numSegmenterThreads];

    }

    /**
     * 启动分词线程
     */
    public void segmenterWorker(int work) {
        processors[work] = new SegmenterProcessor();
        Utils.newThread("Segmenter-Processor-" + work, processors[work], true).start();
    }

    /**
     * 注册任务
     * @param request
     * @throws InterruptedException
     */
    public void registerWork(SegmenterEntry request) {
        int current = currentProcessor.getAndIncrement() % (numSegmenterThreads - 1);
        try {
            processors[current].accept(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void singleRegisterWork(SegmenterEntry request) {
        try {
            processors[0].accept(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class SegmenterProcessor implements Runnable , Closeable {
        private volatile boolean isRuntime = false;
        private BlockingQueue<SegmenterEntry> works;
        public SegmenterProcessor() {
            works = new ArrayBlockingQueue<>(10);
        }
        @Override
        public void run() {
            isRuntime = true;
            while (isRuntime) {
                try {
                    SegmenterEntry request = works.take();
                    //hash 分词后 发送 索引存储
                    int shard = engine.getShard(request.hash);
                    if (request.docId == 0) {
                        if (request.forceUpdate) {
                            for (int i = 0; i < engine.initOptions.NumShards; i++) {
                                engine.indexerAddDocChannels.get(i).addWork(new IndexerAddEntry(true));
                            }
                        }
                        return;
                    }
                    Map<String, List<Integer>> tokensMap = new HashMap<>();
                    int numTokens = 0;
                    if (!engine.initOptions.NotUsingSegmenter && request.data.Content != "") {
                        // 当文档正文不为空时，优先从内容分词中得到关键词
                        List<Term> segments = engine.segmenter.Segment(request.data.Content);
                        segments.forEach(segment -> {
                            String token = segment.word;
                            if (!engine.stopTokens.IsStopToken(segment)) {
                                List<Integer> offsets = tokensMap.get(token);
                                if(offsets == null) offsets = new ArrayList<>();
                                offsets.add(segment.offset);
                                tokensMap.put(token,offsets);

                            }
                        });
                        numTokens = segments.size();
                    } else {
                        // 否则载入用户输入的关键词
                        request.data.TokenData.forEach(t ->{
                            if (!engine.stopTokens.IsStopToken(t.Text)) {
                                tokensMap.put(t.Text,t.Locations);
                            }
                        });
                        numTokens = request.data.TokenData.size();
                    }

                    // 加入非分词的文档标签
                    request.data.Labels.forEach(label -> {
                        if (!engine.initOptions.NotUsingSegmenter) {
                            if (!engine.stopTokens.IsStopToken(label)) {
                                // 当正文中已存在关键字时，若不判断，位置信息将会丢失
                                if(!tokensMap.containsKey(label)) {
                                    tokensMap.put(label,new ArrayList<>());
                                }
                            }
                        }else {
                            // 当正文中已存在关键字时，若不判断，位置信息将会丢失
                            if(!tokensMap.containsKey(label)) {
                                tokensMap.put(label,new ArrayList<>());
                            }
                        }
                    });

                    // 构建索引
                    IndexerAddEntry indexerRequest = new IndexerAddEntry(request.IndexName,
                            new DocumentIndex(request.docId, (float) numTokens,new ArrayList<>(tokensMap.size()))
                            ,request.forceUpdate
                    );

                    tokensMap.forEach((k,v) -> indexerRequest.document.Keywords.add(
                            new DocumentIndex.KeywordIndex(
                                    k,
                                    // 非分词标注的词频设置为0，不参与tf-idf计算
                                    (float)v.size(),
                                    v
                            )
                    ));

                    // 加入索引通道
                    engine.indexerAddDocChannels.get(shard).addWork(indexerRequest);
                    /*
                    if (request.forceUpdate) {
                        for (int i = 0; i < engine.initOptions.NumShards; i++) {
                            if (i == shard) {
                                continue;
                            }
                            engine.indexerAddDocChannels.get(shard).addWork(new IndexerAddDocumentRequest.Entry(true));
                        }
                    }
                    */
                    // 加入排序通道
                    RankerAddEntry rankerRequest = new RankerAddEntry();
                    rankerRequest.IndexName = request.IndexName;
                    rankerRequest.docId = request.docId;
                    rankerRequest.fields = request.data.Fields;
                    engine.rankerAddDocChannels.get(shard).addWork(rankerRequest);
                } catch (InterruptedException e) {

                }
            }

        }

        public void accept(SegmenterEntry entry) throws InterruptedException {
            works.put(entry);
        }

        @Override
        public void close() throws IOException {
            isRuntime = false;
        }
    }

}
