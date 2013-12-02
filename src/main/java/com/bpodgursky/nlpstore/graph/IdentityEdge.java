package com.bpodgursky.nlpstore.graph;

import java.util.Set;

public class IdentityEdge {
  private final Set<RefNode> nodes;

  //  other pedigree info?
  private final IdentitySource source;

  IdentityEdge(Set<RefNode> nodes, IdentitySource source) {
    this.nodes = nodes;
    this.source = source;
  }

  public Set<RefNode> getNodes() {
    return nodes;
  }

  public IdentitySource getSource() {
    return source;
  }
}
