// Bar chart:
var posctx = document.getElementById("posChart");
var posChart = new Chart(posctx, {
  type: 'bar',
  data: {
    labels: [],
    datasets: [{
      label: "Anzahl",
      backgroundColor: "#4e73df",
      hoverBackgroundColor: "#2e59d9",
      borderColor: "#4e73df",
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
          maxTicksLimit: 6
        },
        maxBarThickness: 25,
      }],
      yAxes: [{
        ticks: {
          min: 0,
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
    },
  }
});

/**
 * Function to create pos chart.
 * @author Philipp
 * @param category dropdown input
 * @param input user input
 */
function createPosChart(category, input) {
    if (category == "speakerID") {
      min = 10;
    } else {
      min = 1000
    }
    // Ajax request:
    $.ajax({
        url: encodeURI(globalUrl + "pos?minocc=" + min + "&" + category + "=" + input + "&minms=" + minTime + "&maxms=" + maxTime),
        method: 'GET',
        dataType: 'json',
        success: function (data) {
          if (data.message == "SUCCESS") {
            var labelList = [];
            var dataList = [];

            // Get data:
            data.result.forEach(p => {
            labelList.push(p.pos);
            dataList.push(p.count);
            });

            posChart.data.datasets[0].data = dataList;
            posChart.data.labels = labelList;
            posChart.update();

            if (data.result.length == 0) {
              document.getElementById("posHeader").innerHTML = "Keine Daten gefunden.";
            } else {
              document.getElementById("posHeader").innerHTML = "Verteilung POS:";
            }
          } else {
            document.getElementById("posHeader").innerHTML = data.message;
          }
        },
        error: function () {
          document.getElementById("posHeader").innerHTML = "Laden fehlgeschlagen.";
        }
      });
}