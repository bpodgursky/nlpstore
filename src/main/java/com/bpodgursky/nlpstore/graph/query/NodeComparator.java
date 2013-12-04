package com.bpodgursky.nlpstore.graph.query;

import com.bpodgursky.nlpstore.graph.Node;

public interface NodeComparator {
  public boolean match(Node data, Node query) throws Exception;
}
