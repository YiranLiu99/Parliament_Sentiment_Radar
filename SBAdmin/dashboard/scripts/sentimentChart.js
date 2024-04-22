// Radar chart:
var ctx = document.getElementById("sentimentChart");
var sentimentChart = new Chart(ctx, {
  type: 'radar',
  data: {
    labels: ["Positiv", "Negativ", "Neutral"],
    datasets: [{
      label: "Anzahl",
      backgroundColor: "rgba(78, 115, 223, 0.05)",
      borderColor: "rgba(78, 115, 223, 1)",
      pointRadius: 3,
      pointBackgroundColor: "rgba(78, 115, 223, 1)",
      pointBorderColor: "rgba(78, 115, 223, 1)",
      pointHoverRadius: 3,
      pointHoverBackgroundColor: "rgba(78, 115, 223, 1)",
      pointHoverBorderColor: "rgba(78, 115, 223, 1)",
      pointHitRadius: 10,
      pointBorderWidth: 2,
      data: [],
    }],
  },
  options: {
    maintainAspectRatio: false,
    layout: {
      padding: {
        left: 10,
        right: 25,
        top: 25,
        bottom: 0
      }
    },
    legend: {
      display: false
    },
    tooltips: {
      titleMarginBottom: 10,
      titleFontColor: '#6e707e',
      titleFontSize: 14,
      backgroundColor: "rgb(255,255,255)",
      bodyFontColor: "#858796",
      borderColor: '#dddfeb',
      borderWidth: 1,
      xPadding: 15,
      yPadding: 15,
      displayColors: false,
      caretPadding: 10,
      callbacks: {
        label: function(tooltipItem, data) {
          return data.labels[tooltipItem.index];
        }
      }
    },
  }
});

/**
 * Function to create sentiment chart.
 * @author Philipp
 * @author Yiran (edited)
 * @param category dropdown input
 * @param input user input
 */
function createSentimentChart(category, input) {
    // Ajax request:
    $.ajax({
        url: encodeURI(globalUrl + "sentiments?" + category + "=" + input + "&minms=" + minTime + "&maxms=" + maxTime),
        method: 'GET',
        dataType: 'json',
        success: function (data) {
          if (data.message == "SUCCESS") {
            var sentiList = new Array();

            // Get data:
            data.result.forEach(s => {
              sentiList[s.sentiment] = s.count;
            });

            var positiv = 0;
            var negativ = 0;
            var neutral = 0;
            for(var key in sentiList){
              if (key > 0.0) {
                positiv += sentiList[key];
              } else if(key < 0.0) {
                negativ += sentiList[key];
              } else {
                neutral += sentiList[key];
              }
            }

            var dataList = [positiv, negativ, neutral]
            sentimentChart.data.datasets[0].data = dataList;
            sentimentChart.update();

            if (data.result.length == 0) {
              document.getElementById("sentimentHeader").innerHTML = "Keine Daten gefunden.";
            } else {
              document.getElementById("sentimentHeader").innerHTML = "Verteilung Sentiments:";
            }
          } else {
            document.getElementById("sentimentHeader").innerHTML = data.message;
          }
        },
        error: function () {
          document.getElementById("sentimentHeader").innerHTML = "Laden fehlgeschlagen.";
        }
      });
}