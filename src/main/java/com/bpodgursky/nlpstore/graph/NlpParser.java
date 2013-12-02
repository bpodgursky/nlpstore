package com.bpodgursky.nlpstore.graph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

public class NlpParser {

  private final StanfordCoreNLP pipeline;

  public NlpParser() {
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
    pipeline = new StanfordCoreNLP(props);
  }

  public Node parse(String questionText){

    // create an empty Annotation just with the given text
    Annotation document = new Annotation(questionText);

    // run all Annotators on this text
    pipeline.annotate(document);

    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

    if(sentences.size() != 1){
      throw new RuntimeException("Parsed to multiple sentences! "+questionText);
    }

    CoreMap question = sentences.get(0);

    SemanticGraph semanticGraph = question.get(BasicDependenciesAnnotation.class);
    String sentenceText = semanticGraph.toRecoveredSentenceString();

    Map<IndexedWord, Node> nodes = Maps.newHashMap();

    for (IndexedWord vertex : semanticGraph.vertexSet()) {
      String s = vertex.get(LemmaAnnotation.class);
      Node node = new Node(vertex.word(), s, sentenceText);
      nodes.put(vertex, node);
    }

    for (SemanticGraphEdge edge : semanticGraph.getEdgeSet()) {

      Node source = nodes.get(edge.getSource());
      Node target = nodes.get(edge.getTarget());

      Edge relationEdge = new Edge(edge.getRelation().getLongName(), source, target);

      source.addOutgoingEdge(relationEdge);
      target.addIncomingEdge(relationEdge);
    }

    for (Node node : nodes.values()) {
      if(node.getIncomingEdges().isEmpty()){
        return node;
      }
    }

    throw new RuntimeException("No root node found: "+questionText);
  }

  public void process(String text, KnowledgeGraph graph) {

    // create an empty Annotation just with the given text
    Annotation document = new Annotation(text);

    // run all Annotators on this text
    pipeline.annotate(document);

    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

    TreeMap<Integer, Node> nodesByIndex = Maps.newTreeMap();
    Map<IndexedWord, Node> nodes = Maps.newHashMap();

    int vertexSum = 0;

    int sentenceIndex = 1;
    Map<Integer, Integer> sentenceToOffset = Maps.newHashMap();
    Map<Integer, String> indexToSentence = Maps.newHashMap();

    for (CoreMap sentence : sentences) {
      SemanticGraph semanticGraph = sentence.get(BasicDependenciesAnnotation.class);
      String sentenceText = semanticGraph.toRecoveredSentenceString();

      for (IndexedWord vertex : semanticGraph.vertexSet()) {
        String s = vertex.get(LemmaAnnotation.class);

        Node node = graph.createNode(vertex.word(), s, sentenceText);
        nodes.put(vertex, node);

        nodesByIndex.put(vertexSum + vertex.index(), node);

      }

      indexToSentence.put(sentenceIndex, sentenceText);
      sentenceToOffset.put(sentenceIndex++, vertexSum);
      vertexSum += semanticGraph.vertexSet().size();

      for (IndexedWord vertex : semanticGraph.vertexSet()) {
        for (SemanticGraphEdge outEdge : semanticGraph.getOutEdgesSorted(vertex)) {
          Node source = nodes.get(vertex);
          Node target = nodes.get(outEdge.getTarget());

          graph.addRelation(source, target, outEdge.getRelation().getLongName());
        }
      }
    }

    Map<Integer, CorefChain> corefGraph = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);

    for (Entry<Integer, CorefChain> entry : corefGraph.entrySet()) {

      Set<RefNode> references = Sets.newHashSet();

      for (Entry<IntPair, Set<CorefMention>> entry2 : entry.getValue().getMentionMap().entrySet()) {
        for (CorefMention mention : entry2.getValue()) {

          Integer offset = sentenceToOffset.get(mention.sentNum);

          Collection<Node> values = nodesByIndex.subMap(
              offset + mention.startIndex, true,
              offset + mention.endIndex, false)
              .values();

          RefNode ref = new RefNode(mention.mentionSpan,
              indexToSentence.get(mention.sentNum),
              Lists.newArrayList(values)
          );

          references.add(ref);
          graph.addReference(ref);
        }
      }

      graph.merge(references, IdentitySource.COREFERENCE);

    }

  }
}
