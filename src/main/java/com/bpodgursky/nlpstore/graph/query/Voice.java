package com.bpodgursky.nlpstore.graph.query;

import com.bpodgursky.nlpstore.graph.Edge;
import com.bpodgursky.nlpstore.graph.Node;

interface Voice {
  public boolean matches(Edge dataEdge, Edge queryEdge);
  public Node extractMatch(Node answer);
}
