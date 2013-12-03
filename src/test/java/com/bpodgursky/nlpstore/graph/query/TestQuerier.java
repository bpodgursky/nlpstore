package com.bpodgursky.nlpstore.graph.query;

import com.bpodgursky.nlpstore.graph.Entity;
import com.bpodgursky.nlpstore.graph.KnowledgeGraph;
import com.bpodgursky.nlpstore.graph.NlpParser;
import com.bpodgursky.nlpstore.graph.Node;
import com.bpodgursky.nlpstore.graph.RefNode;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestQuerier {

  @Test
  public void testQuery() throws Exception {

    String text =
        "The Monroe Doctrine was a policy of the United States introduced on December 2, 1823. " +
            "It stated that further efforts by European nations to colonize land or interfere with states in North or South America would be viewed as acts of aggression, requiring U.S. intervention. " +
            "At the same time, the doctrine noted that the United States would neither interfere with existing European colonies nor meddle in the internal concerns of European countries. " +
            "The Doctrine was issued at a time when nearly all Latin American colonies of Spain and Portugal had achieved or were at the point of gaining independence from the Portuguese Empire and Spanish Empire; Peru and Bolivia would become independent in 1825, leaving only Cuba and Puerto Rico under Spanish rule. " +
            "The United States, working in agreement with Britain, wanted to guarantee that no European power would move into the Americas. " +
            "President James Monroe first stated the doctrine during his seventh annual State of the Union Address to Congress. " +
            "The term Monroe Doctrine itself was coined in 1850. " +
            "By the end of the nineteenth century, Monroe's declaration was seen as a defining moment in the foreign policy of the United States and one of its longest-standing tenets. " +
            "It would be invoked by many U.S. statesmen and several U.S. presidents, including Theodore Roosevelt, John F. Kennedy, Lyndon B. Johnson, Ronald Reagan and many others. " +
            "The intent and impact of the Monroe Doctrine persisted with only minor variations for more than a century. " +
            "Its primary objective was to free the newly independent colonies of Latin America from European intervention and avoid situations which could make the New World a battleground for the Old World powers. " +
            "The doctrine asserted that the New World and the Old World were to remain distinctly separate spheres of influence, for they were composed of entirely separate and independent nations. " +
            "However, the policy became deeply resented by Latin American nations for its overt interventionism and perceived imperialism, and in November 2013, Secretary of State John Kerry declared that \"The era of the Monroe Doctrine is over.\" " +
            "";

    BasicConfigurator.configure();
    Logger.getLogger("com.bpodgursky").setLevel(Level.INFO);

    KnowledgeGraph graph = new KnowledgeGraph();

    NlpParser parser = new NlpParser();
    parser.process(text, graph);

    String dot = graph.toDotFile();
    FileWriter fw = new FileWriter("output.dot");
    fw.append(dot);
    fw.close();

    //  TODO synonyms
    //  TODO prune out relative clause modifiers (others)?  "The Monroe Doctrine was a policy of the United States introduced on December 2, 1823. "
    //    to "The Monroe Doctrine was introduced on December 2, 1823.
    //  TODO: sentence rearranging
    //    "What did the United States want to guarantee?" ("What" vs "What did" seems problematic)
    //  TODO: passive / active restructuring
    //    "Who invoked it?"
    //  TODO: time...
    //    "When was the term \"Monroe Doctrine\" coined?"
    //    "In what year was the term Monroe Doctrine coined?"
    //    "When was the term Monroe Doctrine coined?"
    //  TODO: existence verb weirdness
    //    "What was The Monroe Doctrine?"


    Set<Entity> entities = Sets.newHashSet();
    for (RefNode node : graph.getReferences().values()) {
      entities.add(node.getIdentity());
    }

    System.out.println();
    for (Entity entity : entities) {
      System.out.println();
      for (RefNode node : entity.getNodes()) {
        System.out.println(Node.getSentencePart(node.getHeadNode()));
      }
    }

    verifyAnswers(graph, parser.parse("The doctrine noted what?"),
        Collections.singletonMap(
            "What",
            "that the United States would neither interfere with existing European colonies"
        )
    );

    verifyAnswers(graph, parser.parse("When was the doctrine issued?"),
        Collections.singletonMap(
            "When",
            "at a time when nearly all Latin American colonies of Spain and Portugal had achieved or were at the point of gaining independence from the Portuguese Empire and Spanish Empire; Peru and Bolivia would become independent in 1825, leaving only Cuba and Puerto Rico under Spanish rule"
        )
    );

    verifyAnswers(graph, parser.parse("What did the Monroe doctrine assert?"),
        Collections.singletonMap(
            "What",
            "that the New World and the Old World were to remain distinctly separate spheres of influence for they were composed of entirely separate and independent nations"
        )
    );

    verifyAnswers(graph, parser.parse("What did the doctrine assert?"),
        Collections.singletonMap(
            "What",
            "that the New World and the Old World were to remain distinctly separate spheres of influence for they were composed of entirely separate and independent nations"
        )
    );

    verifyAnswers(graph, parser.parse("Who first stated the doctrine?"),
        Collections.singletonMap(
            "Who",
            "President James Monroe"
        )
    );

    verifyAnswers(graph, parser.parse("By whom would it be invoked?"),
        Collections.singletonMap(
            "whom",
            "many U.S. statesmen and several U.S. presidents"
        )
    );

    verifyAnswers(graph, parser.parse("The United States wanted to guarantee what?"),
        Collections.singletonMap(
            "what",
            "that no European power would move into the Americas"
        )
    );

  }

  private void verifyAnswers(KnowledgeGraph data,
                             Node questionRoot,
                             Map<String, String> answer) throws IOException {
    //noinspection unchecked
    verifyAnswers(data, questionRoot, Lists.newArrayList(answer));
  }

  private static int i = 0;

  private void verifyAnswers(KnowledgeGraph data,
                             Node questionRoot,
                             List<Map<String, String>> answers) throws IOException {

    FileWriter fw = new FileWriter("question-" + (i++) + ".dot");
    fw.append(Node.toJson(questionRoot));
    fw.close();

    List<QueryResult> matches = Querier.match(data, questionRoot);
    Collection<Map<String, String>> answerText = Collections2.transform(matches, new Function<QueryResult, Map<String, String>>() {
      @Override
      public Map<String, String> apply(QueryResult input) {
        Map<String, String> results = Maps.newHashMap();
        for (Entry<Node, Node> entry : input.getResolvedIndefinites().entrySet()) {
          results.put(Node.getSentencePart(entry.getKey()), Node.getSentencePart(entry.getValue()));
        }
        return results;
      }
    });


    System.out.println();
    System.out.println();
    System.out.println(questionRoot.getSentenceRef());
    for (QueryResult match : matches) {
      System.out.println();
      for (Entry<Node, Node> entry : match.getResolvedIndefinites().entrySet()) {
        System.out.println(Node.getSentencePart(entry.getKey())+"->"+Node.getSentencePart(entry.getValue()));
      }
    }

    for (Map<String, String> results : answerText) {
      assertTrue(answers.contains(results));
    }

    assertEquals(answers.size(), answerText.size());

  }

  //  TODO assert
}
