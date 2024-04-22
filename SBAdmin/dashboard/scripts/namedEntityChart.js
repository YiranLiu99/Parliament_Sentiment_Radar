// Multi line chart:
var ctx = document.getElementById("namedEntityChart");
var namedEntityChart = new Chart(ctx, {
  type: 'line',
  data: {
    labels: [],
    datasets: [
    {
      label: "Orte",
      backgroundColor: "rgba(78, 115, 223, 0.05)",
      borderColor: "#4e73df",
      pointRadius: 1,
      pointBackgroundColor: "#4e73df",
      pointBorderColor: "#4e73df",
      pointHoverRadius: 3,
      pointHoverBackgroundColor: "#2e59d9",
      pointHoverBorderColor: "#2e59d9",
      pointHitRadius: 10,
      pointBorderWidth: 2,
      data: [],
    },
    {
      label: "Personen",
      backgroundColor: "rgba(78, 115, 223, 0.05)",
      borderColor: "#1cc88a",
      pointRadius: 1,
      pointBackgroundColor: "#1cc88a",
      pointBorderColor: "#1cc88a",
      pointHoverRadius: 3,
      pointHoverBackgroundColor: "#17a673",
      pointHoverBorderColor: "#17a673",
      pointHitRadius: 10,
      pointBorderWidth: 2,
      data: [],
      },
      {
      label: "Organisationen",
      backgroundColor: "rgba(78, 115, 223, 0.05)",
      borderColor: "#36b9cc",
      pointRadius: 1,
      pointBackgroundColor: "#36b9cc",
      pointBorderColor: "#36b9cc",
      pointHoverRadius: 3,
      pointHoverBackgroundColor: "#2c9faf",
      pointHoverBorderColor: "#2c9faf",
      pointHitRadius: 10,
      pointBorderWidth: 2,
      data: [],
      }
    ],
  },
  options: {
    responsive: true,
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
      display: true
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
 * Function to create named entity chart.
 * @author Yiran (edited)
 * @author Philipp
 * @param category dropdown input
 * @param input user input
 */
function createNamedEntityChart(category, input) {
    if (category == "speakerID") {
      min = 1;
    } else if(category == "fraction" || category == "party") {
      min = 40
    } else {
      min = 200
    }
    // Ajax request:
    $.ajax({
        url: encodeURI(globalUrl + "namedEntities?minocc=" + min + "&" + category + "=" + input + "&minms=" + minTime + "&maxms=" + maxTime),
        method: 'GET',
        dataType: 'json',
        success: function (data) {
          if (data.message == "SUCCESS") {
            var locations = data.result[0].locations.length;
            var persons = data.result[1].persons.length;
            var organisations = data.result[2].organisations.length;
            var labelList = [];

            // Get locations:
            var dataLocations = [];
            data.result[0].locations.forEach(e => {
              labelList.push(e.namedEntity);
              dataLocations.push(e.count);
            });
            dataLocations = dataLocations.concat(Array(persons + organisations).fill(0));
            namedEntityChart.data.datasets[0].data = dataLocations;

            // Get persons:
            var dataPersons = Array(locations).fill(0);
            data.result[1].persons.forEach(e => {
              labelList.push(e.namedEntity);
              dataPersons.push(e.count);
            });
            dataPersons = dataPersons.concat(Array(organisations).fill(0));
            namedEntityChart.data.datasets[1].data = dataPersons;

            // Get organisations:
            var dataOrganisations = Array(locations + persons).fill(0);
            data.result[2].organisations.forEach(e => {
              labelList.push(e.namedEntity);
              dataOrganisations.push(e.count);
            });
            namedEntityChart.data.datasets[2].data = dataOrganisations;

            namedEntityChart.data.labels = labelList;
            namedEntityChart.update();

            if (data.result[0].locations.length == 0 && data.result[1].persons.length == 0 && data.result[2].organisations.length == 0) {
              document.getElementById("neHeader").innerHTML = "Keine Daten gefunden.";
            } else {
              document.getElementById("neHeader").innerHTML = "Verteilung NamedEntities:";
            }
          } else {
            document.getElementById("neHeader").innerHTML = data.message;
          }
        },
        error: function () {
          document.getElementById("neHeader").innerHTML = "Laden fehlgeschlagen.";
        }
      });
}