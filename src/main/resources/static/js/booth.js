function capture() {
    return $.get("camera/capture");
}

function captureSuccess(result) {
    console.log("Successful capture: " + result.photoId);
    hideCountdown();
    showResult();

    $("div#result")
        .css("background-image", "url(image/" + result.photoId + ")");

//    var img = $('<img id="result-img">');
//    img.attr('src', "image/" + result.photoId);
//    img.appendTo("#result");

    setTimeout(() => {
        hideResult()
        showPrompt()
    }, 10000)
}

function captureFailed(result) {
    console.log("Failed capture: " + result);
    hideCountdown();
    alert("Failed to capture image")
    showPrompt();
}

var hideElement = (selector) => $(selector).addClass("hidden")
var showElement = (selector) => $(selector).removeClass("hidden")


var hidePrompt = () => hideElement("#prompt");
var showPrompt = () => showElement("#prompt");

var hideCountdown = () => hideElement("#countdown");
var showCountdown = () => showElement("#countdown");

var hideResult = () => hideElement("#result");
var showResult = () => showElement("#result");

var countdownValue = (val) => $("#countdown-number").text(val);
function startCountdown() {
    hidePrompt();
    showCountdown();

    var countdown = 3; // # seconds until capturing
    countdownValue(countdown);

    var countdownTimer = setInterval(() => {
        console.log("Countdown: " + countdown);
        countdown -= 1;
        countdownValue(countdown);

        if (countdown <= 0) {
            clearInterval(countdownTimer);
            // TODO: Setup spinner while we wait for the capture
            countdownValue("");
            capture()
                .done(captureSuccess)
                .fail(captureFailed);
        }
    }, 1000);
}

$("#capture-btn").click(startCountdown);