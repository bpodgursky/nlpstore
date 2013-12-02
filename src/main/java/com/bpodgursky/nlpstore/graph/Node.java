package com.bpodgursky.nlpstore.graph;

import com.google.common.collect.Lists;

import java.util.List;

public class Node {

  private final String token;
  private final String stem;
  private final String sentenceRef;
  private final List<Edge> incomingEdges = Lists.newArrayList();
  private final List<Edge> outgoingEdges = Lists.newArrayList();

  private final int id;

  public Node(String token, String stem, String sentenceRef) {
    id = NodeCounter.getId();

    this.token = token;
    this.stem = stem;
    this.sentenceRef = sentenceRef;
  }

  public String getToken() {
    return token;
  }

  public String getSentenceRef() {
    return sentenceRef;
  }

  public List<Edge> getIncomingEdges() {
    return incomingEdges;
  }

  public String getStem() {
    return stem;
  }

  public List<Edge> getOutgoingEdges() {
    return outgoingEdges;
  }

  public int getId() {
    return id;
  }

  protected void addIncomingEdge(Edge edge){
    incomingEdges.add(edge);
  }

  protected void addOutgoingEdge(Edge edge){
    outgoingEdges.add(edge);
  }

  @Override
  public String toString() {
    return "Node{" +
        "token='" + token + '\'' +
        ", stem='" + stem + '\'' +
        ", sentenceRef='" + sentenceRef + '\'' +
        ", incomingEdges=" + incomingEdges +
        ", outgoingEdges=" + outgoingEdges +
        ", id=" + id +
        '}';
  }


  public static String toJson(Node root){
    StringBuilder builder = new StringBuilder();
    builder.append("digraph G {");

    for (Node node : collectNodes(root)) {
      builder.append(node.getId()).append(" ").append("[label=\"").append(node.getToken()).append("\"];\n");

      for (Edge edge : node.getOutgoingEdges()) {
        builder.append(edge.getSource().getId())
            .append("->")
            .append(edge.getTarget().getId())
            .append(" [label=\"")
            .append(edge.getRelation())
            .append("\"];\n");
      }
    }
    builder.append("}");

    return builder.toString();
  }

  private static List<Node> collectNodes(Node node){
    List<Node> nodes = Lists.newArrayList();
    for (Edge edge : node.getOutgoingEdges()) {
      nodes.addAll(collectNodes(edge.getTarget()));
    }
    nodes.add(node);
    return nodes;
  }

}
