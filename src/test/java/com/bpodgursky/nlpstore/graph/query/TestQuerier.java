package com.bpodgursky.nlpstore.graph.query;

import com.bpodgursky.nlpstore.graph.KnowledgeGraph;
import com.bpodgursky.nlpstore.graph.NlpParser;
import com.bpodgursky.nlpstore.graph.Node;
import com.bpodgursky.nlpstore.graph.NodeHelper;
import com.google.common.collect.Lists;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.FileWriter;
import java.util.List;
import java.util.Map.Entry;

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

    //  TODO
    //    "What did the Monroe Doctrine state?";
    //    "What did the United States want to guarantee?"
    //    "Who invoked it?"
    //    "When was the term \"Monroe Doctrine\" coined?"
    //    "In what year was the term Monroe Doctrine coined?"
    //    "When was the term Monroe Doctrine coined?"
    //    "What was The Monroe Doctrine?"

    //  TODO check corefs of things... blurgh

    //  TODO caps

    List<String> questions = Lists.newArrayList(
        "What did the doctrine assert?",
        "Who first stated the doctrine?",
        "By whom would it be invoked?",
        "The United States wanted to guarantee what?"
    );

    int i = 0;
    for (String questionText : questions) {
      Node question = parser.parse(questionText);
      System.out.println();
      System.out.println(questionText);

      List<QueryResult> match = Querier.match(graph, question);
      for (QueryResult queryResult : match) {
        System.out.println();
        System.out.println("Sentence: " + queryResult.getMatchingSentence().getSentenceRef());

        for (Entry<Node, Node> entry : queryResult.getResolvedIndefinites().entrySet()) {
          System.out.println(NodeHelper.getNodeString(entry.getKey()) + " -> " + NodeHelper.getNodeString(entry.getValue()));
        }

        System.out.println(queryResult);

      }

      FileWriter fw = new FileWriter("question-" + (i++) + ".dot");
      fw.append(Node.toJson(question));
      fw.close();
    }

    FileWriter fw = new FileWriter("output.dot");
    fw.append(dot);
    fw.close();
  }

  //  TODO assert
}
