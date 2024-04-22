import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.*;
import org.bson.conversions.Bson;
import javax.json.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Projections.*;

/**
 * Class for Producing and Returning the Result Jsons
 * @author Lanouar Dominik Jaouani (implemented and modified)
 */

public class MongoDBConnectionHandler {
    //Initializing Class Attributes
    private String mbUser;
    private String mbPassword;
    private String mbHost;
    private int mbPort;
    private String mbDatabaseName;
    private MongoCredential mongoCredential;
    private MongoClientOptions mongoClientOptions;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public MongoDBConnectionHandler() {
        //Load MongoDB-LogIn-Data
        Properties properties = new Properties();
        String path = "src/main/resources/PRG_WiSe21_Gruppe_1_1.txt";
        try {
            properties.load(new FileInputStream(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mbUser = properties.getProperty("remote_user");
        mbPassword = properties.getProperty("remote_password");
        mbHost = properties.getProperty("remote_host");
        mbPort = Integer.parseInt(properties.getProperty("remote_port"));
        mbDatabaseName = properties.getProperty("remote_database");

        //Create MongoDB-Connection
        this.mongoCredential = MongoCredential.createCredential(mbUser, mbDatabaseName, mbPassword.toCharArray());
        this.mongoClientOptions = MongoClientOptions.builder().serverSelectionTimeout(5000).sslEnabled(false).build();
        this.mongoClient = new MongoClient(new ServerAddress(mbHost, mbPort), Arrays.asList(mongoCredential), mongoClientOptions);
        this.mongoDatabase = mongoClient.getDatabase(mbDatabaseName);
    }

    /**
     * Function for building and returning the TreeviewJson
     * @author Lanouar Dominik Jaouani (implemented and modified)
     */
    public String getTreeViewResult(){
        //Get Collection
        MongoCollection<Document> collection = this.mongoDatabase.getCollection("speeches");
        String message = "SUCCESS";

        //Get Documents and fill treeviewmap
        Map<String,Map> treeviewMap = new HashMap<>();
        FindIterable<Document> findIterable = collection.find(new BasicDBObject()).projection(fields(include("electionPeriod", "agendaItemId", "sessionNumber")));
        try{for(Document doc: findIterable){
            treeviewMap = fillMaps(treeviewMap, doc, "electionPeriod");
        }}catch (Exception e){
            message = "ERROR HAPPENED - PLEASE LOOK IF YOURE CONNECTED TO YOUR INTERNET";
        }

        //Build Json
        JsonArrayBuilder jsonArrayElectionPeriods = Json.createArrayBuilder();
        for(String electionPeriod: treeviewMap.keySet()){

            JsonArrayBuilder jsonArraySessions = Json.createArrayBuilder();
            Map<String,Map> sessionMap = (Map<String,Map>) treeviewMap.get(electionPeriod);
            for(String sessionNR: sessionMap.keySet()){

                JsonArrayBuilder jsonArrayTOPs = Json.createArrayBuilder();
                Map<String,Set> topMap = (Map<String,Set>) sessionMap.get(sessionNR);
                for(String topid: topMap.keySet()){

                    JsonArrayBuilder jsonArraySpeechID = Json.createArrayBuilder();
                    Set<String> idSet = (Set<String>) topMap.get(topid);
                    for(String speechID: idSet){
                        jsonArraySpeechID.add(Json.createObjectBuilder().add("SpeechID", speechID).build());
                    }

                    JsonObject jsonObjectTop = Json.createObjectBuilder().add("Top-ID", topid).add("SpeechIDArray", jsonArraySpeechID.build()).build();

                    jsonArrayTOPs.add(jsonObjectTop);
                }

                JsonObject jsonObjectSession = Json.createObjectBuilder().add("SessionNr",sessionNR).add("AgendaItemIDArray", jsonArrayTOPs.build()).build();

                jsonArraySessions.add(jsonObjectSession);
            }

            JsonObject jsonObjectElectionPeriod = Json.createObjectBuilder().add("ElectionPeriod", electionPeriod).add("SessionNrArray",jsonArraySessions.build()).build();

            jsonArrayElectionPeriods.add(jsonObjectElectionPeriod);
        }

        //Return Json
        String result = "{\"result\":" + jsonArrayElectionPeriods.build() + ", \"message\":\"" + message + "\"}";
        return result;
    }

    /**
     * Function for building and returning the StatusJson
     * @author Lanouar Dominik Jaouani (implemented and modified)
     */
    public String getStatus(){
        //Get Collection
        MongoCollection<Document> collection = this.mongoDatabase.getCollection("countProgress");
        String message = "SUCCESS";

        //Build Json
        JsonArrayBuilder jsonArrayResult = Json.createArrayBuilder();
        FindIterable<Document> findIterable = collection.find(new BasicDBObject());
        try{for(Document doc: findIterable){
            JsonObject jsonObject = Json.createObjectBuilder().add(String.valueOf(doc.get("_id")), Json.createObjectBuilder().add("Total", String.valueOf(doc.get("Total"))).add("Count",String.valueOf(doc.get("count"))).build()).build();
            jsonArrayResult.add(jsonObject);
        }}catch (Exception e){
            message = "ERROR HAPPENED - PLEASE LOOK IF YOURE CONNECTED TO YOUR INTERNET";
        }

        //Return Json
        String result = "{\"result\":" + jsonArrayResult.build() + ", \"message\":\"" + message + "\"}";
        return result;
    }

    /**
     * Function for building and returning all other Jsons
     * @author Lanouar Dominik Jaouani (implemented and modified)
     */
    public String getResult(String collectionName, List<Bson> filterParts, List<Bson> filterParts2, String analysisType, Bson projectionAttrs, int minocc){
        //Get Collection
        MongoCollection<Document> collection = this.mongoDatabase.getCollection(collectionName);
        String message = "SUCCESS";

        //Build Filter
        if(!filterParts2.isEmpty()){
            Bson filter2 = and(filterParts2);
            List<String> speakerIDList = getSpeakerIDList(filter2);
            if (speakerIDList == null){
                message = "ERROR HAPPENED - PLEASE LOOK IF YOURE CONNECTED TO YOUR INTERNET";
                filterParts.add(new BasicDBObject("speakerID", new BasicDBObject("$in", new ArrayList<>())));
            }else{
                filterParts.add(new BasicDBObject("speakerID", new BasicDBObject("$in", speakerIDList)));
            }
        }
        Bson filter;
        if(!filterParts.isEmpty()){
            filter = and(filterParts);
        }else{
            filter = new BasicDBObject();
        }

        //Build Json
        String result;
        if(analysisType == null){
            result = "{\"result\":[";
            FindIterable<Document> findIterable = collection.find(filter).projection(fields(projectionAttrs));
            try{for(Document doc: findIterable){
                if(!result.equals("{\"result\":[")){
                    result = result + "," + doc.toJson();
                }else{
                    result = result + doc.toJson();
                }
            }}catch (Exception e){
                message = "ERROR HAPPENED - PLEASE LOOK IF YOURE CONNECTED TO YOUR INTERNET";
            }
            result = result + "], \"message\":\"" + message + "\"}";
        }else{
            result = "{\"result\":";
            FindIterable<Document> findIterable = collection.find(filter).projection(fields(include(analysisType + "List"),exclude("_id")));
            if(analysisType.equals("namedEntity")){
                List<Document> locationDocList = new ArrayList<>();
                List<Document> personDocList = new ArrayList<>();
                List<Document> organisationDocList = new ArrayList<>();
                try{
                    for(Document doc: findIterable){
                        Document doc2 = (Document) doc.get("namedEntityList");
                        locationDocList.addAll((List<Document>) doc2.get("locationList"));
                        personDocList.addAll((List<Document>) doc2.get("personList"));
                        organisationDocList.addAll((List<Document>) doc2.get("organisationList"));
                    }

                }catch(Exception e){
                    message = "ERROR HAPPENED - PLEASE LOOK IF YOURE CONNECTED TO YOUR INTERNET";
                }
                result += "[{\"locations\":";

                Map<String, Integer> locationMap = fillAnalyseMap(locationDocList, analysisType);
                JsonArray jsonArray1 = getSortedJsonArray(locationMap, minocc, analysisType);

                result += jsonArray1 + "},{\"persons\":";

                Map<String, Integer> personMap = fillAnalyseMap(personDocList, analysisType);
                JsonArray jsonArray2 = getSortedJsonArray(personMap, minocc, analysisType);

                result += jsonArray2 + "},{\"organisations\":";

                Map<String, Integer> organisationMap = fillAnalyseMap(organisationDocList, analysisType);
                JsonArray jsonArray3 = getSortedJsonArray(organisationMap, minocc, analysisType);

                result += jsonArray3 + "}], \"message\":\"" + message + "\"}";
            }else{
                Map<String, Integer> countmap = fillAnalyseMap(findIterable, analysisType);
                if(countmap == null){
                    message = "ERROR HAPPENED - PLEASE LOOK IF YOURE CONNECTED TO YOUR INTERNET";
                    result += "[], \"message\":\"" + message + "\"}";
                }else{
                    JsonArray jsonArray = getSortedJsonArray(countmap, minocc, analysisType);
                    result += jsonArray.toString() + ", \"message\":\"" + message + "\"}";
                }
            }
        }

        //Return Json
        return result;
    }

    /**
     * Function for building and returning the TreeviewJson
     * @author Lanouar Dominik Jaouani (implemented and modified)
     */
    public Map fillMaps(Map map, Document doc, String type){
        String value = String.valueOf(doc.get(type));
        if(!map.containsKey(value)){
            switch(type){
                case "electionPeriod":
                    Map<String, Map> sessionNrMap = new HashMap<>();
                    sessionNrMap = fillMaps(sessionNrMap, doc, "sessionNumber");
                    map.put(value, sessionNrMap);
                    break;
                case "sessionNumber":
                    Map<String, Set> topMap = new HashMap<>();
                    topMap = fillMaps(topMap, doc, "agendaItemId");
                    map.put(value, topMap);
                    break;
                case "agendaItemId":
                    Set<String> idSet = new HashSet<>();
                    idSet.add(String.valueOf(doc.get("_id")));
                    map.put(value, idSet);
                    break;
                default:
                    break;
            }
        }else{
            switch(type){
                case "electionPeriod":
                    Map<String, Map> sessionNrMap = (Map<String, Map>) map.get(value);
                    sessionNrMap = fillMaps(sessionNrMap, doc, "sessionNumber");
                    break;
                case "sessionNumber":
                    Map<String, Set> topMap = (Map<String, Set>) map.get(value);
                    topMap = fillMaps(topMap, doc, "agendaItemId");
                    break;
                case "agendaItemId":
                    Set<String> idSet = (Set<String>) map.get(value);
                    idSet.add(String.valueOf(doc.get("_id")));
                    break;
                default:
                    break;
            }
        }

        return map;
    }

    /**
     * Function for building and returning sorted JsonArray
     * @author Lanouar Dominik Jaouani (implemented and modified)
     */
    public JsonArray getSortedJsonArray(Map<String, Integer> countmap, int minocc, String analysisType){
        //Sort Hashmap after value - catch only those elements which are over the minocc
        LinkedHashMap<String, Integer> sortedmap = new LinkedHashMap<>();
        countmap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(x-> {
            if(x.getValue() >= minocc){
                sortedmap.put(x.getKey(),x.getValue());
            }
        });

        //Build Json
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for(String key: sortedmap.keySet()){
            JsonObject empObject = Json.createObjectBuilder().add(analysisType, key).add("count", sortedmap.get(key)).build();
            jsonArrayBuilder.add(empObject);
        }

        return jsonArrayBuilder.build();
    }

    /**
     * Function1 for filling and returning an analyse Map
     * @author Lanouar Dominik Jaouani (implemented and modified)
     */
    public Map<String, Integer> fillAnalyseMap(List<Document> documentList, String analysisType){
        Map<String, Integer> countmap = new HashMap<>();
        for(Document doc2 : documentList) {
            String name = (String) doc2.get(analysisType);
            int count = (int) doc2.get("count");
            if (!countmap.containsKey(name)) {
                countmap.put(name, count);
            } else {
                countmap.put(name, countmap.get(name) + count);
            }
        }
        return countmap;
    }

    /**
     * Function2 for filling and returning an analyse Map
     * @author Lanouar Dominik Jaouani (implemented and modified)
     */
    public Map<String, Integer> fillAnalyseMap(FindIterable<Document> findIterable, String analysisType){
        Map<String, Integer> countmap = new HashMap<>();
        try{
            for(Document doc: findIterable){
                List<Document> documentList = (List<Document>) doc.get(analysisType + "List");
                for(Document doc2 : documentList) {
                    String name = (String) doc2.get(analysisType);
                    int count = (int) doc2.get("count");
                    if (!countmap.containsKey(name)) {
                        countmap.put(name, count);
                    } else {
                        countmap.put(name, countmap.get(name) + count);
                    }
                }
            }
        }catch (Exception e){
            return null;
        }
        return countmap;
    }

    /**
     * Function for building a speakeridlist which includes the request parameters
     * @author Lanouar Dominik Jaouani (implemented and modified)
     */
    public List<String> getSpeakerIDList(Bson filter){
        MongoCollection<Document> collection = this.mongoDatabase.getCollection("speakers");
        List<String> speakerIDList = new ArrayList<>();

        FindIterable<Document> findIterable = collection.find(filter);

        try{
            for(Document doc: findIterable) {
                speakerIDList.add(String.valueOf(doc.get("_id")));
            }
        }catch (Exception e){
            return null;
        }

        return speakerIDList;
    }
}