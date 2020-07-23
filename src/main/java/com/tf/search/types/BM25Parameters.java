package com.tf.search.types;

public class BM25Parameters {

    public float K1;

    public float B;

    public BM25Parameters(){
        K1 = 2.0f;
        B = 0.75f;
    }
}
