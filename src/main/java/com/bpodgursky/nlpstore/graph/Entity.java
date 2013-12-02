package com.bpodgursky.nlpstore.graph;

import com.google.common.collect.Sets;

import java.util.Set;

public class Entity {

  private final Set<IdentityEdge> identityEdges;
  private final Set<RefNode> nodes;
  private final int id;

  public Entity() {
    id = NodeCounter.getId();
    nodes = Sets.newHashSet();
    identityEdges = Sets.newHashSet();
  }

  public void addEdgeSet(IdentityEdge edge){
    this.nodes.addAll(edge.getNodes());
    this.identityEdges.add(edge);
  }

  public Set<IdentityEdge> getIdentityEdges() {
    return identityEdges;
  }

  public Set<RefNode> getNodes() {
    return nodes;
  }

  public int getId() {
    return id;
  }

  @Override
  public String toString() {
    return "Entity{" +
        "identityEdges=" + identityEdges +
        ", nodes=" + nodes +
        '}';
  }
}
