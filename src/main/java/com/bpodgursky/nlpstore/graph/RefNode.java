package com.bpodgursky.nlpstore.graph;

import java.util.List;

public class RefNode {

  private final String text;
  private final String sentence;
  private final List<Node> references;
  private final int id;

  private Entity identity;

  public RefNode(String text, String sentence, List<Node> references) {
    this.id = NodeCounter.getId();
    this.text = text;
    this.sentence = sentence;
    this.references = references;
  }

  public int getId() {
    return id;
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
    return references;
  }

  @Override
  public String toString() {
    return "RefNode{" +
        "text='" + text + '\'' +
        ", references=" + references +
        '}';
  }
}
