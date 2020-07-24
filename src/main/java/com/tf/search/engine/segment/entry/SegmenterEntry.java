package com.tf.search.engine.segment.entry;

import com.tf.search.types.DocumentIndexData;

public class SegmenterEntry {
    public String IndexName;
    public Long docId;
    public Integer hash;
    public DocumentIndexData data;
    public boolean forceUpdate;
    public SegmenterEntry(String IndexName, Long docId, int hash, DocumentIndexData data, boolean forceUpdate) {
        this.IndexName = IndexName;
        this.docId = docId;
        this.hash = hash;
        this.data = data;
        this.forceUpdate = forceUpdate;
    }
}
