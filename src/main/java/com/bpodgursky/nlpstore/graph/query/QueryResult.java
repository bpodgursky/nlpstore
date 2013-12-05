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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof QueryResult)) return false;

    QueryResult that = (QueryResult) o;

    if (matchingSentence != null ? !matchingSentence.equals(that.matchingSentence) : that.matchingSentence != null)
      return false;
    if (resolvedIndefinites != null ? !resolvedIndefinites.equals(that.resolvedIndefinites) : that.resolvedIndefinites != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = resolvedIndefinites != null ? resolvedIndefinites.hashCode() : 0;
    result = 31 * result + (matchingSentence != null ? matchingSentence.hashCode() : 0);
    return result;
  }
}
