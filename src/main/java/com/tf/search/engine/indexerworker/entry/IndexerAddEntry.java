package com.tf.search.engine.indexerworker.entry;

import com.tf.search.types.DocumentIndex;

public class IndexerAddEntry {
    public String indexName;
    public DocumentIndex document;
    public boolean forceUpdate;
    public IndexerAddEntry(boolean forceUpdate) {
        this(null,null,forceUpdate);
    }
    public IndexerAddEntry(String indexName, DocumentIndex document, boolean forceUpdate) {
        this.indexName = indexName;
        this.document = document;
        this.forceUpdate = forceUpdate;
    }
}
