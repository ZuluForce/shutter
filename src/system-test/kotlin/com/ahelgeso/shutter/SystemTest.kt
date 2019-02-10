package com.ahelgeso.shutter

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SystemTest {
    @LocalServerPort
    protected var apiPort: Int = 0
}