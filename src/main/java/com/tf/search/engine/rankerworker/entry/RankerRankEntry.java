package com.tf.search.engine.rankerworker.entry;

import com.tf.search.engine.RankerReturnRequest;
import com.tf.search.types.IndexedDocument;
import com.tf.search.types.RankOptions;

import java.util.List;

public class RankerRankEntry {
    public String IndexName;
    public List<IndexedDocument> docs;
    public RankOptions options;
    public RankerReturnRequest rankerReturnRequest;
    public Boolean countDocsOnly;
}
