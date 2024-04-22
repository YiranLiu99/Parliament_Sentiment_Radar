package org.ppr.xmlAnalyse.data.impl;
import org.ppr.xmlAnalyse.data.Speaker;

import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for representing a speaker
 * @author Lanouar Dominik Jaouani (implemented and modified)
 * @author Nuri Arslan (implemented and modified)
 */

public class Speaker_File_Impl implements Speaker {
    private String speakerID;
    private String title;
    private String firstName;
    private String nameSuffix;
    private String lastName;
    private String placeSuffix;
    private String fractionName;
    private String roleLong;
    private String roleShort;
    private String state;
    private String[] pictureArray;
    private String birthday;
    private String birthplace;
    private String dateofdeath;
    private String gender;
    private String partyName;

    public Speaker_File_Impl(String speakerID, String title, String firstName, String nameSuffix, String lastName, String placeSuffix, String fractionName, String roleLong, String roleShort, String state, String[] pictureArray, String[] bioArray){
        this.speakerID = speakerID;
        this.title = title;
        this.firstName = firstName;
        this.nameSuffix = nameSuffix;
        this.lastName = lastName;
        this.placeSuffix = placeSuffix;
        if(fractionName == null){
            this.fractionName = "fraktionslos";
        } else{
            this.fractionName = fractionName;
        }
        this.roleLong = roleLong;
        this.roleShort = roleShort;
        this.state = state;
        this.pictureArray = reducePictureArray(pictureArray);
        this.birthday = bioArray[0];
        this.birthplace = bioArray[1];
        this.dateofdeath = bioArray[2];
        this.gender = bioArray[3];
        if(bioArray[4] == null){
            this.partyName = "parteilos";
        } else{
            this.partyName = bioArray[4];
        }
    }

    @Override
    public String[] reducePictureArray(String[] pictureArray){
        try {
            pictureArray[3] = pictureArray[3].replaceAll("Abgebildete\\s*Person:\\s*", "");
            pictureArray[4] = pictureArray[4].replaceAll("Ort:\\s*", "");
            pictureArray[5] = pictureArray[5].replaceAll("Aufgenommen:\\s*", "");
            pictureArray[6] = pictureArray[6].replaceAll("Bildnummer:\\s*", "");
            pictureArray[7] = pictureArray[7].replaceAll("Fotograf/in:\\s*", "");
        }catch (Exception e){}

        return pictureArray;
    }

    @Override
    public Document createDocument() {
        // Create picture map:
        Map<String, String> pictureMap = new HashMap<>();
        pictureMap.put("FotoURL", this.pictureArray[0]);
        pictureMap.put("Einleitung", this.pictureArray[1]);
        pictureMap.put("Bildart", this.pictureArray[2]);
        pictureMap.put("AbgebildetePerson", this.pictureArray[3]);
        pictureMap.put("Ort", this.pictureArray[4]);
        pictureMap.put("Aufgenommen", this.pictureArray[5]);
        pictureMap.put("Bildnummer", this.pictureArray[6]);
        pictureMap.put("Fotograf/in", this.pictureArray[7]);
        return new Document("_id", speakerID).append("title", title).append("firstName", firstName).append("nameSuffix", nameSuffix)
                .append("lastName", lastName).append("placeSuffix", placeSuffix).append("fraction", fractionName).append("party", partyName)
                .append("roleLong", roleLong).append("roleShort", roleShort).append("state", state).append("birthday", birthday)
                .append("birthplace", birthplace).append("dateofdeath", dateofdeath).append("gender", gender).append("picture", pictureMap);
    }

    @Override
    public String getSpeakerID() {
        return speakerID;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getNameSuffix() {
        return nameSuffix;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getFractionName() {
        return fractionName;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public String getPlaceSuffix() {
        return placeSuffix;
    }

    @Override
    public String getRoleShort() {
        return roleShort;
    }

    @Override
    public String getRoleLong() {
        return roleLong;
    }

    @Override
    public String[] getPictureArray() {
        return pictureArray;
    }

    @Override
    public String getPartyName() {
        return partyName;
    }

    @Override
    public String getBirthday() {
        return birthday;
    }

    @Override
    public String getBirthplace() {
        return birthplace;
    }

    @Override
    public String getDateofdeath() {
        return dateofdeath;
    }

    @Override
    public String getGender() {
        return gender;
    }
}
