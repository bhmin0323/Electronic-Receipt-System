package ssu.sdj.digital_receipt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ssu.sdj.digital_receipt.service.DataService;


@org.springframework.web.bind.annotation.RestController
public class RestController {
    private final DataService dataService;

    public RestController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/upload")
    public String upload(@RequestParam(required = false) String data) {
        return dataService.saveData(data);
    }
}
