package org.ppr.xmlAnalyse.data;

import org.bson.Document;

/**
 * Interface for representing a speaker
 * @author Lanouar Dominik Jaouani (implemented and modified)
 * @author Nuri Arslan (implemented and modified)
 */

public interface Speaker{
    /**
     * Get the iD of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getSpeakerID();

    /**
     * Get the title of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getTitle();

    /**
     * Get the first name of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getFirstName();

    /**
     * Get the name suffix of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getNameSuffix();

    /**
     * Get the last name of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getLastName();

    /**
     * Get the name of the fraction of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getFractionName();

    /**
     * Get the state (Bundesland)
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getState();

    /**
     * Get the place suffix
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getPlaceSuffix();

    /**
     * Get the short role of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getRoleShort();

    /**
     * Get the long role of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getRoleLong();

    /**
     * Get the Picture Array of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String[]
     */
    String[] getPictureArray();

    /**
     * Get the Party Name of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getPartyName();

    /**
     * Get the Birthday of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getBirthday();

    /**
     * Get the Birthplace of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getBirthplace();

    /**
     * Get the Date of Death of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getDateofdeath();

    /**
     * Get the Gender of the speaker
     * @author Lanouar Dominik Jaouani
     * @author Nuri Arslan
     * @return String
     */
    String getGender();

    /**
     * Method to create MongoDB document of speaker.
     * @author Philipp
     * @author Yiran
     * @return MongoDB document
     */
    Document createDocument();

    /**
     * Method to reduce the PictureArray of speaker
     * @author Lanouar Dominik Jaouani (implemented and modified)
     * @author Nuri Arslan (implemented and modified)
     * @param pictureArray
     * @return String[]
     */
    String[] reducePictureArray(String[] pictureArray);
}
