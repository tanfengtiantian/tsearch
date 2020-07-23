package com.tf.search.types;

import java.util.List;

public interface ScoringCriteria {

    List<Float> Score(IndexedDocument doc, Object fields);

}
