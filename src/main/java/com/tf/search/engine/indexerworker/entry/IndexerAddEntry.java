package com.tf.search.engine.indexerworker.entry;

import com.tf.search.types.DocumentIndex;

public class IndexerAddEntry {
    public DocumentIndex document;
    public boolean forceUpdate;
    public IndexerAddEntry(boolean forceUpdate) {
        this(null,forceUpdate);
    }
    public IndexerAddEntry(DocumentIndex document, boolean forceUpdate) {
        this.document = document;
        this.forceUpdate = forceUpdate;
    }
}
