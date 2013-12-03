package com.bpodgursky.nlpstore.graph;

import java.util.List;

public class RefNode {

  private final String text;
  private final String sentence;
  private final Node headNode;
  private final int id;

  private Entity identity;

  public RefNode(String text, String sentence, Node headNode) {
    this.id = NodeCounter.getId();
    this.text = text;
    this.sentence = sentence;
    this.headNode = headNode;
  }

  public int getId() {
    return id;
  }

  public Node getHeadNode() {
    return headNode;
  }

  public String getSentence() {
    return sentence;
  }

  protected void setEntity(Entity entity){
    this.identity = entity;
  }

  public Entity getIdentity() {
    return identity;
  }

  public String getText() {
    return text;
  }

  public List<Node> getReferences() {
    return Node.collectChildren(headNode);
  }

  @Override
  public String toString() {
    return "RefNode{" +
        "text='" + text + '\'' +
        ", sentence='" + sentence + '\'' +
        ", headNode=" + headNode +
        ", id=" + id +
        ", identity=" + identity +
        '}';
  }
}
