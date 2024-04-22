// Horizontal bar chart:
var ctx = document.getElementById("speakerChart");
var speakerChart = new Chart(ctx, {
  type: 'horizontalBar',
  data: {
    labels: [],
    imageList: [],
    datasets: [{
      label: "Anzahl Reden",
      backgroundColor: "#36b9cc",
      hoverBackgroundColor: "#2c9faf",
      borderColor: "#4e73df",
      data: [],
    },
    {
      label: "Anzahl Kommentare",
      backgroundColor: "#1cc88a",
      hoverBackgroundColor: "#17a673",
      borderColor: "#1cc88a",
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
          min: 0,
          maxTicksLimit: 6
        },
        maxBarThickness: 25,
      }],
      yAxes: [{
        ticks: {
          min: 0,
          max: 15000,
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
    onHover:(evt) => {
      handleHover(evt, speakerChart);
    },
    legend: {
      display: true
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
 * Function for speaker picture hover effect.
 * @author Solaiman
 * @author Philipp (edited)
 * @param evt event
 * @param chart speaker chart
 */
let img = new Image();
function handleHover(evt, chart) {

  if (chart.getElementAtEvent(evt).length > 0) {
    let activeElement = chart.getElementAtEvent(evt)[0];
    let idx = activeElement._index;
    
    // Size of picture:
    let width = 100;   
    let height = 100;
  
    img.src = speakerChart.data.imageList[idx];  

    // Positioning:
    let y = activeElement._yScale.getPixelForValue(idx)-(height/2);
    let x = activeElement._xScale.getPixelForValue(speakerChart.data.datasets[0].data[idx])-(width + 5);
    
    // Draw the image:
    chart.ctx.drawImage(img, x, y, width, height)
  }
}

/**
 * Function to create speaker chart.
 * @author Philipp
 * @author Yiran (edited)
 * @param category dropdown input
 * @param input user input
 */
function createSpeakerChart(category, input) {
    var min = 1;
    if (category == "speakerID") {
      category = "_id";
    } else if (category == "") {
      min = 100  // Only show the speakers who has more than 100 speeches.
    }
    // Ajax request:
    $.ajax({
      url: encodeURI(globalUrl + "speakers?" + category + "=" + input),
      method: 'GET',
      dataType: 'json',
      success: function (data) {
        if (data.message == "SUCCESS") {
          var labelList = [];
          var dataList = [];
          var dataList2 = [];

          // Get data:
          data.result.forEach(s => {          
            var speakerID = s._id;
            var speeches = 0;
            var comments = 0;

            // Wait for second ajax:
            $.when(speechAjax(speakerID)).done(function(data) {
              if (data.result.length > min) {
                labelList.push(s.firstName + " " + s.lastName);
                if (s.picture.FotoURL == null) {
                  speakerChart.data.imageList.push("https://magral.de/wp-content/themes/magral/assets/images/no_image.jpg");
                } else {
                  speakerChart.data.imageList.push(s.picture.FotoURL);
                }

                speeches += data.result.length;

                data.result.forEach(s => {
                  comments += s.commentList.length;
                });

                dataList.push(speeches);
                dataList2.push(comments);
                speakerChart.data.datasets[0].data = dataList;
                speakerChart.data.datasets[1].data = dataList2;
                speakerChart.data.labels = labelList;
                speakerChart.update();
                console.log(dataList.length)
              }
            });
          });   
            
          if (data.result.length == 0) {
            document.getElementById("speakerHeader").innerHTML = "Keine Daten gefunden.";
          } else {
            
            document.getElementById("speakerHeader").innerHTML = "Verteilung Redner:";
          }
        } else {
          document.getElementById("speakerHeader").innerHTML = data.message;
        }
      },
      error: function () {
        document.getElementById("speakerHeader").innerHTML = "Laden fehlgeschlagen.";
      }
    });
}

/**
 * Function for speaker ajax.
 * @author Philipp
 * @param speakerID speaker id
 * @returns speaker data
 */
function speechAjax(speakerID) {
  // Get countSpeech and countComments
  return $.ajax({
    url: encodeURI(globalUrl + "speechesWtextsANDcomments?speakerID=" + speakerID + "&minms=" + minTime + "&maxms=" + maxTime),
    method: 'GET',
    dataType: 'json',
    success: function(data) {
      if (data.message == "SUCCESS") {
        return data; 
      } else {
        document.getElementById("speakerHeader").innerHTML = data.message;
      }
    },
    error: function () {
      document.getElementById("speakerHeader").innerHTML = "Laden fehlgeschlagen.";
    }
  });
}