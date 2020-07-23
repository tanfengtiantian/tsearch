package com.tf.search.types;

import com.tf.search.engine.rankerworker.entry.RankerReturnEntry;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Ranker {

    public boolean initialized = false;

    public Map<Long,Object> fields;

    public Map<Long,Boolean> docs;


    public void Init(IndexerInitOptions indexerInitOptions) {
        if(initialized) {
            System.out.println("排序器不能初始化两次");
        }
        initialized = true;

        fields = new HashMap<>();
        docs = new HashMap<>();

    }

    // 给某个文档添加评分字段
    public void AddDoc(Long docId,Object fields){
        if (!initialized) {
            System.err.println("排序器尚未初始化");
            return;
        }
        synchronized (this){
            this.fields.put(docId,fields);
            this.docs.put(docId,true);
        }
    }

    // 给文档评分并排序
    public RankerReturnEntry Rank(List<IndexedDocument> docs, RankOptions options, Boolean countDocsOnly) {
        if (!initialized) {
            System.err.println("排序器尚未初始化");
            return null;
        }
        // 对每个文档评分
        List<ScoredDocument> outputDocs = new ArrayList<>();
        AtomicInteger numDocs = new AtomicInteger();
        docs.forEach(d->{
            synchronized (this) {
                // 判断doc是否存在
                Boolean ok = this.docs.get(d.DocId);
                if(ok!=null && ok) {
                    Object fs = fields.get(d.DocId);
                    // 计算评分并剔除没有分值的文档
                   List<Float> scores = options.ScoringCriteria.Score(d, fs);
                    if (scores.size() > 0) {
                        if (!countDocsOnly) {
                            ScoredDocument scoredDocument = new ScoredDocument();
                            scoredDocument.DocId = d.DocId;
                            scoredDocument.Scores = scores;
                            scoredDocument.TokenSnippetLocations = d.TokenSnippetLocations;
                            scoredDocument.TokenLocations = d.TokenLocations;

                            outputDocs.add(scoredDocument);
                        }
                        numDocs.getAndIncrement();
                    }
                }
            }
        });
        // 排序
        if (!countDocsOnly) {
            if (options.ReverseOrder) {//顺序
                outputDocs.sort(Comparator.comparing(o -> o.DocId));
            } else { //倒序
                outputDocs.sort((o1,o2)->o2.DocId.compareTo(o1.DocId));
            }
            // 当用户要求只返回部分结果时返回部分结果

        }
        RankerReturnEntry entry = new RankerReturnEntry();
        entry.docs = outputDocs.toArray(new ScoredDocument[0]);
        entry.numDocs = numDocs.get();
        return entry;
    }
}
