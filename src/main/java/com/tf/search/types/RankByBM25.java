package com.tf.search.types;

import java.util.ArrayList;
import java.util.List;

public class RankByBM25 implements ScoringCriteria{
    @Override
    public List<Float> Score(IndexedDocument doc, Object fields) {
        return new ArrayList<Float>(){{add(doc.BM25);}};
    }
}
