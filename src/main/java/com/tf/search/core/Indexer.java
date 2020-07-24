package com.tf.search.core;

import com.tf.search.types.*;

import java.util.*;

public class Indexer {

    public String Name;

    public Map<String, SimpleFieldInfo> Fields = new HashMap<>();

    public TableLock tableLock = new TableLock();

    public AddCacheLock addCacheLock = new AddCacheLock();

    public RemoveCacheLock removeCacheLock = new RemoveCacheLock();

    public IndexerInitOptions initOptions;

    private volatile boolean initialized;

    // 这实际上是总文档数的一个近似
    public long numDocuments;

    // 所有被索引文本的总关键词数
    public float totalTokenLength;

    // 每个文档的关键词长度
    public Map<Long,Float> docTokenLengths;

    public void Init(IndexerInitOptions options) {
        if (initialized) {
            System.err.println("索引器不能初始化两次");
            return;
        }
        options.Init();
        initOptions = options;
        initialized = true;
        tableLock.table = new HashMap<>();
        tableLock.docsState = new HashMap<>();
        addCacheLock.addCache = new DocumentIndex[initOptions.DocCacheSize];
        removeCacheLock.removeCache = new int[initOptions.DocCacheSize * 2];
        docTokenLengths = new HashMap<>();
    }


    public class TableLock {
        public Map<String,KeywordIndices> table;
        public Map<Long,Integer> docsState; // nil: 表示无状态记录，0: 存在于索引中，1: 等待删除，2: 等待加入
    }

    public class AddCacheLock {
        public int addCachePointer;
        public DocumentIndex[] addCache;
    }

    public class RemoveCacheLock {
        public int removeCachePointer;
        public int[] removeCache;
    }

    // 反向索引表的一行，收集了一个搜索键出现的所有文档，按照DocId从小到大排序。
    public class KeywordIndices {
        // 下面的切片是否为空，取决于初始化时IndexType的值
        public List<Long> docIds;           // 全部类型都有
        public List<Float> frequencies;     // IndexType == FrequenciesIndex
        public List<Indexer[]> locations;   // IndexType == LocationsIndex
    }

    public void AddDocumentToCache(DocumentIndex document, boolean forceUpdate) {
        if (!initialized) {
            System.err.println("索引器尚未初始化");
            return;
        }

        if (document != null) {
            synchronized (this){
                addCacheLock.addCache[addCacheLock.addCachePointer] =  document;
                addCacheLock.addCachePointer++;
            }
        }
        if (addCacheLock.addCachePointer >= initOptions.DocCacheSize || forceUpdate) {
            int position = 0;
            synchronized (this){
                for (int i = 0; i < addCacheLock.addCachePointer; i++) {
                    DocumentIndex docIndex = addCacheLock.addCache[i];
                    Integer state = tableLock.docsState.get(docIndex.DocId);
                    if(state != null && state <= 1) {
                        // ok && docState == 0 表示存在于索引中，需先删除再添加
                        // ok && docState == 1 表示不一定存在于索引中，等待删除，需先删除再添加
                        if(position != i) {

                        }
                        if (state == 0) {

                        }

                        position++;
                    }else if (state == null) {
                        tableLock.docsState.put(docIndex.DocId,2);
                    }

                }
            }
            if (RemoveDocumentToCache(0, forceUpdate)) {
                // 只有当存在于索引表中的文档已被删除，其才可以重新加入到索引表中
                position = 0;
            }
            // [0:2] 切片返回数组
            DocumentIndex[] addCachedDocuments =  Arrays.copyOfRange(addCacheLock.addCache,position,addCacheLock.addCachePointer);
            addCacheLock.addCachePointer = position;
            Arrays.sort(addCachedDocuments,Comparator.comparing(o -> o.DocId));
            AddDocuments(addCachedDocuments);
        }


    }

    private void AddDocuments(DocumentIndex[] documents) {
        if (!initialized) {
            System.err.println("索引器尚未初始化");
            return;
        }

        synchronized (this){
            Map<String ,Integer> indexPointers = new HashMap<>(tableLock.table.size());
            // DocId 递增顺序遍历插入文档保证索引移动次数最少
            for (int i = 0; i < documents.length; i++) {
                if(i < documents.length-1 && documents[i].DocId == documents[i+1].DocId) {
                    // 如果有重复文档加入，因为稳定排序，只加入最后一个
                    continue;
                }
                DocumentIndex document = documents[i];
                Integer state = tableLock.docsState.get(document.DocId);
                if(state != null && state == 1) {
                    // 如果此时 docState 仍为 1，说明该文档需被删除
                    // docState 合法状态为 nil & 2，保证一定不会插入已经在索引表中的文档
                    continue;
                }

                // 更新文档关键词总长度
                if (document.TokenLength != 0) {
                    docTokenLengths.put(document.DocId,document.TokenLength);
                    totalTokenLength += document.TokenLength;
                }
                boolean docIdIsNew = true;

                for (int keyIndex = 0; keyIndex < document.Keywords.size(); keyIndex++) {
                    DocumentIndex.KeywordIndex keyword = document.Keywords.get(keyIndex);
                    KeywordIndices indices = tableLock.table.get(keyword.Text);
                    if(indices == null) {
                        indices = new KeywordIndices();

                        switch (initOptions.IndexType) {
                            case IndexerInitOptions.LocationsIndex:

                                break;
                            case  IndexerInitOptions.FrequenciesIndex:
                                indices.frequencies = new ArrayList<>();
                                indices.frequencies.add(keyword.Frequency);
                                break;
                        }

                        indices.docIds = new ArrayList<>();
                        indices.docIds.add(document.DocId);
                        tableLock.table.put(keyword.Text,indices);
                        continue;
                    }
                    // 查找应该插入的位置，且索引一定不存在，索引需小到大排序
                    Integer start = indexPointers.get(keyword.Text);
                    SearchIndexRet re = searchIndex(indices, start == null ? 0 : start, getIndexLength(indices)-1, document.DocId);
                    int position = re.start;
                    indexPointers.put(keyword.Text, position);
                    switch (initOptions.IndexType) {
                        case IndexerInitOptions.LocationsIndex:

                            break;
                        case  IndexerInitOptions.FrequenciesIndex:
                            indices.frequencies.add(position,keyword.Frequency);
                            break;
                    }
                    indices.docIds.add(position,document.DocId);
                    /*
                    position, _ := indexer.searchIndex(
                            indices, indexPointers[keyword.Text], indexer.getIndexLength(indices)-1, document.DocId)
                    indexPointers[keyword.Text] = position
                    switch indexer.initOptions.IndexType {
                        case types.LocationsIndex:
                            indices.locations = append(indices.locations, []int{})
                        copy(indices.locations[position+1:], indices.locations[position:])
                        indices.locations[position] = keyword.Starts
                        case types.FrequenciesIndex:
                            indices.frequencies = append(indices.frequencies, float32(0))
                            copy(indices.frequencies[position+1:], indices.frequencies[position:])
                            indices.frequencies[position] = keyword.Frequency
                    }
                    */

                    //foundKeyword.docIds = append(indices.docIds, 0)
                    //copy(indices.docIds[position+1:], indices.docIds[position:])
                    //indices.docIds[position] = document.DocId


                }

                // 更新文章状态和总数
                if (docIdIsNew) {
                    tableLock.docsState.put(document.DocId,0);
                    numDocuments++;
                }
            }
        }
    }

    // 二分法查找indices中某文档的索引项
    // 第一个返回参数为找到的位置或需要插入的位置
    // 第二个返回参数标明是否找到
    public SearchIndexRet searchIndex(KeywordIndices indices, int start, int end, Long docId) {
        // 特殊情况
        if (getIndexLength(indices) == start) {
            return new SearchIndexRet(start,false);
        }
        // docid < 最小值 docid
        if (docId < getDocId(indices, start)) {
            return new SearchIndexRet(start,false);
        } else if (docId == getDocId(indices, start)) {
            return new SearchIndexRet(start,true);
        }

        if (docId > getDocId(indices, end)) {
            return new SearchIndexRet(end + 1, false);
        } else if (docId == getDocId(indices, end)) {
            return new SearchIndexRet(end, true);
        }

        //二分
        int middle;
        while (end-start > 1) {
            middle = (start + end) / 2;
            if (docId == getDocId(indices, middle)) {
                return new SearchIndexRet(middle, true);
            } else if (docId > getDocId(indices, middle)) {
                start = middle;
            } else {
                end = middle;
            }
        }
        return new SearchIndexRet(end,false);
    }


    public boolean RemoveDocumentToCache(int docId, boolean forceUpdate) {

        return false;
    }



    // 查找包含全部搜索键(AND操作)的文档
    // 当docIds不为nil时仅从docIds指定的文档中查找
    public SearchResult Lookup(List<String> tokens, List<String> labels, Map<Long, Boolean> docIds, boolean countDocsOnly) {

        if (!initialized) {
            System.err.println("索引器尚未初始化");
            return null;
        }

        if (numDocuments == 0) {
            return null;
        }

        int initialCapacity = (tokens == null ? 0 : tokens.size()) + (labels == null ? 0 : labels.size());
        List<String> keywords = new ArrayList<>(initialCapacity);
        if(tokens != null)
            keywords.addAll(tokens);
        if(labels != null)
            keywords.addAll(labels);

        // 返回参数
        SearchResult result = new SearchResult();
        result.numDocs = 0;

        synchronized (this) {
            KeywordIndices[] table = new KeywordIndices[keywords.size()];
            for (int i = 0; i < keywords.size(); i++) {
                KeywordIndices indices = tableLock.table.get(keywords.get(i));
                if(indices == null){
                    return null;
                } else {
                    table[i] = indices;
                }
            }

            // 当没有找到时直接返回
            if (table.length == 0) {
                return null;
            }
            //最大文档排序
            Arrays.sort(table, (t1, t2) -> t2.docIds.size() - t1.docIds.size());

            // 归并查找各个搜索键出现文档的交集
            // 从后向前查保证先输出DocId较大文档
            Integer[] indexPointers = new Integer[table.length];
            for (int iTable = 0; iTable < table.length; iTable++) {
                indexPointers[iTable] = getIndexLength(table[iTable])- 1;
            }

            // 平均文本关键词长度，用于计算BM25
            float avgDocLength = totalTokenLength / numDocuments;
            int indexPointer = indexPointers[0];
            for (; indexPointer >= 0; indexPointer--) {
                // 以第一个搜索键出现的文档作为基准，并遍历其他搜索键搜索同一文档
                Long baseDocId = getDocId(table[0], indexPointer);
                if (docIds != null) {
                    Boolean found = docIds.get(baseDocId);
                    if(found != null && !found)
                        continue;
                }

                int iTable = 1;
                boolean found = true;
                for (; iTable < table.length; iTable++) {
                    // 二分法比简单的顺序归并效率高，也有更高效率的算法，
                    // 但顺序归并也许是更好的选择，考虑到将来需要用链表重新实现
                    // 以避免反向表添加新文档时的写锁。
                    // TODO: 进一步研究不同求交集算法的速度和可扩展性。
                    SearchIndexRet re = searchIndex(table[iTable],
                            0, indexPointers[iTable], baseDocId);
                    int position = re.start;
                    boolean foundBaseDocId = re.state;
                    if (foundBaseDocId) {
                        indexPointers[iTable] = position;
                    } else {
                        if (position == 0) {
                            // 该搜索键中所有的文档ID都比baseDocId大，因此已经没有
                            // 继续查找的必要。
                            return result;
                        } else {
                            // 继续下一indexPointers[0]的查找
                            indexPointers[iTable] = position - 1;
                            found = false;
                            break;
                        }
                    }
                }

                if (found) {
                    Integer docState = tableLock.docsState.get(baseDocId);
                    if (docState == null || docState != 0) {
                        continue;
                    }
                    IndexedDocument indexedDoc = new IndexedDocument();
                    // 当为LocationsIndex时计算关键词紧邻距离
                    if (initOptions.IndexType == IndexerInitOptions.LocationsIndex){

                    }
                    // FrequenciesIndex时计算BM25
                    if (initOptions.IndexType == IndexerInitOptions.FrequenciesIndex) {
                        float bm25 = 0f;
                        Float d = docTokenLengths.get(baseDocId);
                        for (int i = 0; i < tokens.size(); i++) {
                            Float frequency;
                            KeywordIndices t = table[i];
                            if (initOptions.IndexType == IndexerInitOptions.LocationsIndex) {
                                frequency = Float.intBitsToFloat(t.locations.get(indexPointers[i]).length);
                            } else {
                                frequency = t.frequencies.get(indexPointers[i]);
                            }

                            // 计算BM25
                            if(t.docIds.size()>0 && frequency >0 && initOptions.bm25Parameters != null && avgDocLength != 0) {
                                double idf = Math.log(numDocuments/t.docIds.size()+1);
                                float k1 = initOptions.bm25Parameters.K1;
                                float b = initOptions.bm25Parameters.B;
                                bm25 += idf * frequency * (k1 + 1) / (frequency + k1*(1-b+b*d/avgDocLength));
                            }
                        }
                        indexedDoc.BM25 = bm25;
                    }
                    indexedDoc.DocId = baseDocId;
                    if (!countDocsOnly) {
                        result.docs.add(indexedDoc);
                    }
                    result.numDocs++;
                }
            }
        }
        return result;
    }


    // 得到KeywordIndices中文档总数
    public int getIndexLength(KeywordIndices ti) {
        return ti.docIds.size();
    }
    // 从KeywordIndices中得到第i个文档的DocId
    public Long getDocId(KeywordIndices ti, int i) {
        return ti.docIds.get(i);
    }

    public static class SearchIndexRet{
        public int start;
        public boolean state;
        public SearchIndexRet(int start, boolean state) {
            this.start = start;
            this.state = state;
        }
    }

    public static class SearchResult{
        public List<IndexedDocument> docs = new ArrayList<>();
        public int numDocs = 0;
    }

}
