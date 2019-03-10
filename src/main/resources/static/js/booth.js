function capture() {
    $.get("camera/capture"), result =>
        { console.log("Result of image capture: " + result)}
}

$("#capture-btn").click(capture);