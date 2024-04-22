var globalUrl = "http://localhost:4567/";

var minTime = "";
var maxTime = "";

// Create all charts:
createTokenChart("", "");
createNamedEntityChart("", "");
createPosChart("", "");
createSentimentChart("", "");
createSpeakerChart("", "");
createTreeview();

/**
 * Function to add an iframe for a new sub dashboard.
 * @author Philipp
 */
function displayIframe() {
    input = document.getElementById("searchInput").value;
    category = document.getElementById("dropdownInput").value;

    document.getElementById("iframeHolder").innerHTML = "<div class=\"col-xl-12 col-lg-5\"><div class=\"card shadow mb-4\"><iframe class=\"card shadow mb-4\" src=\"charts.html?category=" + category + "&input=" + input + "&minTime=" + minTime + "&maxTime=" + maxTime + "\" width=\"100%\" height=\"600\" style=\"border:none;\" ></iframe></div></div>" + document.getElementById("iframeHolder").innerHTML;
}

/**
 * Function to close all iframes.
 * @author Philipp
 */
function closeIframe() {
  document.getElementById("iframeHolder").innerHTML = ""
}

/**
 * Function to set the time.
 * @author Philipp
 */
function setTime() {
  minTime = Date.parse(document.getElementById("minInput").value);
  maxTime = Date.parse(document.getElementById("maxInput").value);
  if (isNaN(minTime)) {
    minTime = "";
  }
  if (isNaN(maxTime)) {
    maxTime = "";
  }
  createTokenChart("", "");
  createNamedEntityChart("", "");
  createPosChart("", "");
  createSentimentChart("", "");
  createSpeakerChart("", "");
}

/**
 * Function for triggering search button with enter key.
 * @author Philipp
 */
function search() {
  if(event.keyCode === 13) {
    event.preventDefault();
    document.getElementById("searchButton").click();
  }
}