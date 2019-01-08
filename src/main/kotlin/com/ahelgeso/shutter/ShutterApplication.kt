package com.ahelgeso.shutter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ShutterApplication

fun main(args: Array<String>) {
    runApplication<ShutterApplication>(*args)
}
