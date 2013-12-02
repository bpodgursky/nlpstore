package com.bpodgursky.nlpstore.graph;

public class NodeCounter {
  private static int count = 0;
  public static int getId(){
    return count++;
  }
}
