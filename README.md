# Shutter - Another Photo Booth

[![Build Status](https://travis-ci.com/ZuluForce/shutter.svg?branch=master)](https://travis-ci.com/ZuluForce/shutter)

Shutter is yet another photo booth software. If you google for open source photo booth software you will find many options. They're architectures and requirements
are widely varied. Some have tests for part of the application and others for none of it. I started this project for my wedding out of frustration with other
open source photo booths either not working or having an architecture that didn't suite my needs.

The goal of shutter is to provide a limited set of functionality but something that is well documented and well tested. Development choices are heavily weighed towards
maintainability and a big part of that is tests.

## Development Setup

### IDE
This is a gradle project so setting up the dev environment should be as simple as importing into your favorite IDE. IntelliJ was used in the intial setup as it has very
good Kotlin language support. Other IDEs like Eclipse should work fine as well.

### GPhoto
The GPhoto library is also necessary for interacting with the camera. We only need the library, not the gphoto cli.

### Hardware
The GPhoto library allows for a lot of complex operations however Shutter does the most basic photo capture and download. Basically any camera compatible with
GPhoto should support these basic things. Check compatibility here: http://www.gphoto.org/proj/libgphoto2/support.php

Use whatever method to connect the camera to the computer. Shutter does not care how the camera is connected. Those details are handled by GPhoto.

## Architecture

Shutter is a web application entirely. You request a photo to be taken via API calls and you fetch the resulting images via the API. This choice was made to allow
a heterogenous set of devices to use the same service with only needing a web browser. At a wedding for example you may have an iPad serving as the photo booth screen
and you may have phones connecting to look at the photos that have been taken. You may yet have a laptop hooked up to a larger screen showing a slideshow of photos.

TODO: Add architecture image

## Technology

### Spring Boot
Spring boot is used as the application container and to drive the API. We use spring boot to manage the lifecycle of internal components and to wire those components
to the consumer via a rest API.
