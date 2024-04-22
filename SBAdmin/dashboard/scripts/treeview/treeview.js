var config = {'core' : {'data' : {"text" : "Root node","state" : {"opened" : true },"children" : []}}};

/**
 * Funktion to create and handle treeview.
 * @author Yiran
 */
function createTreeview() {
    var jsonObj = [];
    //var root = '{"text" : "Root node","state" : {"opened" : true },"children" : []}';
    //jsonObj.push(JSON.parse(root));
    $.ajax({
        url: encodeURI("http://localhost:4567/treeview"),
        method: 'GET',
        dataType: 'json',
        async: false,
        success: function (data) {
            data.result.forEach(electionPeriod => {
                var electionPeriodObj = {};
                electionPeriodObj.text = "ElectionPeriod " + electionPeriod.ElectionPeriod;
                electionPeriodObj.children = [];
        
                electionPeriod.SessionNrArray.forEach(session => {
                    var sessionObj = {};
                    sessionObj.text = "SessionNum " + session.SessionNr;
                    sessionObj.children = [];
        
                    session.AgendaItemIDArray.forEach(agenda => {
                        var agendaObj = {};
                        agendaObj.text = agenda["Top-ID"];
                        agendaObj.children = [];
        
                        agenda.SpeechIDArray.forEach(speech => {
                            var speechObj = {};
                            speechObj.text = "SpeechID " + speech.SpeechID;
                            agendaObj.children.push(speechObj);
                        });
                        sessionObj.children.push(agendaObj);
                    });
                    electionPeriodObj.children.push(sessionObj);
                });
                
                jsonObj.push(electionPeriodObj);
            });
        }
    });
    config.core.data.children = jsonObj;
}

createTreeview();


$(function() {
    $('#container').on("select_node.jstree", function (e, data) {
        if(data.selected.length) {
            var feedbackArr = data.instance.get_node(data.selected[0]).text.split(" ");
            if(feedbackArr[0] == "SpeechID") {
                var speechID = feedbackArr[1];
                getSpeech(speechID);
            }
        }
    })
    $('#container').jstree(config);
});
