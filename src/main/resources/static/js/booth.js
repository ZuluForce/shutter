function capture() {
    return $.get("camera/capture");
}

var previewTimer;

function captureSuccess(result) {
    console.log("Successful capture: " + result.photoId);
    $(".fa-laugh-wink").removeClass("hidden");
    $(".fa-spinner").addClass("hidden");
    hideCountdown();
    showResult();

    $("#preview")
        .css("background-image", "url(image/" + result.photoId + ")");

    previewTimer = setTimeout(() => {
        hideResult()
        showPrompt()
    }, 20000)
}

function captureFailed(result) {
    console.log("Failed capture: " + result);
    hideCountdown();
    alert("Failed to capture image")
    showPrompt();
}

var hideElement = (selector) => $(selector).addClass("hidden")
var showElement = (selector) => $(selector).removeClass("hidden")


var hidePrompt = () => hideElement("#capture");
var showPrompt = () => showElement("#capture");

var hideCountdown = () => hideElement("#counter");
var showCountdown = () => showElement("#counter");

var hideResult = () => hideElement("#preview");
var showResult = () => showElement("#preview");

var countdownValue = (val) => $("#number").text(val);
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
            countdownValue("Loading image...");
            $(".fa-laugh-wink").addClass("hidden");
            $(".fa-spinner").removeClass("hidden");
            capture()
                .done(captureSuccess)
                .fail(captureFailed);
        }
    }, 1000);
}

$("#capture-button").click(startCountdown);
$("#capture-another").click(() => {
    window.clearTimeout(previewTimer);
    hideResult();
    showPrompt();
})