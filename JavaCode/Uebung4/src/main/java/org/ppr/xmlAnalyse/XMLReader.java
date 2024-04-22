package org.ppr.xmlAnalyse;
import org.ppr.database.MongoDBConnectionHandler;
import org.ppr.xmlAnalyse.data.impl.Speaker_File_Impl;
import org.ppr.xmlAnalyse.data.impl.Speech_File_Impl;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class to analyze the XML files and create Objects
 * @author Lanouar Dominik Jaouani
 * @author Nuri Arslan
 */

public class XMLReader {
    private Map<String, Speech_File_Impl> speechMap;
    private Map<String, Speaker_File_Impl> speakerMap;

    public XMLReader(){
        fillAllMaps();
    }

    /**
     * Method to read the XML files, create Speaker/Speech Objects and fill the Maps
     * @author Lanouar Dominik Jaouani (implemented and modified)
     * @author Nuri Arslan (implemented and modified)
     */
    public void fillAllMaps(){
        //Reset Maps
        this.speechMap = new HashMap<>();
        this.speakerMap = new HashMap<>();

        //Create connection to mongoDB
        MongoDBConnectionHandler mongoDBConnectionHandler = null;
        try {
            mongoDBConnectionHandler = new MongoDBConnectionHandler();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Initialize variables for counting
        int datatotal = countXMLFiles();
        int datacounter = 0;

        //Reset and Update counting
        mongoDBConnectionHandler.resetCountProgressCollection("countXMLReader");
        mongoDBConnectionHandler.updateCountProgressCollection("countXMLReader", "Total", datatotal);

        //Create New Instance of DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        //Parsing mdb Stammdaten
        Document mdbDoc = null;
        try{
            DocumentBuilder db2 = dbf.newDocumentBuilder();
            mdbDoc = db2.parse("ProtokollXMLs/MdB-Stammdaten-data/MDB_STAMMDATEN.XML");
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        //Parsing all XMLs
        try {
            //Parse from last XML to first XML
            DocumentBuilder db = dbf.newDocumentBuilder();
            File[] files = new File("ProtokollXMLs/").listFiles();
            for (int a = files.length - 1; a >= 0; a--) {
                File file = files[a];
                if (file.isFile() && file.getName().matches(".*\\.xml")) {
                    //Show Progress in console
                    System.out.println("Die Datei " + file.getName() + " wird gerade bearbeitet...");
                    System.out.println("\tSpeeches werden gesammelt...");

                    //Go through XML aka Session
                    Document xmlDoc = db.parse("ProtokollXMLs/" + file.getName());
                    NodeList sessionInfoNodes = xmlDoc.getElementsByTagName("dbtplenarprotokoll");
                    for(int b = 0; b < sessionInfoNodes.getLength(); b++){
                        Node sessionInfoNode = sessionInfoNodes.item(b);
                        if(sessionInfoNode.getNodeType() == Node.ELEMENT_NODE){
                            Element sessionInfoElement = (Element) sessionInfoNode;

                            String sessionnr = sessionInfoElement.getAttribute("sitzung-nr");
                            String electionperiod = sessionInfoElement.getAttribute("wahlperiode");
                            String sessiondate = sessionInfoElement.getAttribute("sitzung-datum");

                            //Go Through all TOPs
                            List<Element> aiElementList = getElementList(sessionInfoElement, "tagesordnungspunkt");
                            for(Element aiElement:aiElementList){
                                String topid = aiElement.getAttribute("top-id");

                                //Go through all Speeches
                                List<Element> speechElementList = getElementList(aiElement, "rede");
                                for (Element speech : speechElementList) {

                                    String speechID = speech.getAttribute("id");
                                    String speakerID = "";
                                    String speechText = "";
                                    int sameSpeechCounter = 0;
                                    List<String> commentList = new ArrayList<>();
                                    boolean addStatus = false;

                                    //Go through all ChildNodes of a speech
                                    List<Element> speechChildNodeList = getChildElementList(speech);
                                    for (Element speechChild: speechChildNodeList) {
                                        switch (speechChild.getTagName()) {
                                            //If its a <name>-Tag -> So its not a speaker (set addStatus = false) --> The whole text up to this will be added if the tag before was a <p klasse="redner">-Tag (addStatus == true)
                                            case "name":
                                                sameSpeechCounter = addToSpeechMap(speechID, speakerID, speechText, addStatus, mongoDBConnectionHandler, sameSpeechCounter, sessionnr, electionperiod, sessiondate, topid, commentList);
                                                addStatus = false;
                                                speakerID = "";
                                                speechText = "";
                                                commentList.clear();
                                                break;
                                            //If its a <p klasse="redner">-Tag -> So its a speaker (set addStatus = true) --> The whole text up to this will be added if the tag before was a <p klasse="redner">-Tag (addStatus == true)
                                            case "p":
                                                if (speechChild.getAttribute("klasse").equals("redner")) {
                                                    sameSpeechCounter = addToSpeechMap(speechID, speakerID, speechText, addStatus, mongoDBConnectionHandler, sameSpeechCounter, sessionnr, electionperiod, sessiondate, topid, commentList);
                                                    addStatus = true;
                                                    NodeList speakerNodeList = speechChild.getChildNodes();
                                                    for (int e = 0; e < speakerNodeList.getLength(); e++) {
                                                        Node speakerNode = speakerNodeList.item(e);
                                                        if (speakerNode.getNodeType() == Node.ELEMENT_NODE) {
                                                            Element speaker = (Element) speakerNode;
                                                            if (speaker.getTagName().equals("redner")) {
                                                                speakerID = speaker.getAttribute("id");
                                                            }
                                                        }
                                                    }
                                                    speechText = "";
                                                    commentList.clear();
                                                } else {
                                                    if (speechText.equals("")) {
                                                        speechText = speechText + "" + speechChild.getTextContent();
                                                    } else {
                                                        speechText = speechText + " " + speechChild.getTextContent();
                                                    }
                                                }
                                                break;
                                            case "kommentar":
                                                String comment = speechChild.getTextContent();

                                                //Remove first bracket
                                                comment = comment.replaceFirst("\\(","");

                                                //Remove last bracket
                                                String reComment = new StringBuilder(comment).reverse().toString();
                                                reComment = reComment.replaceFirst("\\)","");
                                                comment = new StringBuilder(reComment).reverse().toString();

                                                commentList.add(comment);
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                    //At the end of a xml speech: The whole text up to this will be added if the tag before was a <p klasse="redner">-Tag (addStatus == true)
                                    addToSpeechMap(speechID, speakerID, speechText, addStatus, mongoDBConnectionHandler, sameSpeechCounter, sessionnr, electionperiod, sessiondate, topid, commentList);
                                }
                            }

                            System.out.println("\tSpeeches Sammeln wurde erfolgreich abgeschlossen!");
                            System.out.println("\tSpeakers werden gesammelt...");

                            //Go through all Speakers
                            List<Element> speakerElementList = getElementList(sessionInfoElement, "redner");
                            for(Element speakerElement: speakerElementList){
                                String[] speakerProperties = new String[10];

                                //If Speaker is not in MongoDB, work on the speaker and add it to MongoDB
                                if(!mongoDBConnectionHandler.checkIfDocumentExists("speakers", speakerElement.getAttribute("id"))){
                                    if (!(speakerElement.getAttribute("id").equals("")) && !(speakerMap.containsKey(speakerElement.getAttribute("id")))) {
                                        speakerProperties[0] = speakerElement.getAttribute("id");

                                        List<Element> nameElementList = getElementList(speakerElement, "name");
                                        for (Element name : nameElementList) {
                                            List<Element> childElementList = getChildElementList(name);
                                            for (Element nameChild : childElementList) {
                                                switch (nameChild.getTagName()) {
                                                    case "titel":
                                                        speakerProperties[1] = nameChild.getTextContent();
                                                        break;
                                                    case "vorname":
                                                        speakerProperties[2] = nameChild.getTextContent();
                                                        break;
                                                    case "namenszusatz":
                                                        speakerProperties[3] = nameChild.getTextContent();
                                                        break;
                                                    case "nachname":
                                                        speakerProperties[4] = nameChild.getTextContent();
                                                        break;
                                                    case "ortszusatz":
                                                        speakerProperties[5] = nameChild.getTextContent();
                                                        break;
                                                    case "fraktion":
                                                        speakerProperties[6] = nameChild.getTextContent();
                                                        break;
                                                    case "rolle":
                                                        List<Element> roleElementList = getChildElementList(nameChild);
                                                        for(Element role: roleElementList){
                                                            if (role.getTagName().equals("rolle_lang")) {
                                                                speakerProperties[7] = role.getTextContent();
                                                            }
                                                            if (role.getTagName().equals("rolle_kurz")) {
                                                                speakerProperties[8] = role.getTextContent();
                                                            }
                                                        }
                                                        break;
                                                    case "bdland":
                                                        speakerProperties[9] = nameChild.getTextContent();
                                                        break;
                                                    default:
                                                        break;
                                                }
                                            }
                                        }
                                        String[] pictureArray = producePictureUrl(speakerProperties[2], speakerProperties[4]);
                                        String[] bioArray = getBio(mdbDoc, speakerProperties[0]);
                                        Speaker_File_Impl speaker = new Speaker_File_Impl(speakerProperties[0], speakerProperties[1], speakerProperties[2], speakerProperties[3], speakerProperties[4], speakerProperties[5], speakerProperties[6], speakerProperties[7], speakerProperties[8], speakerProperties[9], pictureArray, bioArray);
                                        this.speakerMap.put(speaker.getSpeakerID(), speaker);
                                    }
                                }else{
                                    System.out.println("\t\tSpeaker " + speakerElement.getAttribute("id") + " ist schon in der DB");
                                }
                            }
                            System.out.println("\tSpeakers Sammeln wurde erfolgreich abgeschlossen!");
                            System.out.println("Die Datei " + file.getName() + " wurde erfolgreich bearbeitet!\n");
                        }
                    }
                    //Update counting
                    datacounter += 1;
                    mongoDBConnectionHandler.updateCountProgressCollection("countXMLReader", "count", datacounter);
                }
            }
        }
        catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to get the Number of Speeches which already exists in DB and add it if not
     * @author Lanouar Dominik Jaouani (implemented and modified)
     * @author Nuri Arslan (implemented and modified)
     * @param speechID
     * @param speakerID
     * @param speechText
     * @param addStatus
     * @param mongoDBConnectionHandler
     * @param sameSpeechCounter
     * @param sessionnr
     * @param electionperiod
     * @param sessiondate
     * @param topid
     * @param commentList
     * @return int
     */
    public int addToSpeechMap(String speechID, String speakerID, String speechText, boolean addStatus, MongoDBConnectionHandler mongoDBConnectionHandler, int sameSpeechCounter, String sessionnr, String electionperiod, String sessiondate, String topid, List<String> commentList){
        if ((!(speakerID.equals(""))) && (!(speechText.equals(""))) && addStatus) {
            sameSpeechCounter = sameSpeechCounter + 1;

            //If Speech is not in MongoDB, work on speech and add it to MongoDB
            if(!mongoDBConnectionHandler.checkIfDocumentExists("speeches", (speechID + "#" + String.valueOf(sameSpeechCounter)))){
                this.speechMap.put((speechID + "#" + String.valueOf(sameSpeechCounter)), new Speech_File_Impl((speechID + "#" + String.valueOf(sameSpeechCounter)), speakerID, speechText, new ArrayList<>(commentList), sessionnr, electionperiod, sessiondate, topid));
            }else{
                System.out.println("\t\tSpeech " + (speechID + "#" + String.valueOf(sameSpeechCounter)) + " ist schon in der DB");
            }
        }
        return sameSpeechCounter;
    }

    /**
     * Method to get the numbers of XML-Files in ProtokollXMLs-Folder
     * @author Lanouar Dominik Jaouani (implemented and modified)
     * @author Nuri Arslan (implemented and modified)
     * @return int
     */
    public int countXMLFiles(){
        //Initializing datatotal Variable
        int datatotal = 0;

        //Count all XML-Files in ProtokollXMLs-Folder
        File[] files = new File("ProtokollXMLs/").listFiles();
        for (int a = files.length - 1; a >= 0; a--) {
            File file = files[a];
            if (file.isFile() && file.getName().matches(".*\\.xml")) {
                datatotal += 1;
            }
        }
        return datatotal;
    }

    /**
     * Method to create the BioArray of a speaker out of mdb-stammdaten
     * @author Lanouar Dominik Jaouani (implemented and modified)
     * @author Nuri Arslan (implemented and modified)
     * @param mdbDoc
     * @param speakerID
     * @return String[]
     */
    private String[] getBio(Document mdbDoc, String speakerID){
        //Initializing bioArray Variable
        String[] bioArray = new String[5];

        if(!(mdbDoc == null)){
            NodeList mdbNodes = mdbDoc.getElementsByTagName("MDB");
            for(int a = 0; a < mdbNodes.getLength(); a++){
                Node mdbNode = mdbNodes.item(a);
                if(mdbNode.getNodeType() == Node.ELEMENT_NODE){
                    Element mdbElement = (Element) mdbNode;

                    String mdbID = getElementList(mdbElement, "ID").get(0).getTextContent();
                    if(mdbID.equals(speakerID)){
                        Element bioElem = getElementList(mdbElement, "BIOGRAFISCHE_ANGABEN").get(0);
                        List<Element> bioElementChildElementS = getChildElementList(bioElem);
                        for (Element bioElementChildElem: bioElementChildElementS) {
                            switch (bioElementChildElem.getTagName()) {
                                case "GEBURTSDATUM":
                                    bioArray[0] = bioElementChildElem.getTextContent();
                                    break;
                                case "GEBURTSORT":
                                    bioArray[1] = bioElementChildElem.getTextContent();
                                    break;
                                case "STERBEDATUM":
                                    bioArray[2] = bioElementChildElem.getTextContent();
                                    break;
                                case "GESCHLECHT":
                                    bioArray[3] = bioElementChildElem.getTextContent();
                                    break;
                                case "PARTEI_KURZ":
                                    if(bioElementChildElem.getTextContent().equals("")){
                                        bioArray[4] = "parteilos";
                                    }else{
                                        bioArray[4] = bioElementChildElem.getTextContent();
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
        }
        return bioArray;
    }

    /**
     * Method to produce the Picture Array of a speaker with all relevant Data (URL, Meta-Data) from the Picture database
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @param firstName
     * @param lastName
     * @return String[]
     */
    public String[] producePictureUrl(String firstName, String lastName) {
        //Initializing pictureArray Variable
        String[] pictureArray = new String[8];

        //Thread Sleep for 250ms so that we dont get kicked off the bundestag server
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Parsing Bilddatenbank
        try {
            //Build Bilddatenbank-URL
            firstName = firstName.replaceAll(" ", "+");
            lastName = lastName.replaceAll(" ", "+");
            String innerPart = lastName + "%2C+" + firstName;
            String picDatabaseUrl = "https://bilddatenbank.bundestag.de/search/picture-result?filterQuery%5Bname%5D%5B%5D=" + innerPart + "&filterQuery%5Bereignis%5D%5B%5D=Portr%C3%A4t%2FPortrait&sortVal=3";
            System.out.println(picDatabaseUrl);

            //Check if the Bilddatenbank-Site has results
            org.jsoup.nodes.Document picDatabaseHTML = Jsoup.connect(picDatabaseUrl).get();
            Elements errors = picDatabaseHTML.select("p");
            boolean error = false;
//            for(org.jsoup.nodes.Element element: errors){
//                if(element.text().equals("Es wurden keine Bilder gefunden.")){
//                    error = true;
//                }
//            }

            //If the Bilddatenbank-Site has no error (Site has results)
            if(!error){
                org.jsoup.nodes.Element firstPictureInDatabase = picDatabaseHTML.getElementsByAttributeValue("data-fancybox", "group").first();
                org.jsoup.nodes.Document firstPictureHTML = Jsoup.connect(firstPictureInDatabase.attr("abs:href")).get();
                org.jsoup.nodes.Element picture = firstPictureHTML.getElementsByTag("figure").first();

                //Get Picture URL
                pictureArray[0] = picture.child(0).attr("abs:src");

                //Get MetaData of the Picture
                String[] metadata = picture.child(1).child(0).child(0).html().replaceAll("\\s*<h6>.*</h6>\\s*","").replaceAll("\\s*<b>.*</b>\\s*","").split("\\s*\n*\\s*<br>\\s*");
                System.out.println(Arrays.toString(metadata));
                //Fill pictureArray with the MetaData
                for(int a = 0; a < metadata.length; a++){
                    pictureArray[a + 1] = metadata[a];
                }
                System.out.println(Arrays.toString(pictureArray));
                System.out.println(111);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pictureArray;
    }

    /**
     * Get the Elemenlist of a parentElement depending on the searching tag
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @param parentElement
     * @param tagName
     * @return List<Element>
     */
    private List<Element> getElementList(Element parentElement, String tagName){
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        List<Element> elementList = new ArrayList<>();
        for (int a = 0; a < nodeList.getLength(); a++) {
            Node node = nodeList.item(a);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                elementList.add(element);
            }
        }
        return elementList;
    }

    /**
     * Get the Child Elementlist of a parentElement
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @param parentNode
     * @return List<Element>
     */
    private List<Element> getChildElementList(Element parentNode){
        NodeList nodeList = parentNode.getChildNodes();
        List<Element> elementList = new ArrayList<>();
        for (int a = 0; a < nodeList.getLength(); a++) {
            Node node = nodeList.item(a);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                elementList.add(element);
            }
        }
        return elementList;
    }

    /**
     * Get the Speaker Map
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return Map<String, Speaker_File_Impl>
     */
    public Map<String, Speaker_File_Impl> getSpeakerMap() {
        return speakerMap;
    }

    /**
     * Get the Speech Map
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return Map<String, Speech_File_Impl>
     */
    public Map<String, Speech_File_Impl> getSpeechMap() {
        return speechMap;
    }
}