package ssu.sdj.digital_receipt.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ssu.sdj.digital_receipt.entity.Data;
import ssu.sdj.digital_receipt.service.DataService;

import java.time.LocalDateTime;

@org.springframework.stereotype.Controller
public class Controller {
    private final DataService dataService;

    public Controller(DataService dataService) {
        this.dataService = dataService;
    }

    // 보안을 개나 줘버린 get 요청
    @GetMapping("/view")
    public String view(@RequestParam String id, @RequestParam String hash, Model model) throws Exception {
        model.addAttribute("data", dataService.getData(id, hash));
        return "view";
    }

    @GetMapping("/viewtest")
    public String viewTest(Model model) {
        String output = "상호: 상도동주민들\n" +
                "대표자: 이지민\n" +
                "사업자번호: 123-45-67890    TEL: 02-000-0000\n" +
                "주소: 서울특별시 동작구 상도로 369\n" +
                "---------------------------------------------\n" +
                "%-20s %3d개 %10s원\n" +
                "---------------------------------------------\n" +
                "거래금액:%33s 원\n" +
                "부 가 세:%33s 원\n" +
                "총 합 계:%33s 원\n" +
                "---------------------------------------------\n" +
                "전자서명전표\n" +
                "---------------------------------------------\n" +
                "찾아주셔서 감사합니다. (고객용)\n";
        System.out.println("Found Data: " + output);

        model.addAttribute("data", output);
        return "view";
    }
}
