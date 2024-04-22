package org.ppr.xmlAnalyse.data;

import org.bson.Document;

import java.util.List;

/**
 * Interface for representing a speech
 * @author Lanouar Dominik Jaouani (implemented and modified)
 * @author Nuri Arslan (implemented and modified)
 */

public interface Speech {

    /**
     * Get the iD of the speech
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getSpeechID();

    /**
     * Get the iD of the speaker of the speech
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getSpeakerID();

    /**
     * Get the text of the speech
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getSpeechText();

    /**
     * Get all comments of the speech
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return List<String>
     */
    List<String> getCommentList();


    /**
     * Method to create MongoDB document of speech.
     * @author Philipp
     * @author Yiran (edited)
     * @return MongoDB document
     */
    Document createDocument();

    /**
     * Method to convert Session date to SimpleDateFormat
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @param sessiondatestring
     * @return long
     */
    long getMS(String sessiondatestring);

    /**
     * Get the sessiondate of the speech
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return long
     */
    long getSessiondate();

    /**
     * Get the Electionperiod of the speech
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getElectionperiod();

    /**
     * Get the SessionNr of the speech
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getSessionnr();

    /**
     * Get the AgendaID of the speech
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getTopid();


}
