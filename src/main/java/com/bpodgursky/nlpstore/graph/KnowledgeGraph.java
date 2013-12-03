package com.bpodgursky.nlpstore.graph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class KnowledgeGraph {

  private Multimap<String, Node> labelToNodes = HashMultimap.create();
  private Multimap<String, RefNode> references = HashMultimap.create();
  private Multimap<Node, RefNode> nodeToRefs = HashMultimap.create();
  private Set<Node> roots = Sets.newHashSet();

  public Collection<Node> getNodes(String label) {
    return labelToNodes.get(label);
  }

  public Collection<RefNode> getRefNode(String label) {
    return references.get(label);
  }

  public Multimap<String, Node> getLabelToNodes() {
    return labelToNodes;
  }

  public Multimap<String, RefNode> getReferences() {
    return references;
  }

  public Collection<RefNode> getRefNodesForHead(Node node){
    return nodeToRefs.get(node);
  }

  public String toDotFile() {

    StringBuilder builder = new StringBuilder();

    builder.append("digraph G {");

    for (Node node : labelToNodes.values()) {
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

    for (RefNode refNode : references.values()) {
      builder.append(refNode.getId()).append(" ").append("[label=\"").append(refNode.getText()).append("\"];\n");

      for (Node node : refNode.getReferences()) {
        builder.append(refNode.getId())
            .append("->")
            .append(node.getId())
            .append(";\n");
      }

      builder.append(refNode.getIdentity().getId())
          .append("->")
          .append(refNode.getId())
          .append(";\n");
    }

    builder.append("}");

    return builder.toString();
  }

  public Set<Node> getRoots() {
    return roots;
  }

  public void addRelation(Node source, Node target, String relation) {
    Edge edge = new Edge(relation, source, target);

    roots.remove(target);

    source.addOutgoingEdge(edge);
    target.addIncomingEdge(edge);
  }

  public Node createNode(String token, String stem, String sentence, int index) {
    Node node = new Node(token, stem, sentence, index);
    labelToNodes.put(token, node);
    roots.add(node);
    return node;
  }

  protected void addReference(RefNode ref) {
    Entity entity = new Entity();
    ref.setEntity(entity);
    entity.addEdgeSet(new IdentityEdge(Sets.newHashSet(ref), IdentitySource.SEED));

    nodeToRefs.put(ref.getHeadNode(), ref);
    references.put(ref.getText(), ref);
  }

  public void merge(Set<RefNode> nodes, IdentitySource cause) {

    if (nodes.isEmpty()) {
      return;
    }

    if(nodes.size() == 1){
      return;
    }

    Iterator<RefNode> iter = nodes.iterator();
    RefNode base = iter.next();
    Entity mainIdentity = base.getIdentity();

    while (iter.hasNext()) {
      RefNode next = iter.next();
      Entity otherIdentity = next.getIdentity();

      for (RefNode node : otherIdentity.getNodes()) {
        node.setEntity(mainIdentity);
      }

      for (IdentityEdge edge : otherIdentity.getIdentityEdges()) {
        mainIdentity.addEdgeSet(edge);
      }

    }

    mainIdentity.addEdgeSet(new IdentityEdge(nodes, cause));

  }
}
