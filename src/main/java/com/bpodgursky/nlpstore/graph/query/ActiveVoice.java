package com.bpodgursky.nlpstore.graph.query;

import com.bpodgursky.nlpstore.graph.Edge;
import com.bpodgursky.nlpstore.graph.Node;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ActiveVoice implements Voice {

  //  query - data
  public static final Multimap<String, String> COMPATIBLE_CLAUSES = HashMultimap.create();

  static {
    COMPATIBLE_CLAUSES.put("direct object", "clausal complement");
    COMPATIBLE_CLAUSES.put("attributive", "nominal subject");
    COMPATIBLE_CLAUSES.put("adverbial modifier", "prepositional modifier");
    COMPATIBLE_CLAUSES.put("dependent", "prepositional modifier");
  }

  @Override
  public boolean matches(Edge dataEdge, Edge queryEdge) {
    return matchesInternal(dataEdge, queryEdge);
  }

  public static boolean matchesInternal(Edge dataEdge, Edge queryEdge){
    if (dataEdge.getRelation().equals(queryEdge.getRelation())) {
      return true;
    }

    if (COMPATIBLE_CLAUSES.containsEntry(queryEdge.getRelation(), dataEdge.getRelation())) {
      return true;
    }

    return false;
  }

  @Override
  public Node extractMatch(Node answer) {
    return answer;
  }
}
