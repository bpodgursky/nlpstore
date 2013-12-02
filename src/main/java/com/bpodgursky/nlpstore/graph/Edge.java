package com.bpodgursky.nlpstore.graph;

public class Edge {

  private final String relation;
  private final Node source;
  private final Node target;

  public Edge(String relation, Node source, Node target) {
    this.relation = relation;
    this.source = source;
    this.target = target;
  }

  public String getRelation() {
    return relation;
  }

  public Node getSource() {
    return source;
  }

  public Node getTarget() {
    return target;
  }
}
