/**
 * Get the info of the speaker of a speech.
 * @author Yiran
 * @param speechId speech id
 */
function getSpeaker(speechId) {
    $.ajax({
        url: globalUrl + "speechesinfos?_id=" + encodeURIComponent(speechId),
        method: 'GET',
        dataType: 'json',
        success: function (dataSpeech) {
          if (dataSpeech.message == "SUCCESS") {
            var speakerID = dataSpeech.result[0].speakerID;
            $.ajax({
              url: globalUrl + "speakers?_id=" + encodeURIComponent(speakerID),
              method: 'GET',
              dataType: 'json',
              success: function (dataSpeaker) {
                if (dataSpeaker.message == "SUCCESS") {
                  document.getElementById("speakerInfo").innerHTML = "SpeakerID" + "Name: " + dataSpeaker.result[0].firstName + " " + dataSpeaker.result[0].lastName + ", \n Partei: " + dataSpeaker.result[0].party  + ", \n Fraktion: " +  dataSpeaker.result[0].fraction;
                  document.getElementById("idInfo").innerHTML = "SpeakerID: " + speakerID + "          SpeechID: " + speechId;
                  if(!dataSpeaker.result[0].picture.FotoURL=="") {
                    document.getElementById('speakerImage').width = 400;
                    document.getElementById('speakerImage').height = 300;
                    document.getElementById('speakerImage').src = dataSpeaker.result[0].picture.FotoURL;
                  } else {
                    document.getElementById('speakerImage').width = 400;
                    document.getElementById('speakerImage').height = 300;
                    document.getElementById('speakerImage').src = "https://magral.de/wp-content/themes/magral/assets/images/no_image.jpg";
                  }
                } else {
                  console.log(dataSpeaker.message);
                }
              },
              error: function () {
                console.log("Error: speaker ajax");
              }
            });
          } else {
            console.log(dataSpeech.message);
          }
        },
        error: function () {
          console.log("Error: speechesinfos ajax");
        }
      });
}

/**
 * Get a speech with speechID.
 * @author Yiran
 * @param speech id 
 */
function getSpeech(speechId) {
    $.ajax({
        url: globalUrl + "speechesWtextsANDcomments?_id=" + encodeURIComponent(speechId),
        method: 'GET',
        dataType: 'json',
        success: function (data) {
          if (data.message == "SUCCESS") {
            getSpeaker(speechId);
            document.getElementById("speechText").innerHTML = data.result[0].speechText;
          } else {
            console.log(data.message);
          }
        },
        error: function () {
          console.log("Error: speechesWtextsANDcomments ajax");
        }
      });
}

/**
 * Get speech by text input.
 * @author Yiran
 */
function searchText() {
    var speechId = document.getElementById("searchTextInput").value;
    getSpeech(speechId);
  }