package org.ppr.xmlAnalyse;
import org.ppr.database.MongoDBConnectionHandler;

import net.lingala.zip4j.ZipFile;

import org.apache.commons.io.FileUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for download all important files from the Bundestag website
 * and save them in a Folder ("ProtokollXMLs")
 * @author Lanouar Dominik Jaouani (implemented and modified)
 * @author Nuri Arslan (implemented and modified)
 */

public class XMLDownloader {
    /**
     * Method to download the files
     * @author Lanouar Dominik Jaouani (implemented and modified)
     * @author Nuri Arslan (implemented and modified)
     */
    public static void downloadAllXMLs(){
        try {
            //Get HTML
            Document opendataHTML = Jsoup.connect("https://www.bundestag.de/services/opendata").get();

            //Initialize variables for counting
            int datatotal = getDataCount(opendataHTML);
            int datacounter = 0;

            //Reset and Update counting
            MongoDBConnectionHandler mongoDBConnectionHandler = new MongoDBConnectionHandler();
            mongoDBConnectionHandler.resetCountProgressCollection("countXMLDownloader");
            mongoDBConnectionHandler.updateCountProgressCollection("countXMLDownloader", "Total", datatotal);

            //Downloading DTD und MDB-Stammdaten
            Elements furtherInfoElementS = opendataHTML.getElementsByClass("bt-link-dokument");
            for(Element furtherInfoElem: furtherInfoElementS){
                //Case for dtd
                if(Pattern.matches("DTD für Plenarprotokolle des Deutschen Bundestags, gültig ab 19\\. Wahlperiode.*", furtherInfoElem.attr("title"))){
                    Element dtdElem = furtherInfoElem;
                    try {
                        URL dtdURL = new URL(dtdElem.attr("abs:href"));
                        File dtdFile = new File("ProtokollXMLs/dbtplenarprotokoll.dtd");
                        System.out.println("Downloading DTD from " + dtdURL + " at " + dtdFile);
                        FileUtils.copyURLToFile(dtdURL, dtdFile);
                        datacounter += 1;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //Case for mdb
                if(Pattern.matches("Stammdaten aller Abgeordneten seit 1949 im XML-Format.*", furtherInfoElem.attr("title"))){
                    Element mdbElem = furtherInfoElem;
                    try {
                        URL mdbURL = new URL(mdbElem.attr("abs:href"));
                        File mdbFile = new File("ProtokollXMLs/MdB-Stammdaten-data.zip");
                        System.out.println("Downloading MDB Stammdaten from " + mdbURL + " at " + mdbFile);
                        FileUtils.copyURLToFile(mdbURL, mdbFile);
                        datacounter += 1;
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    //Unzip MDB Stammdaten
                    ZipFile zipFile = new ZipFile("ProtokollXMLs/MdB-Stammdaten-data.zip");
                    zipFile.extractAll("ProtokollXMLs/MdB-Stammdaten-data/");
                }
            }

            //Update counting
            mongoDBConnectionHandler.updateCountProgressCollection("countXMLDownloader", "count", datacounter);

            //Downloading XMLs
            Elements sectionElementS = opendataHTML.getElementsByTag("section");
            for (Element sectionElem: sectionElementS){
                String bttitle = sectionElem.getElementsByClass("bt-title").text();
                if(bttitle.matches("Plenarprotokolle der 19. Wahlperiode|Plenarprotokolle der [2-9][0-9]. Wahlperiode|Plenarprotokolle der [1-9][0-9]{2,}. Wahlperiode")){
                    String modid = (sectionElem.attr("id")).replace("mod", "");
                    int maxoffset = 0;
                    while(true){
                        String url = "https://www.bundestag.de/ajax/filterlist/de/services/opendata/" + modid + "-" + modid + "?limit=10&noFilterSet=true&offset=" + maxoffset;

                        //Counting maxoffset until url doesnt return results - Then download backwards seen from the maxoffset
                        if(checkSite(url)){
                            maxoffset += 10;
                        } else{
                            maxoffset = maxoffset - 10;
                            for(int offset = maxoffset; offset >= 0; offset = offset - 10){
                                url = "https://www.bundestag.de/ajax/filterlist/de/services/opendata/" + modid + "-" + modid + "?limit=10&noFilterSet=true&offset=" + offset;
                                datacounter += downloadTenXMLs(url);

                                //Update counting
                                mongoDBConnectionHandler.updateCountProgressCollection("countXMLDownloader", "count", datacounter);
                            }
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to Get the number of XML downlaods
     * @author Lanouar Dominik Jaouani (implemented and modified)
     * @author Nuri Arslan (implemented and modified)
     * @param opendataHTML
     * @return int
     */
    public static int getDataCount(Document opendataHTML){
        //Initializing datacount variable
        int datacount = 0;

        //Counting all XMLs
        Elements sectionElementS = opendataHTML.getElementsByTag("section");
        for (Element sectionElem: sectionElementS){
            String bttitle = sectionElem.getElementsByClass("bt-title").text();
            if(bttitle.matches("Plenarprotokolle der 19. Wahlperiode|Plenarprotokolle der [2-9][0-9]. Wahlperiode|Plenarprotokolle der [1-9][0-9]{2,}. Wahlperiode")){
                String modid = (sectionElem.attr("id")).replace("mod", "");
                int maxoffset = 0;
                while(true){
                    String url = "https://www.bundestag.de/ajax/filterlist/de/services/opendata/" + modid + "-" + modid + "?limit=10&noFilterSet=true&offset=" + maxoffset;

                    if(checkSite(url)){
                        maxoffset += getPlusOffset(url);
                    }else{
                        break;
                    }
                }
                datacount += maxoffset;
            }
        }

        //Counting other Data
        boolean dtdFound = false;
        boolean mdbFound = false;

        Elements furtherInfoElementS = opendataHTML.getElementsByClass("bt-link-dokument");
        for(Element furtherInfoElem: furtherInfoElementS){
            if(Pattern.matches("DTD für Plenarprotokolle des Deutschen Bundestags, gültig ab 19\\. Wahlperiode.*", furtherInfoElem.attr("title"))){
                dtdFound = true;
            }
            if(Pattern.matches("Stammdaten aller Abgeordneten seit 1949 im XML-Format.*", furtherInfoElem.attr("title"))){
                mdbFound = true;
            }
        }
        datacount += (dtdFound ? 1:0) + (mdbFound ? 1:0);
        return datacount;
    }


    /**
     * Method to get current number of XMLs for download
     * @author Lanouar Dominik Jaouani (implemented and modified)
     * @author Nuri Arslan (implemented and modified)
     * @param url
     * @return int
     */
    public static int getPlusOffset(String url){
        int plusoffset = 0;
        try {
            Document siteHTML = Jsoup.connect(url).get();
            Elements xmlrows = siteHTML.getElementsByTag("a");
            plusoffset = xmlrows.size();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return plusoffset;
    }


    /**
     * Method to download exactly 10 xml files
     * @author Lanouar Dominik Jaouani (implemented and modified)
     * @author Nuri Arslan (implemented and modified)
     * @param url
     */
    private static int downloadTenXMLs(String url){
        int downloadedCounter = 0;
        try {
            Document siteHTML = Jsoup.connect(url).get();
            Elements tbodyElementS = siteHTML.getElementsByTag("tbody");
            for(Element tbodyElem: tbodyElementS){
                Elements xmlrows = tbodyElem.getElementsByTag("a");
                for(int a = xmlrows.size() - 1; a >= 0; a--) {
                    try {
                        URL xmlURL = new URL(xmlrows.get(a).attr("abs:href"));
                        File xmlFile = new File("ProtokollXMLs/" + getFileName(xmlrows.get(a).attr("abs:href")) + ".xml");
                        System.out.println("Downloading XML from " + xmlURL + " at " + xmlFile);
                        FileUtils.copyURLToFile(xmlURL, xmlFile);
                        downloadedCounter += 1;
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return downloadedCounter;
    }

    /**
     * Method to get the Filename from URL
     * @author Lanouar Dominik Jaouani (implemented and modified)
     * @author Nuri Arslan (implemented and modified)
     * @param url
     * @return String
     */
    private static String getFileName(String url){
        Pattern regex = Pattern.compile(".*/(.*?)\\.xml");
        Matcher regexMatcher = regex.matcher(url);
        regexMatcher.find();
        String fileName = regexMatcher.group(1);
        return fileName;
    }

    /**
     * Method to check the Website (URL) if there is an error (no more files)
     * @author Lanouar Dominik Jaouani (implemented and modified)
     * @author Nuri Arslan (implemented and modified)
     * @param url
     * @return boolean
     */
    private static boolean checkSite(String url){
        try {
            Document siteHTML = Jsoup.connect(url).get();
            Elements errors = siteHTML.getElementsByClass("col-xs-12 bt-slide-error bt-slide");
            if(errors.size() >= 1){
                return false;
            }else{
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}