package org.ppr.nlp;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.bson.Document;
import org.hucompute.textimager.fasttext.labelannotator.LabelAnnotatorDocker;
import org.hucompute.textimager.uima.gervader.GerVaderSentiment;
import org.hucompute.textimager.uima.spacy.SpaCyMultiTagger3;
import org.hucompute.textimager.uima.type.Sentiment;
import org.hucompute.textimager.uima.type.category.CategoryCoveredTagged;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

/**
 * Class for uima pipeline and nlp analysis.
 */
public class NLPAnalyser {
    private AnalysisEngine pAE = null;
    private HashMap<String, String> topicsMap = null;

    /**
     * Getter for analysis engine.
     * @author Yiran
     * @return analysis engine
     */
    public AnalysisEngine getAnalysisEngine() {
        return pAE;
    }

    /**
     * Constructor of NLPAnalyser.
     * @author Yiran
     */
    public NLPAnalyser() {
        AggregateBuilder pipeline = new AggregateBuilder();

        // Add different engines to the pipeline:
        try {
            pipeline.add(createEngineDescription(SpaCyMultiTagger3.class,
                    SpaCyMultiTagger3.PARAM_REST_ENDPOINT,
                    "http://spacy.prg2021.texttechnologylab.org"
            ));

            String sPOSMapFile = NLPAnalyser.class.getClassLoader().getResource("am_posmap.txt").getPath();

            pipeline.add(createEngineDescription(LabelAnnotatorDocker.class,
                    LabelAnnotatorDocker.PARAM_FASTTEXT_K, 100,
                    LabelAnnotatorDocker.PARAM_CUTOFF, false,
                    LabelAnnotatorDocker.PARAM_SELECTION, "text",
                    LabelAnnotatorDocker.PARAM_TAGS, "ddc3",
                    LabelAnnotatorDocker.PARAM_USE_LEMMA, true,
                    LabelAnnotatorDocker.PARAM_ADD_POS, true,
                    LabelAnnotatorDocker.PARAM_POSMAP_LOCATION, sPOSMapFile,
                    LabelAnnotatorDocker.PARAM_REMOVE_FUNCTIONWORDS, true,
                    LabelAnnotatorDocker.PARAM_REMOVE_PUNCT, true,
                    LabelAnnotatorDocker.PARAM_REST_ENDPOINT, "http://ddc.prg2021.texttechnologylab.org"
            ));

            pipeline.add(createEngineDescription(GerVaderSentiment.class,
                    GerVaderSentiment.PARAM_REST_ENDPOINT, "http://gervader.prg2021.texttechnologylab.org",
                    GerVaderSentiment.PARAM_SELECTION, "text,de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence"
            ));

            // Create an analysisEngine for running the pipeline:
            pAE = pipeline.createAggregate();


        } catch (UIMAException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to read in ddc3 topic names.
     * @author Philipp
     * @throws IOException IOException
     */
    public void setTopicsMap() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/ddc3-names-de.csv"));
        HashMap<String, String> map = new HashMap<>();

        String line;
        while ((line = reader.readLine()) != null) {
            if(line.length() > 0) {
                String[] lineArray = line.split("\t");
                map.put(lineArray[0], lineArray[1]);
            }
        }
        topicsMap = map;
    }

    /**
     * Method to convert map into array of documents.
     * @author Philipp
     * @param hashMap map
     * @param name key in document
     * @return array of documents
     */
    public ArrayList<Document> mapToDocumentArray(HashMap<String, Integer> hashMap, String name) {
        ArrayList<Document> documentArray = new ArrayList<>();

        for (String key : hashMap.keySet()) {
            documentArray.add(new Document(name, key).append("count", hashMap.get(key)));
        }
        return documentArray;
    }


    /**
     * Method to get the lemma.
     * @author Philipp
     * @param jCas jCas
     * @return array of lemmas
     */
    public ArrayList<String> getLemma(JCas jCas) {
        ArrayList<String> lemmas = new ArrayList<>();
        for (Lemma lemma : JCasUtil.select(jCas, Lemma.class)) {
            lemmas.add(lemma.getCoveredText());
        }
        return lemmas;
    }

    /**
     * Method to get the topic with most scores.
     * @author Philipp
     * @author Yiran (edited)
     * @param jCas jCas
     * @return array of topics
     */
    public Document getCategory(JCas jCas) {
        String topicNumber = "";
        String topic = "";
        for (CategoryCoveredTagged category : JCasUtil.select(jCas, CategoryCoveredTagged.class)) {
            topicNumber = category.getValue().replace("__label_ddc__", "");
            topic = topicsMap.get(topicNumber);
            break;
        }

        return new Document("value", topicNumber).append("topic", topic);
    }

    /**
     * Method to get all sentiments.
     * @author Yiran
     * @author Philipp (edited)
     * @param jCas jCas
     * @return map of sentiments
     */
    public HashMap<String, Integer> getSentiments(JCas jCas) {
        HashMap<String, Integer> sentimentWithFrequency = new HashMap<>();
        for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
            for (Sentiment sentiment : JCasUtil.selectCovered(Sentiment.class, sentence)) {
                String sentimentValue = String.valueOf(sentiment.getSentiment());
                if (sentimentWithFrequency.containsKey(sentimentValue)) {
                    sentimentWithFrequency.put(sentimentValue, sentimentWithFrequency.get(sentimentValue) + 1);
                } else {
                    sentimentWithFrequency.put(sentimentValue, 1);
                }
            }
        }
        return sentimentWithFrequency;
    }

    /**
     * Method to get all tokens.
     * @author Yiran
     * @author Philipp (edited)
     * @param jCas jCas
     * @return map of tokens
     */
    public HashMap<String, Integer> getTokens(JCas jCas) {
        HashMap<String, Integer> tokenWithFrequency = new HashMap<>();
        for (Token token : JCasUtil.select(jCas, Token.class)) {
            String tokenValue = token.getCoveredText();
            if (tokenWithFrequency.containsKey(tokenValue)) {
                tokenWithFrequency.put(tokenValue, tokenWithFrequency.get(tokenValue) + 1);
            } else {
                tokenWithFrequency.put(tokenValue, 1);
            }
        }
        return tokenWithFrequency;
    }

    /**
     * Method to get all POS.
     * @author Yiran
     * @author Philipp (edited)
     * @param jCas jCas
     * @return map of POS
     */
    public HashMap<String, Integer> getPOS(JCas jCas) {
        HashMap<String, Integer> POSWithFrequency = new HashMap<>();
        for (POS pos : JCasUtil.select(jCas, POS.class)) {
            String posValue = pos.getPosValue();
            if (POSWithFrequency.containsKey(posValue)) {
                POSWithFrequency.put(posValue, POSWithFrequency.get(posValue) + 1);
            } else {
                POSWithFrequency.put(posValue, 1);
            }
        }
        return POSWithFrequency;
    }

    /**
     * Method to get all named entities.
     * @author Yiran
     * @author Philipp (edited)
     * @param jCas jCas
     * @return map of named entities
     */
    public HashMap<String, ArrayList<Document>> getNameEntities(JCas jCas) {
        HashMap<String, ArrayList<Document>> NameEntityWithFrequency = new HashMap<>();
        HashMap<String, Integer> persons = new HashMap<>();
        HashMap<String, Integer> locations = new HashMap<>();
        HashMap<String, Integer> organisations = new HashMap<>();

        for (NamedEntity entity : JCasUtil.select(jCas, NamedEntity.class)) {
            String entityText = entity.getCoveredText();
            if (entity.getValue().equals("PER")) {
                if (persons.containsKey(entityText)) {
                    persons.put(entityText, persons.get(entityText) + 1);
                } else {
                    persons.put(entityText, 1);
                }
            } else if (entity.getValue().equals("LOC")) {
                if (locations.containsKey(entityText)) {
                    locations.put(entityText, locations.get(entityText) + 1);
                } else {
                    locations.put(entityText, 1);
                }
            } else if (entity.getValue().equals("ORG")) {
                if (organisations.containsKey(entityText)) {
                    organisations.put(entityText, organisations.get(entityText) + 1);
                } else {
                    organisations.put(entityText, 1);
                }
            }
        }
        NameEntityWithFrequency.put("personList", mapToDocumentArray(persons, "namedEntity"));
        NameEntityWithFrequency.put("locationList", mapToDocumentArray(locations, "namedEntity"));
        NameEntityWithFrequency.put("organisationList", mapToDocumentArray(organisations, "namedEntity"));
        return NameEntityWithFrequency;
    }
}