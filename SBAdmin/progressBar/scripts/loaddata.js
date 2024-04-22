/**
 * JavaScript for "Fortschrittsanzeige"
 * @author Lanouar Dominik Jaouani
 * @author Nuri Arslan
 * @author Philipp Stephan (edited updateBar())
 */

//Caption with server status
var caption = document.getElementById("caption");

//Start updating bars every half second
update();

/**
 * Function for updating bars every half second
 * @author Lanouar Dominik Jaouani
 * @author Nuri Arslan
 */
function update(){
    $.ajax({
        url: "http://localhost:4567/status",
        method: 'GET',
        dataType: 'json',
        success: function (d) {
            //Show that server is online
            caption.innerHTML = "Fortschritt (API-Server ist online)";
            
            //Update every bar
            for (var i = 0; i <= 4; i++) {
                updateBar(i, d);
            }

            //Update again
            setTimeout(update, 500);
        },
        error: function(d){
            //Show that server is offline
            caption.innerHTML = "Fortschritt (API-Server ist offline - Warten Sie hier bis der Server wieder online ist - Page Refresh ist nicht nötig)";
            console.log(d);

            //Update again
            setTimeout(update, 500);
        }
    });
}

/**
 * Function for updating bar (i+1)
 * @author Lanouar Dominik Jaouani
 * @author Nuri Arslan
 * @author Philipp Stephan (edited updateBar())
 */
function updateBar(i, d){
    //Get the HTML-Elements
    var a = i + 1;
    var textDisplay = document.getElementById("textDisplay" + a);
    var percentageDisplay = document.getElementById("percentageDisplay" + a);
    var barDisplay = document.getElementById("barDisplay" + a);
    
    try{
        //Calculate with the results from API
        var total = 0;
        var count = 0;
        var addtext = "";
        switch (i) {
            case 0:
                total = d.result[i].countXMLDownloader.Total;
                count = d.result[i].countXMLDownloader.Count;
                addtext = " XMLS HERUNTERGELADEN:";
                break;
            case 1:
                total = d.result[i].countXMLReader.Total;
                count = d.result[i].countXMLReader.Count;
                addtext = " PROTOKOLLE BEARBEITET:";
                break;
            case 2:
                total = d.result[i].countSpeakersData.Total;
                count = d.result[i].countSpeakersData.Count;
                addtext = " SPEAKER HOCHGELADEN:";
                break;
            case 3:
                total = d.result[i].countSpeechesData.Total;
                count = d.result[i].countSpeechesData.Count;
                addtext = " SPEECHES HOCHGELADEN:";
                break;
            case 4:
                total = d.result[i].countNLP.Total;
                count = d.result[i].countNLP.Count;
                addtext = " NLP ANALYSEN:";
                break;
            default:
                break;
        }
        var result = count/total;
        var percentage = getroundedpercentage(result);
        if(isNaN(percentage)){
            percentage = 0;
        }

        //Display the results
        textDisplay.innerHTML = count + " VON " + total + addtext;
        percentageDisplay.innerHTML = percentage.toString() + "%";
        barDisplay.setAttribute("style", "width: " + percentage.toString() + "%");
    }catch(error){
        textDisplay.innerHTML = "Es ist ein Fehler aufgetreten, für weitere Infos bitte in der Console nachsehen!";
        console.log(error);
    }
}

/**
 * Function for returning the percentage rounded to 2 decimal places
 * @author Lanouar Dominik Jaouani
 * @author Nuri Arslan
 */
function getroundedpercentage(number){
    return (Math.round((number * 10000)) / 100);
}