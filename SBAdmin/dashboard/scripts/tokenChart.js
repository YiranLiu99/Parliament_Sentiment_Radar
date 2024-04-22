// Line chart:
var ctx = document.getElementById("tokenChart");
var tokenChart = new Chart(ctx, {
  type: 'line',
  data: {
    labels: [],
    datasets: [{
      label: "Anzahl",
      backgroundColor: "rgba(78, 115, 223, 0.05)",
      borderColor: "rgba(78, 115, 223, 1)",
      pointRadius: 1,
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
    scales: {
      xAxes: [{
        gridLines: {
          display: false,
          drawBorder: false
        },
        ticks: {
          maxTicksLimit: 7
        }
      }],
      yAxes: [{
        ticks: {
          maxTicksLimit: 5,
          padding: 10,
        },
        gridLines: {
          color: "rgb(234, 236, 244)",
          zeroLineColor: "rgb(234, 236, 244)",
          drawBorder: false,
          borderDash: [2],
          zeroLineBorderDash: [2]
        }
      }],
    },
    legend: {
      display: false
    },
    tooltips: {
      backgroundColor: "rgb(255,255,255)",
      bodyFontColor: "#858796",
      titleMarginBottom: 10,
      titleFontColor: '#6e707e',
      titleFontSize: 14,
      borderColor: '#dddfeb',
      borderWidth: 1,
      xPadding: 15,
      yPadding: 15,
      displayColors: false,
      intersect: false,
      mode: 'index',
      caretPadding: 10,
    }
  }
});

/**
 * Function to create token chart.
 * @author Philipp
 * @param category dropdown input
 * @param input user input
 */
function createTokenChart(category, input) {
    if (category == "speakerID") {
      min = 10;
    } else if(category == "fraction" || category == "party") {
      min = 3000
    } else {
      min = 20000
    }
    // Ajax request:
    $.ajax({
        url: encodeURI(globalUrl + "tokens?minocc=" + min + "&" + category + "=" + input + "&minms=" + minTime + "&maxms=" + maxTime),
        method: 'GET',
        dataType: 'json',
        success: function (data) {
          if (data.message == "SUCCESS") {
            var labelList = [];
            var dataList = [];

            // Get data:
            data.result.forEach(t => {
              labelList.push(t.token);
              dataList.push(t.count);
            });

            tokenChart.data.datasets[0].data = dataList;
            tokenChart.data.labels = labelList;
            tokenChart.update();

            if (data.result.length == 0) {
              document.getElementById("tokenHeader").innerHTML = "Keine Daten gefunden.";
            } else {
              document.getElementById("tokenHeader").innerHTML = "Verteilung Token:";
            }
          } else {
            document.getElementById("tokenHeader").innerHTML = data.message;
          }
        },
        error: function () {
          document.getElementById("tokenHeader").innerHTML = "Laden fehlgeschlagen.";
        }
      });
}