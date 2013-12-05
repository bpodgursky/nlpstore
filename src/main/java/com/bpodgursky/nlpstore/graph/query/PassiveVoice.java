package com.bpodgursky.nlpstore.graph.query;

import com.bpodgursky.nlpstore.graph.Edge;
import com.bpodgursky.nlpstore.graph.Node;

/**
 * This almost cetainly doesn't cover all cases, but can add to / clean up if else blocks below as more are uncovered
 */
public class PassiveVoice implements Voice {

  @Override
  public boolean matches(Edge dataEdge, Edge queryEdge) {
    return matchesInternal(dataEdge, queryEdge);
  }

  public static boolean matchesInternal(Edge dataEdge, Edge queryEdge){

    String dataRelation = dataEdge.getRelation();
    String queryRelation = queryEdge.getRelation();

    if (dataRelation.equals("prepositional modifier")) {
      if (dataEdge.getTarget().getToken().equals("by")) {
        if (queryRelation.equals("nominal subject")) {
          return true;
        }
        return false;
      }
    }

    if (dataRelation.equals("nominal subject")) {
      if (queryEdge.getTarget().getToken().equals("by")) {
        if (queryRelation.equals("prepositional modifier")) {
          return true;
        }
        return false;
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
    if(answer.getToken().equals("by")){
      if(answer.getOutgoingEdges().size() == 1){
        return answer.getOutgoingEdges().get(0).getTarget();
      }
    }
    return answer;
  }
}
