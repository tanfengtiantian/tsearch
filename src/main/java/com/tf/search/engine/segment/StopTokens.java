package com.tf.search.engine.segment;


import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;

public class StopTokens {

    public void Init(String stopTokenFile) {
        HanLP.Config.CoreStopWordDictionaryPath = stopTokenFile;
    }

    public boolean IsStopToken(Term token) {

        return CoreStopWordDictionary.shouldRemove(token);
    }

    public boolean IsStopToken(String token) {
        //CoreStopWordDictionary.apply();
        return CoreStopWordDictionary.shouldRemove(new Term(token, Nature.x));
    }
}
