package com.tf.search.engine.segment;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.mining.word.TfIdf;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Segmenter {

    public void LoadDictionary(String segmenterDictionaries) {
        HanLP.Config.CoreDictionaryPath = segmenterDictionaries;
    }

    public List<Term> Segment(String Text) {
        return NLPTokenizer.segment(Text);
    }

    public Map<String, Double> SegmentTFIDF(String Text) {
        return TfIdf.tf(convert(NLPTokenizer.segment(Text)));
    }

    private List<String> convert(List<Term> termList)
    {
        List<String> words = new ArrayList<String>(termList.size());
        for (Term term : termList)
        {
            words.add(term.word);
        }
        return words;
    }
}
