var globalUrl = "http://localhost:4567/";

var category = getParamValue("category");
var input = decodeURI(getParamValue("input"));
var minTime = getParamValue("minTime");
var maxTime = getParamValue("maxTime");

/**
 * Function to get value of url parameter.
 * @author Philipp
 * @param paramName parameter name
 */
function getParamValue(paramName) {
    var url = window.location.search.substring(1);
    var firstArray = url.split('&');

    for (var i = 0; i < firstArray.length; i++) {
        var secondArray = firstArray[i].split('=');
        if (secondArray[0] == paramName) 
            return secondArray[1];
    }
}


// Get output for header:
if (category == "fraction") {
    var output = "Fraktion"
} else if (category == "party") {
    var output = "Partei"
} else if (category == "speakerID") {
    var output = "Redner"
}
document.getElementById("header").innerHTML = input + " (" + output + ")"


// Get speaker id by speaker name:
if (category == "speakerID" && isNaN(parseInt(input))) {
        // Ajax request:
        $.ajax({
            url: encodeURI(globalUrl + "speakers"),
            method: 'GET',
            dataType: 'json',
            success: function (data) {
              if (data.message == "SUCCESS") {
                var speakers =  data.result;

                // Get id:
                for (s in speakers) {
                    if (input == speakers[s].firstName + " " + speakers[s].lastName) {
                        input = speakers[s]._id;
                        break;
                    }
                }

                // Create all charts:
                createTokenChart(category, input);
                createNamedEntityChart(category, input);
                createPosChart(category, input);
                createSentimentChart(category, input);
                createSpeakerChart(category, input);
              } else {
                console.log(data.message);
              }
            },
            error: function () {
              console.log("Error: Speaker ajax ");
            }
        });

}

// Create all charts:
createTokenChart(category, input);
createNamedEntityChart(category, input);
createPosChart(category, input);
createSentimentChart(category, input);
createSpeakerChart(category, input);