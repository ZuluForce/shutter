package com.ahelgeso.shutter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SystemTest {
    @LocalServerPort
    protected var apiPort: Int = 0
}