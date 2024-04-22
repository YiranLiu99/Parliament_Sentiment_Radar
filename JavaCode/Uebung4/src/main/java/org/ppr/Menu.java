package org.ppr;

import org.apache.uima.UIMAException;
import org.ppr.database.MongoDBConnectionHandler;
import org.ppr.database.ParliamentToMongoDB;
import org.ppr.xmlAnalyse.XMLDownloader;

import java.io.IOException;
import java.util.Scanner;

/**
 * Class to print and manage user menu.
 */
public class Menu {
    /**
     * Main method to print and manage user menu.
     * @author Yiran (edited)
     * @author Philipp
     */
    public static void main(String[] args) throws IOException, InterruptedException, UIMAException {
        Scanner scanner = new Scanner(System.in);
        String userInput = "";
        ParliamentToMongoDB toMongoDB = new ParliamentToMongoDB();
        MongoDBConnectionHandler mongoHandler = new MongoDBConnectionHandler();

        // Print user menu:
        while (!userInput.equals("6")) {
            Thread.sleep(1000);
            System.out.println("------------ Menü ------------\n" +
                    "(1) XMLs downloaden\n" +
                    "(2) Datenbank anlegen\n" +
                    "(3) NLP-Analyse starten\n" +
                    "(4) Datenbank leeren\n" +
                    "(5) Count Progress Collection zurücksetzen\n" +
                    "(6) Programm beenden\n" +
                    "------------------------------");
            userInput = scanner.nextLine();

            // Handle input:
            switch (userInput) {
                case "1":
                    XMLDownloader.downloadAllXMLs();
                    System.out.println("Alle XMLs heruntergeladen.");
                    break;
                case "2":
                    System.out.println("Datenbank wird erstellt...");
                    toMongoDB.fillDatabase();
                    break;
                case "3":
                    System.out.println("Starte NLP-Analyse...");
                    toMongoDB.nlpAnalysis();
                    break;
                case "4":
                    System.out.println("Datenbank wirklich leeren?\n" +
                            "------------------------------\n" +
                            "(1) Bestätigen\n(2) Abbrechen");
                    userInput = scanner.nextLine();
                    switch (userInput) {
                        case "1":
                            mongoHandler.deleteMongoCollectionContent("speakers");
                            mongoHandler.deleteMongoCollectionContent("speeches");

                            System.out.println("Datenbank gelöscht.");
                            break;
                        case "2":
                            System.out.println("Abgebrochen.");
                            break;
                        default:
                            System.out.println("Ungültige Eingabe.");
                            break;
                    }
                    break;
                case "5":
                    mongoHandler.resetTotalCountProgressCollection();
                    System.out.println("Alle Collections werden zurückgesetzt.");
                    break;
                case "6":
                    System.out.println("Programm wird beendet...");
                    break;
                default:
                    System.out.println("Falsche Eingabe!");
                    break;
            }
        }
    }

}