package com.bpodgursky.nlpstore.graph.query;

import java.util.Map;

import com.bpodgursky.nlpstore.graph.Node;
import com.bpodgursky.nlpstore.script.EXTJWNLEx;
import com.google.common.collect.Maps;
import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

public class ThesarusComparator implements NodeComparator {

  private final Dictionary dict;

  public ThesarusComparator() {
    try {
      JWNL.initialize(
          EXTJWNLEx.class.getClassLoader().getResourceAsStream("com/bpodgursky/nlpstore/extjwnl/properties.xml"));
    } catch (JWNLException e) {
      e.printStackTrace();
    }
    dict = Dictionary.getInstance();
  }

  private static final Map<String, POS> STANFORD_TO_WN_POS = Maps.newHashMap();
  static {

    STANFORD_TO_WN_POS.put("VBD", POS.VERB);
    STANFORD_TO_WN_POS.put("VBN", POS.VERB);
    STANFORD_TO_WN_POS.put("VB", POS.VERB);
    STANFORD_TO_WN_POS.put("VBG", POS.VERB);
    STANFORD_TO_WN_POS.put("VBP", POS.VERB);
    STANFORD_TO_WN_POS.put("VBZ", POS.VERB);

    STANFORD_TO_WN_POS.put("NN", POS.NOUN);
    STANFORD_TO_WN_POS.put("NNS", POS.NOUN);
    STANFORD_TO_WN_POS.put("NNP", POS.NOUN);
    STANFORD_TO_WN_POS.put("NNPS", POS.NOUN);

    STANFORD_TO_WN_POS.put("JJ", POS.ADJECTIVE);
    STANFORD_TO_WN_POS.put("JJR", POS.ADJECTIVE);
    STANFORD_TO_WN_POS.put("JJS", POS.ADJECTIVE);

    STANFORD_TO_WN_POS.put("RB", POS.ADVERB);
    STANFORD_TO_WN_POS.put("RBR", POS.ADVERB);
    STANFORD_TO_WN_POS.put("RBS", POS.ADVERB);

    STANFORD_TO_WN_POS.put("PRP", POS.NOUN);
    STANFORD_TO_WN_POS.put("PRP$", POS.NOUN);

    STANFORD_TO_WN_POS.put("SYM", POS.NOUN);
    STANFORD_TO_WN_POS.put("CD", POS.NOUN);
    STANFORD_TO_WN_POS.put("WP$", POS.NOUN);

  }

  @Override
  public boolean match(Node data, Node query) throws Exception {

    POS dataPos = STANFORD_TO_WN_POS.get(data.getPos());
    POS queryPos = STANFORD_TO_WN_POS.get(query.getPos());

    String dataLemma = data.getStem().toUpperCase();
    String queryLemma = query.getStem().toUpperCase();


    System.out.println();
    System.out.println("DATA LEMMA: "+dataLemma);
    System.out.println("QUERY LEMMA: "+queryLemma);

    if(dataLemma.equals(queryLemma)){
      return true;
    }

    if (queryPos == null) {
      System.out.println("UNRECOGNIZED: "+query.getPos());
    }

    if(dataPos == null){
      System.out.println("UNRECOGNIZED: "+data.getPos());
    }

    if (dataPos == null || queryPos == null) {
      return false;
    }

    if(checkMatch(queryPos, queryLemma, dataLemma)){
      return true;
    }

    if(checkMatch(dataPos, dataLemma, queryLemma)){
      return true;
    }

    return false;

  }

  private boolean checkMatch(POS pos, String stem, String otherStem) throws JWNLException {
    IndexWord word = dict.getIndexWord(pos, stem);
    if (word != null) {
      for (long offset : word.getSynsetOffsets()) {
        Synset synset = dict.getSynsetAt(pos, offset);
        for (Word word1 : synset.getWords()) {
          if (word1.getLemma().toUpperCase().equals(otherStem)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
