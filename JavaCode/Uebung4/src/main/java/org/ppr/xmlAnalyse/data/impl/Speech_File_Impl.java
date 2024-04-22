package org.ppr.xmlAnalyse.data.impl;
import org.ppr.xmlAnalyse.data.Speech;

import org.bson.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.List;

/**
 * Class for representing a speech
 * @author Lanouar Dominik Jaouani (implemented and modified)
 * @author Nuri Arslan (implemented and modified)
 */

public class Speech_File_Impl implements Speech {
    private String speechID;
    private String speakerID;
    private String speechText;
    private List<String> commentList;
    private String sessionnr;
    private String electionperiod;
    private long sessiondate;
    private String topid;

    public Speech_File_Impl(String speechID, String speakerID, String speechText, List<String> commentList, String sessionnr, String electionperiod, String sessiondatestring, String topid){
        this.speechID = speechID;
        this.speakerID = speakerID;
        this.speechText = speechText;
        this.commentList = commentList;
        this.sessionnr = sessionnr;
        this.electionperiod = electionperiod;
        this.sessiondate = getMS(sessiondatestring);
        this.topid = topid;
    }

    @Override
    public Document createDocument() {
        return new Document("_id", speechID).append("speakerID", speakerID).append("speechText", speechText)
                .append("sessionNumber", sessionnr).append("sessionDate", sessiondate)
                .append("electionPeriod", electionperiod).append("agendaItemId", topid).append("commentList", commentList);
    }

    @Override
    public long getMS(String sessiondatestring){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        long ms = 0;
        try {
            ms = simpleDateFormat.parse(sessiondatestring).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ms;
    }

    @Override
    public long getSessiondate() {
        return sessiondate;
    }

    @Override
    public String getElectionperiod() {
        return electionperiod;
    }

    @Override
    public String getSessionnr() {
        return sessionnr;
    }

    @Override
    public String getTopid() {
        return topid;
    }

    @Override
    public String getSpeechID() {
        return speechID;
    }

    @Override
    public String getSpeakerID() {
        return speakerID;
    }

    @Override
    public String getSpeechText() {
        return speechText;
    }

    @Override
    public List<String> getCommentList() {
        return commentList;
    }
}
