package com.bpodgursky.nlpstore.graph;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class NodeHelper {

  public static List<String> getTokens(Node node){

    List<String> tokens = Lists.newArrayList();
    for (Edge edge : node.getOutgoingEdges()) {
      tokens.addAll(getTokens(edge.getTarget()));
    }

    tokens.add(node.getToken());

    return tokens;
  }

  public static String getNodeString(Node node){
    return StringUtils.join(getTokens(node), " ");
  }
}
