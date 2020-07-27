package com.tf.search.examples.bkd;

import com.tf.search.utils.bkd.BKDTree;
import com.tf.search.utils.bkd.Document;
import com.tf.search.utils.bkd.Tree;

/**
 * Test for BKDTree.
 */
public class BKDTreeTest extends AbstractTreeTest{

    public Tree getTree(Document[] documents, int maxDocsPerLeaf) {
        return new BKDTree(documents, maxDocsPerLeaf);
    }



    public static void main(String[] args) {
        BKDTreeTest bkdTreeTest = new BKDTreeTest();
        bkdTreeTest.testBasicContains();
    }
}
