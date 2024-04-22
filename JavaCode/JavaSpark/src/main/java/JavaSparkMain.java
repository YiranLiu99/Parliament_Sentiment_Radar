import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.bson.conversions.Bson;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.include;
import static spark.Spark.*;

/**
 * Main Class for Starting the API Server
 * @author Lanouar Dominik Jaouani (implemented and modified)
 */

public class JavaSparkMain {
    //Initializing Class Attributes
    private static MongoDBConnectionHandler mongoDBConnectionHandler = new MongoDBConnectionHandler();

    /**
     * Main Function: Starts the API Server
     * @author Lanouar Dominik Jaouani (implemented and modified)
     */
    public static void main(String[] args) {
        //CORS Problem Handling
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.type("application/json;charset=utf-8 ");
        });

        // http://localhost:4567/speakers
        get("/speakers", (request, response) -> getJSONResult(request, response, "speakers",null, new BasicDBObject()));

        // http://localhost:4567/speechesinfos
        get("/speechesinfos", (request, response) -> getJSONResult(request, response, "speeches", null, exclude("tokenList", "posList", "sentimentList", "namedEntityList","commentList","speechText", "topic", "lemmas")));

        // http://localhost:4567/speechesWtextsANDcomments
        get("/speechesWtextsANDcomments", (request, response) -> getJSONResult(request, response, "speeches", null, include("speechText", "commentList")));

        // http://localhost:4567/tokens
        get("/tokens", (request, response) -> getJSONResult(request, response, "speeches", "token", new BasicDBObject()));

        // http://localhost:4567/pos
        get("/pos", (request, response) -> getJSONResult(request, response, "speeches", "pos", new BasicDBObject()));

        // http://localhost:4567/sentiments
        get("/sentiments", (request, response) -> getJSONResult(request, response, "speeches", "sentiment", new BasicDBObject()));

        // http://localhost:4567/namedEntities
        get("/namedEntities", (request, response) -> getJSONResult(request, response, "speeches", "namedEntity", new BasicDBObject()));

        // http://localhost:4567/treeview
        get("/treeview", (request, response) -> {
            String result = mongoDBConnectionHandler.getTreeViewResult();
            return JSON.parse(result);
        });

        // http://localhost:4567/status
        get("/status", (request, response) -> {
            String result = mongoDBConnectionHandler.getStatus();
            return JSON.parse(result);
        });
    }

    /**
     * Helping Function for building Filterlists and requesting builded json results
     * @author Lanouar Dominik Jaouani (implemented and modified)
     */
    private static Object getJSONResult(Request request, Response response, String collectionName, String analysisType, Bson projectionAttrs){
        //Building FilterLists
        Set<String> parameterSet = request.queryParams();
        List<Bson> filterParts = new ArrayList<>();
        List<Bson> filterParts2 = new ArrayList<>();
        int minocc = 0;
        for(String parameter: parameterSet){
            if(!collectionName.equals("speakers")){
                switch(parameter){
                    case "_id":
                    case "speakerID":
                        filterParts.add(eq(parameter, request.queryParams(parameter)));
                        break;
                    case "party":
                    case "fraction":
                        filterParts2.add(eq(parameter, request.queryParams(parameter)));
                        break;
                    case "minms":
                        try{
                            long paramValue = Long.parseLong(request.queryParams(parameter));
                            filterParts.add(gte("sessionDate", paramValue));
                        }catch (Exception e){}
                        break;
                    case "maxms":
                        try{
                            long paramValue = Long.parseLong(request.queryParams(parameter));
                            filterParts.add(lte("sessionDate", paramValue));
                        }catch (Exception e){}
                        break;
                    case "minocc":
                        try{
                            minocc = Integer.parseInt(request.queryParams(parameter));
                        }catch (Exception e){}
                        break;
                    default:
                        break;
                }
            }else{
                switch(parameter){
                    case "_id":
                    case "party":
                    case "fraction":
                        filterParts.add(eq(parameter, request.queryParams(parameter)));
                        break;
                    default:
                        break;
                }
            }

        }

        //Get ResultJSON over filter
        String result = mongoDBConnectionHandler.getResult(collectionName, filterParts, filterParts2, analysisType, projectionAttrs, minocc);

        //Return result as JSON in API
        return JSON.parse(result);
    }
}
