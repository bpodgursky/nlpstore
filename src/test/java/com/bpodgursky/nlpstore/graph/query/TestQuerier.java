package com.bpodgursky.nlpstore.graph.query;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.bpodgursky.nlpstore.graph.KnowledgeGraph;
import com.bpodgursky.nlpstore.graph.NlpParser;
import com.bpodgursky.nlpstore.graph.Node;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestQuerier {

  static {
    BasicConfigurator.configure();
    Logger.getLogger("com.bpodgursky").setLevel(Level.DEBUG);
  }

  private NodeComparator comparator;
  private NlpParser parser;

  @Before
  public void setUp() throws Exception {
    comparator = new ThesarusComparator();
    parser = new NlpParser();
  }

  @Test
  public void testPassive() throws Exception {

    String text = "Tom was given a bag";

    KnowledgeGraph graph = new KnowledgeGraph();
    parser.process(text, graph);

    writeDotFile("simple", graph);

    verifyAnswers(graph, parser.parse("Who was given a bag?"),
        Collections.singletonMap(
            "who",
            "Tom"
        )
    );
  }


  //  Not handled well:

  //  "WHERE",
  //  "WHEN",
  //  "WHY",
  //  "HOW",
  //  "WHICH",
  //  "WHEREFORE",
  //  "WHATEVER",

  //  "WHOSE",
  //  "WHEREWITH",
  //  "WITHER",
  //  "WHENCE"

  //  Done, ish:

  //  what
  //  "WHOM",
  //  "WHO",

  private void writeDotFile(String method, KnowledgeGraph graph) throws IOException {
    String dot = graph.toDotFile();
    FileWriter fw = new FileWriter(method+".dot");
    fw.append(dot);
    fw.close();
  }

  @Test
  public void testQuery() throws Exception {

    String text =
        "The Monroe Doctrine was a policy of the United States introduced on December 2, 1823. " +
            "It stated that further efforts by European nations to colonize land or interfere with states in North or South America would be viewed as acts of aggression, requiring U.S. intervention. " +
            "At the same time, the doctrine said that the United States would neither interfere with existing European colonies nor meddle in the internal concerns of European countries. " +
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


//       String text = "The Doctrine was issued at a time when nearly all Latin American colonies of Spain and Portugal had achieved or were at the point of gaining independence from the Portuguese Empire and Spanish Empire; Peru and Bolivia would become independent in 1825, leaving only Cuba and Puerto Rico under Spanish rule. ";


        //    String text = "It would be invoked by many U.S. statesmen and several U.S. presidents, including Theodore Roosevelt, John F. Kennedy, Lyndon B. Johnson, Ronald Reagan and many others.";

    KnowledgeGraph graph = new KnowledgeGraph();
    parser.process(text, graph);

    writeDotFile("doctrine", graph);

    //  TODO: time...
    //    "When was the term \"Monroe Doctrine\" coined?"
    //    "In what year was the term Monroe Doctrine coined?"
    //    "When was the term Monroe Doctrine coined?"

    //  TODO get rid of "by" in extractMatch
    verifyAnswers(graph, parser.parse("Who would invoke it?"),
        Collections.singletonMap(
            "who",
            "by many U.S. statesmen and several U.S. presidents"
        )
    );

    verifyAnswers(graph, parser.parse("What did the doctrine say?"),
        Lists.newArrayList(
            Collections.singletonMap("what",
                "that the United States would neither interfere with existing European colonies"
            ),
            Collections.singletonMap("what",
                "that further efforts by European nations to colonize land or interfere with states in North or South America would be viewed as acts of aggression")
        )
    );

    verifyAnswers(graph, parser.parse("The doctrine stated what?"),
        Lists.newArrayList(
            Collections.singletonMap("what",
                "that the United States would neither interfere with existing European colonies"
            ),
            Collections.singletonMap("what",
                "that further efforts by European nations to colonize land or interfere with states in North or South America would be viewed as acts of aggression")
        )
    );

    verifyAnswers(graph, parser.parse("What was the Monroe Doctrine?"),
        Collections.singletonMap(
            "What was the Monroe Doctrine ?",
            "The Monroe Doctrine was a policy of the United States introduced on December 2 , 1823 ."
        )
    );

//    verifyAnswers(graph, parser.parse("When was the doctrine issued?"),
//        Collections.singletonMap(
//            "When",
//            "at a time when nearly all Latin American colonies of Spain and Portugal had achieved or were at the point of gaining independence from the Portuguese Empire and Spanish Empire"
//        )
//    );
//
//    verifyAnswers(graph, parser.parse("When was the doctrine published?"),
//        Collections.singletonMap(
//            "When",
//            "at a time when nearly all Latin American colonies of Spain and Portugal had achieved or were at the point of gaining independence from the Portuguese Empire and Spanish Empire"
//        )
//    );

    verifyAnswers(graph, parser.parse("What did the Monroe doctrine assert?"),
        Collections.singletonMap(
            "What",
            "that the New World and the Old World were to remain distinctly separate spheres of influence , for they were composed of entirely separate and independent nations"
        )
    );

    verifyAnswers(graph, parser.parse("What did the Monroe doctrine affirm?"),
        Collections.singletonMap(
            "What",
            "that the New World and the Old World were to remain distinctly separate spheres of influence , for they were composed of entirely separate and independent nations"
        )
    );

    verifyAnswers(graph, parser.parse("What did the doctrine assert?"),
        Collections.singletonMap(
            "What",
            "that the New World and the Old World were to remain distinctly separate spheres of influence , for they were composed of entirely separate and independent nations"
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
            "By whom",
            "by many U.S. statesmen and several U.S. presidents"
        )
    );

    verifyAnswers(graph, parser.parse("The United States wanted to guarantee what?"),
        Collections.singletonMap(
            "what",
            "that no European power would move into the Americas"
        )
    );

  }

  @Test
  public void testWhere() throws Exception {

//    String text =
//        "The law school of Beirut was located in Beirut in classical antiquity for the study of Roman law. " +
//        "It flourished under the patronage of the Roman emperors and functioned as the Roman Empire's preeminent center of jurisprudence until its destruction in 551 CE. \n" +
//        "The law schools of the Roman Empire established organized repositories of imperial constitutions and institutionalized the study and practice of jurisprudence to relieve the busy imperial courts. " +
//        "The archiving of imperial constitutions facilitated the task of jurists in referring to legal precedents. " +
//        "The origins of the law school of Beirut are obscure. " +
//        "The earliest written mention of the school dates to 239 CE, when its reputation had already been established. " +
//        "The school attracted young, affluent Roman citizens, and its professors made major contributions to the Codex of Justinian. " +
//        "The school achieved such wide recognition throughout the Empire that Beirut was known as the \"Mother of Laws\". " +
//        "Beirut was one of the few schools allowed to continue teaching jurisprudence when Byzantine emperor Justinian I shut down other provincial law schools. \n" +
//        "The course of study at Beirut lasted for five years and consisted in the revision and analysis of classical juridic texts and imperial constitutions, in addition to case discussions. " +
//        "Justinian took a personal interest in the teaching process, charging the bishop of Beirut, the governor of Phoenicia Maritima and the teachers with discipline maintenance in the school. \n" +
//        "The school's facilities were destroyed in the aftermath of a massive earthquake that hit the Phoenician coastline. " +
//        "It moved to Sidon but did not survive the Arab conquest of 635 CE. " +
//        "Ancient texts attest that the school was located next to the ancient Anastasis church, vestiges of which lie beneath the Saint George Greek Orthodox Cathedral in Beirut's historic center. "+
//    "";

    String text =
        "The school attracted young, affluent Roman citizens, and its professors made major contributions to the Codex of Justinian. " +
        "It moved to Sidon but did not survive the Arab conquest of 635 CE. " ;


        KnowledgeGraph graph = new KnowledgeGraph();
    parser.process(text, graph);

    writeDotFile("where", graph);

    verifyAnswers(graph, parser.parse("Where did the school move?"),
        Collections.singletonMap(
            "where",
            "to Sidon"
        )
    );

    //  TODO "it was moved to Sidon"

    //  TODO "The law school of Beirut was located where"

    //  TODO "The law school of Beirut was a center located in Beirut in classical antiquity for the study of Roman law"

  }


  private void verifyAnswers(KnowledgeGraph data,
                             Node questionRoot,
                             Map<String, String> answer) throws Exception {
    //noinspection unchecked
    verifyAnswers(data, questionRoot, Lists.newArrayList(answer));
  }

  private static int i = 0;

  private void verifyAnswers(KnowledgeGraph data,
                             Node questionRoot,
                             List<Map<String, String>> answers) throws Exception {

    FileWriter fw = new FileWriter("question-" + (i++) + ".dot");
    fw.append(Node.toJson(questionRoot));
    fw.close();

    GraphQuerier querier = new GraphQuerier(data, comparator);

    Set<QueryResult> matches = querier.match(questionRoot);
    System.out.println("matches: "+matches);

    Collection<Map<String, String>> answerText = Collections2.transform(matches, new Function<QueryResult, Map<String, String>>() {
      @Override
      public Map<String, String> apply(QueryResult input) {
        Map<String, String> results = Maps.newHashMap();
        for (Entry<Node, Node> entry : input.getResolvedIndefinites().entrySet()) {
          results.put(Node.getSentencePart(entry.getKey()).toLowerCase(), Node.getSentencePart(entry.getValue()));
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
        System.out.println(Node.getSentencePart(entry.getKey()) + "->" + Node.getSentencePart(entry.getValue()));
      }
    }


    Collection<Map<String, String>> lowercased = Collections2.transform(answers, new Function<Map<String, String>, Map<String, String>>() {
      @Override
      public Map<String, String> apply(Map<String, String> input) {
        Map<String, String> lowercased = Maps.newHashMap();
        for (Entry<String, String> entry : input.entrySet()) {
          lowercased.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return lowercased;
      }
    });

    for (Map<String, String> results : answerText) {
      assertTrue(lowercased.contains(results));
    }

    assertEquals(answers.size(), answerText.size());

  }

}
