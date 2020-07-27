package com.tf.search.engine.segment.entry;

import com.tf.search.types.DocumentIndexData;
import com.tf.search.types.SimpleFieldInfo;

public class SegmenterEntry {
    public String IndexName;
    public SimpleFieldInfo field;
    public Long docId;
    public Integer hash;
    public DocumentIndexData data;
    public boolean forceUpdate;
    public SegmenterEntry(String IndexName, SimpleFieldInfo field, Long docId, int hash, DocumentIndexData data, boolean forceUpdate) {
        this.IndexName = IndexName;
        this.field = field;
        this.docId = docId;
        this.hash = hash;
        this.data = data;
        this.forceUpdate = forceUpdate;
    }
}
