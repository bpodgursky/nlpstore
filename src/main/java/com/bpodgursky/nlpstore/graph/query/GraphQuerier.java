package com.bpodgursky.nlpstore.graph.query;

import com.bpodgursky.nlpstore.graph.Edge;
import com.bpodgursky.nlpstore.graph.Entity;
import com.bpodgursky.nlpstore.graph.KnowledgeGraph;
import com.bpodgursky.nlpstore.graph.Node;
import com.bpodgursky.nlpstore.graph.RefNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphQuerier {
  private static final Logger LOG = LoggerFactory.getLogger(GraphQuerier.class);

  private static final Set<String> QUESTION_STEMS = Sets.newHashSet(
      "WHO",
      "WHAT",
      "WHERE",
      "WHEN",
      "WHY",
      "HOW",
      "WHICH",
      "WHEREFORE",
      "WHATEVER",
      "WHOM",
      "WHOSE",
      "WHEREWITH",
      "WITHER",
      "WHENCE"
  );

  private static final Set<String> IGNORED_QUESTION_CLAUSES = Sets.newHashSet(
      "auxiliary"
  );

  private final KnowledgeGraph graph;
  private final NodeComparator nodeComparator;

  private static final List<Voice> VOICES = Lists.newArrayList(
      new ActiveVoice(), new PassiveVoice()
  );

  public GraphQuerier(KnowledgeGraph graph, NodeComparator comparator) {
    this.graph = graph;
    this.nodeComparator = comparator;
  }

  public Set<QueryResult> match(Node questionRoot) throws Exception {
    Set<QueryResult> results = Sets.newHashSet();

    for (Node root : graph.getRoots()) {
      for (Voice voice : VOICES) {
        Map<Node, Node> indefinites = Maps.newHashMap();
        if (resolveAllReferences(voice, root, questionRoot, indefinites)) {
          results.add(new QueryResult(indefinites, root));
        }
      }
    }

    return results;
  }

  private boolean resolveAllReferences(Voice voice,
                                       Node data,
                                       Node query,
                                       Map<Node, Node> resolvedIndefinites) throws Exception {

    Set<Node> nodesToExplore = Sets.newLinkedHashSet(Lists.newArrayList(data));

    //  get all nodes which refer to the same thing as the original node
    for (RefNode refNode : graph.getRefNodesForHead(data)) {
      Entity identity = refNode.getIdentity();
      for (RefNode refNode1 : identity.getNodes()) {
        nodesToExplore.add(refNode1.getHeadNode());
      }
    }

    //  if any of the references match, consider it a success
    for (Node toExplore : nodesToExplore) {
      if (exploreNode(voice, toExplore, query, resolvedIndefinites)) {
        return true;
      }
    }

    return false;
  }

  private boolean exploreNode(Voice voice,
                              Node toExplore,
                              Node query,
                              Map<Node, Node> resolvedIndefinites) throws Exception {
    if (matchStem(voice, toExplore, query, resolvedIndefinites)) {
      for (Edge edge : query.getOutgoingEdges()) {
        if (!resolveEdge(voice, edge, toExplore.getOutgoingEdges(), resolvedIndefinites)) {
          return false;
        }
      }
      return true;
    }

    return false;
  }

  private boolean resolveEdge(Voice voice,
                              Edge queryEdge,
                              List<Edge> dataEdges,
                              Map<Node, Node> resolvedIndefinites) throws Exception {

    //  "did", "to", etc
    if (IGNORED_QUESTION_CLAUSES.contains(queryEdge.getRelation())) {
      return true;
    }

    for (Edge dataEdge : dataEdges) {

      if (voice.matches(dataEdge, queryEdge)) {

        if (resolveAllReferences(voice,
            dataEdge.getTarget(),
            queryEdge.getTarget(),
            resolvedIndefinites)) {

          return true;
        }
      }
    }
    return false;
  }

  private boolean matchStem(Voice voice,
                            Node data,
                            Node query,
                            Map<Node, Node> resolvedIndefinites) throws Exception {

    if (QUESTION_STEMS.contains(query.getStem().toUpperCase())) {
      resolvedIndefinites.put(query, voice.extractMatch(data));
      return true;
    }

    return nodeComparator.match(data, query);
  }
}
