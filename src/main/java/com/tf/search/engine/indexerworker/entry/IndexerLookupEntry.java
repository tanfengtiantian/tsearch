package com.tf.search.engine.indexerworker.entry;

import com.tf.search.engine.RankerReturnRequest;
import com.tf.search.types.RankOptions;

import java.util.List;
import java.util.Map;

public class IndexerLookupEntry {
    public Boolean countDocsOnly;
    public List<String> tokens;
    public List<String> labels;
    public Map<Long,Boolean> docIds;
    public RankOptions options;
    public RankerReturnRequest rankerReturnRequest;
    public Boolean orderless;
}
