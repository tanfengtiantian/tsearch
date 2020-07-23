package com.tf.search.engine.segment.entry;

import com.tf.search.types.DocumentIndexData;

public class SegmenterEntry {
    public Long docId;
    public Integer hash;
    public DocumentIndexData data;
    public boolean forceUpdate;
    public SegmenterEntry(long docId, int hash, DocumentIndexData data, boolean forceUpdate) {
        this.docId = docId;
        this.hash = hash;
        this.data = data;
        this.forceUpdate = forceUpdate;
    }
}
