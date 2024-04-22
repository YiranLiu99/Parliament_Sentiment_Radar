package org.ppr.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.bson.Document;
import org.ppr.nlp.NLPAnalyser;
import org.ppr.xmlAnalyse.XMLReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;

/**
 * Class for filling the database with xml and nlp data.
 */
public class ParliamentToMongoDB {

    MongoDBConnectionHandler connectionHandler;
    MongoCollection<Document> speechCollection;
    MongoCollection<Document> speakersCollection;

    /**
     * Constructor of ParliamentToMongoDB class.
     * @author Yiran
     * @throws IOException IOException
     */
    public ParliamentToMongoDB() throws IOException {
        connectionHandler = new MongoDBConnectionHandler();
        speechCollection = connectionHandler.getSpeechCollection();
        speakersCollection = connectionHandler.getSpeakersCollection();
    }

    /**
     * Method for filling the database with xml data.
     * @author Yiran
     * @author Philipp (edited)
     */
    public void fillDatabase() {

        XMLReader reader = new XMLReader();

        connectionHandler.resetCountProgressCollection("countSpeakersData");
        connectionHandler.resetCountProgressCollection("countSpeechesData");
        connectionHandler.insertAllDoucument(reader);


        System.out.println("Datenbank erstellt.");
    }

    /**
     * Method to convert string to jCas.
     * @author Philipp
     * @param text input string
     * @return jCas
     * @throws UIMAException UIMAException
     */
    private JCas toCAS(String text) throws UIMAException {
        return JCasFactory.createText(text, "de");
    }

    /**
     * Method for filling the database with nlp data.
     * @author Philipp
     * @author Yiran (edited)
     */
    public void nlpAnalysis() {
        int allSpeech = (int) speechCollection.countDocuments(eq("tokenList", null));
        connectionHandler.resetCountProgressCollection("countNLP");
        connectionHandler.updateCountProgressCollection("countNLP", "Total", allSpeech);
        System.out.println(allSpeech + " Reden werden neu eingefügt.");
        int countSpeech = 0;
        while (speechCollection.countDocuments(eq("tokenList", null)) > 0) {
            try {
                FindIterable<Document> speechDocuments = connectionHandler.readAllDocumentInCollection(speechCollection, "tokenList");
                NLPAnalyser analyser = new NLPAnalyser();
                analyser.setTopicsMap();

                for (Document document : speechDocuments) {
                    JCas jCas = toCAS(String.valueOf(document.get("speechText")));
                    SimplePipeline.runPipeline(jCas, analyser.getAnalysisEngine());

                    String textId = String.valueOf(document.get("_id"));

                    // Get all NLP data of this text.
                    HashMap<String, Integer> token = analyser.getTokens(jCas);
                    HashMap<String, Integer> sentiment = analyser.getSentiments(jCas);
                    HashMap<String, Integer> pos = analyser.getPOS(jCas);
                    HashMap<String, ArrayList<Document>> namedEntities = analyser.getNameEntities(jCas);
                    Document topic = analyser.getCategory(jCas);
                    ArrayList<String> lemmas = analyser.getLemma(jCas);

                    // Update all NLPDataMap to speechCollection:
                    connectionHandler.updateDocument(speechCollection, textId, "tokenList", analyser.mapToDocumentArray(token, "token"));
                    connectionHandler.updateDocument(speechCollection, textId, "posList", analyser.mapToDocumentArray(pos, "pos"));
                    connectionHandler.updateDocument(speechCollection, textId, "sentimentList", analyser.mapToDocumentArray(sentiment, "sentiment"));
                    connectionHandler.updateDocument(speechCollection, textId, "namedEntityList", namedEntities);
                    connectionHandler.updateDocument(speechCollection, textId, "topic", topic);
                    connectionHandler.updateDocument(speechCollection, textId, "lemmas", lemmas);

                    countSpeech += 1;
                    if (countSpeech % 5 == 0 || countSpeech == allSpeech) {
                        connectionHandler.updateCountProgressCollection("countNLP", "count", countSpeech);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage() + "\n\nNeustart...");
            }
        }
        System.out.println("NLP-Analyse fertig.");
    }

}