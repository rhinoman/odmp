package io.opendmp.dataflow.api.controller

import io.opendmp.dataflow.model.HelloModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class MainController {

    @GetMapping
    fun hello() : HelloModel {
        return HelloModel()
    }
}