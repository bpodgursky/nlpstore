package com.bpodgursky.nlpstore.graph.query;

import com.bpodgursky.nlpstore.graph.Edge;
import com.bpodgursky.nlpstore.graph.Entity;
import com.bpodgursky.nlpstore.graph.KnowledgeGraph;
import com.bpodgursky.nlpstore.graph.Node;
import com.bpodgursky.nlpstore.graph.RefNode;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Querier {
  private static final Logger LOG = LoggerFactory.getLogger(Querier.class);

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

  public static List<QueryResult> match(KnowledgeGraph graph, Node questionRoot) {
    List<QueryResult> results = Lists.newArrayList();

    for (Node root : graph.getRoots()) {
      Map<Node, Node> indefinites = Maps.newHashMap();
      if (resolveAllReferences(graph, root, questionRoot, indefinites)) {
        results.add(new QueryResult(indefinites, root));
      }
    }

    return results;
  }

  private static boolean resolveAllReferences(KnowledgeGraph graph,
                                              Node data,
                                              Node query,
                                              Map<Node, Node> resolvedIndefinites) {
    LOG.debug("\n");
    LOG.debug("Comparing: ");
    LOG.debug(data.toString());
    LOG.debug(query.toString());

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
      if(exploreNode(graph, toExplore, query, resolvedIndefinites)){
        return true;
      }
    }

    return false;
  }

  private static boolean exploreNode(KnowledgeGraph graph,
                                     Node toExplore,
                                     Node query,
                                     Map<Node, Node> resolvedIndefinites){
    if (matchStem(toExplore, query, resolvedIndefinites)) {
      LOG.debug("Stems match");
      for (Edge edge : query.getOutgoingEdges()) {
        if (!resolveEdge(graph, edge, toExplore.getOutgoingEdges(), resolvedIndefinites)) {
          LOG.debug("Cannot resolve edge stem: " + edge.getRelation());
          return false;
        }
      }
      return true;
    }

    return false;
  }

  private static boolean resolveEdge(KnowledgeGraph graph,
                                     Edge queryEdge,
                                     List<Edge> dataEdges,
                                     Map<Node, Node> resovledIndefinites) {

    //  "did", "to", etc
    if (IGNORED_QUESTION_CLAUSES.contains(queryEdge.getRelation())) {
      return true;
    }

    for (Edge dataEdge : dataEdges) {

      LOG.debug("Comparing edge: ");
      LOG.debug(dataEdge.getRelation());
      LOG.debug(queryEdge.getRelation());

      if (matchEdge(dataEdge, queryEdge)) {
        if (resolveAllReferences(graph, dataEdge.getTarget(), queryEdge.getTarget(), resovledIndefinites)) {

          LOG.debug("Comparing in edge: ");
          LOG.debug(dataEdge.getTarget().toString());
          LOG.debug(queryEdge.getTarget().toString());

          return true;
        }
      }
    }
    return false;
  }

  //  query - data
  private static final Multimap<String, String> COMPATIBLE_CLAUSES = HashMultimap.create();

  static {
    COMPATIBLE_CLAUSES.put("direct object", "clausal complement");
    COMPATIBLE_CLAUSES.put("attributive", "nominal subject");
    COMPATIBLE_CLAUSES.put("adverbial modifier", "prepositional modifier");
    COMPATIBLE_CLAUSES.put("dependent", "prepositional modifier");
  }

  private static boolean matchEdge(Edge dataEdge, Edge queryEdge) {
    if (dataEdge.getRelation().equals(queryEdge.getRelation())) {
      return true;
    }

    if (COMPATIBLE_CLAUSES.containsEntry(queryEdge.getRelation(), dataEdge.getRelation())) {
      return true;
    }

    return false;
  }

  private static boolean matchStem(Node data, Node query, Map<Node, Node> resolvedIndefinites) {

    if (QUESTION_STEMS.contains(query.getStem().toUpperCase())) {
      resolvedIndefinites.put(query, data);
      return true;
    }

    return data.getStem().toUpperCase().equals(query.getStem().toUpperCase());
  }
}
