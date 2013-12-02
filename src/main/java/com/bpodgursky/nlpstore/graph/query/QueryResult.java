package com.bpodgursky.nlpstore.graph.query;

import com.bpodgursky.nlpstore.graph.Node;

import java.util.Map;

public class QueryResult {

  private final Map<Node, Node> resolvedIndefinites;
  private final Node matchingSentence;

  public QueryResult(Map<Node, Node> resolvedIndefinites, Node matchingSentence) {
    this.resolvedIndefinites = resolvedIndefinites;
    this.matchingSentence = matchingSentence;
  }

  public Map<Node, Node> getResolvedIndefinites() {
    return resolvedIndefinites;
  }

  public Node getMatchingSentence() {
    return matchingSentence;
  }

  @Override
  public String toString() {
    return "QueryResult{" +
        "resolvedIndefinites=" + resolvedIndefinites +
        ", matchingSentence=" + matchingSentence +
        '}';
  }
}
