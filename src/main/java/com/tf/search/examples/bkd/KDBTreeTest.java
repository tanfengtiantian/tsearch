package com.tf.search.examples.bkd;

import com.tf.search.utils.bkd.Document;
import com.tf.search.utils.bkd.KDBTree;
import com.tf.search.utils.bkd.Tree;

/**
 * Test for KDBTree.
 */
public class KDBTreeTest extends AbstractTreeTest {

    @Override
    public Tree getTree(Document[] documents, int maxDocsPerLeaf) {
        return new KDBTree(documents, maxDocsPerLeaf);
    }
}
