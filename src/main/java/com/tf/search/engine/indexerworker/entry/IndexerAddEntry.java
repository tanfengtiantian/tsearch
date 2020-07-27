package com.tf.search.engine.indexerworker.entry;

import com.tf.search.types.DocumentIndex;
import com.tf.search.types.SimpleFieldInfo;

public class IndexerAddEntry {
    public String indexName;
    public SimpleFieldInfo field;
    public DocumentIndex document;
    public boolean forceUpdate;
    public IndexerAddEntry(boolean forceUpdate) {
        this(null,null,null,forceUpdate);
    }
    public IndexerAddEntry(String indexName, SimpleFieldInfo field, DocumentIndex document, boolean forceUpdate) {
        this.indexName = indexName;
        this.field = field;
        this.document = document;
        this.forceUpdate = forceUpdate;
    }
}
