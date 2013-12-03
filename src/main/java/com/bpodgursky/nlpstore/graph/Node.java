package com.bpodgursky.nlpstore.graph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class Node {

  private final String token;
  private final String stem;
  private final String sentenceRef;
  private final List<Edge> incomingEdges = Lists.newArrayList();
  private final List<Edge> outgoingEdges = Lists.newArrayList();

  private final Integer index;

  private final int id;

  public Node(String token, String stem, String sentenceRef, Integer index) {
    id = NodeCounter.getId();

    this.token = token;
    this.stem = stem;
    this.sentenceRef = sentenceRef;
    this.index = index;
  }

  public String getToken() {
    return token;
  }

  public Integer getIndex() {
    return index;
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

    for (Node node : collectChildren(root)) {
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

  public static List<Node> collectChildren(Node node){
    List<Node> nodes = Lists.newArrayList();
    for (Edge edge : node.getOutgoingEdges()) {
      nodes.addAll(collectChildren(edge.getTarget()));
    }
    nodes.add(node);
    return nodes;
  }

  public static String getSentencePart(Node node) {

    Map<Integer, String> children = Maps.newTreeMap();
    getChildren(node, children);

    return StringUtils.join(Lists.newArrayList(children.values()), " ");
  }

  private static void getChildren(Node node, Map<Integer, String> children) {

    for (Edge edge : node.getOutgoingEdges()) {
      getChildren(edge.getTarget(), children);
    }

    children.put(node.getIndex(), node.getToken());
  }

}
