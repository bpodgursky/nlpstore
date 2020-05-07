package com.bpodgursky.nlpstore.graph.query;

import java.util.Set;

import com.bpodgursky.nlpstore.graph.Edge;
import com.bpodgursky.nlpstore.graph.Node;
import com.google.common.collect.Sets;

/**
 * This almost cetainly doesn't cover all cases, but can add to / clean up if else blocks below as more are uncovered
 */
public class PassiveVoice implements Voice {

  @Override
  public boolean matches(Edge dataEdge, Edge queryEdge) {
    return matchesInternal(dataEdge, queryEdge);
  }

  private static Set<String> QUERY_PASSIVE = Sets.newHashSet(
      "nominal subject",
      "prepositional modifier",
      "adverbial modifier"
  );

  private static Set<String> DATA_PASSIVE = Sets.newHashSet(
      "prepositional modifier",
      "nominal modifier",
      "nominal subject"
  );

  public static boolean matchesInternal(Edge dataEdge, Edge queryEdge){

    String dataRelation = dataEdge.getRelation();
    String queryRelation = queryEdge.getRelation();

    if(QUERY_PASSIVE.contains(queryRelation)){
      if(DATA_PASSIVE.contains(dataRelation)){
        Node target = dataEdge.getTarget();
        for (Edge edge : target.getOutgoingEdges()) {
          if(edge.getRelation().equals("case marker")) {
            if (edge.getTarget().getToken().equals("by")) {
              return true;
            }
          }
        }
      }
    }

    if (dataRelation.equals("nominal passive subject")) {
      if (queryRelation.equals("direct object")) {
        return true;
      }

      return false;
    }

    if (dataRelation.equals("direct object")) {
      if (queryRelation.equals("nominal passive subject")) {
        return true;
      }

      return false;
    }

    return ActiveVoice.matchesInternal(dataEdge, queryEdge);

  }

  @Override
  public Node extractMatch(Node answer) {
    return answer;
  }
}
