package ssu.sdj.digital_receipt.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ssu.sdj.digital_receipt.DTO.ViewDTO;
import ssu.sdj.digital_receipt.entity.Data;
import ssu.sdj.digital_receipt.service.DataService;
import ssu.sdj.digital_receipt.utils.HashUtils;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@org.springframework.web.bind.annotation.RestController
public class RestController {
    private final DataService dataService;

    public RestController(DataService dataService) {
        this.dataService = dataService;
    }

    // Base64 디코딩 함수
    // Base64 디코딩 후 CP949로 디코딩 함수
    private String base64DecodeToCP949(String data) {
        // Base64 디코딩
        byte[] decodedBytes = Base64.getDecoder().decode(data);

        // CP949로 디코딩
        return new String(decodedBytes, Charset.forName("CP949"));
    }

    // 8자리 랜덤 숫자 생성 함수
    private String generateRandomNumber() {
        Random random = new Random();
        int number = 10000000 + random.nextInt(90000000);
        return Integer.toString(number);
    }

    @GetMapping("/upload")
    public String upload(@RequestParam(required = false) String data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Bad Request: Missing data parameter");
        }

        String decodedData;
        String randomNumber;

        try {
            // '+' 문자를 공백으로 변환하지 않도록 처리
            data = data.replace(" ", "+");

            // Base64 디코딩하여 출력
            decodedData = base64DecodeToCP949(data);
            decodedData = decodedData.replace("\r\n", "\n").replace("\r", "\n");
            System.out.println("Decoded Data: " + decodedData);

            // 8자리 랜덤 숫자 생성
            do {
                randomNumber = generateRandomNumber();
            } while (dataService.checkId(randomNumber));

            System.out.println("Generated Random Number: " + randomNumber);
            String hash = HashUtils.generateHash(data);

            Data newData = Data.builder()
                .id(Long.parseLong(randomNumber))
                .decoded_data(decodedData)
                .data_key(hash)
                .expiryDate(LocalDateTime.now().plusMinutes(20))
                .build();

            dataService.save(newData);
            // 응답 반환
            return randomNumber + "," + hash;
        } catch (Exception e) {
            // 에러가 발생한 경우 500 응답
            throw new RuntimeException("Internal Server Error");
        }
    }

    // 보안을 위한 post 요청
//    @PostMapping("/view")
//    public String view(@RequestBody ViewDTO viewDTO) {
//        Data foundData = dataService.findDataById(viewDTO.getId());
//        // id와 일치하는 데이터가 존재하지 않는 경우
//        if(foundData == null) {
//            throw new EntityNotFoundException("Data not found");
//        }
//
//        // hash 값이 틀린 경우
//        if(!foundData.getData_key().equals(viewDTO.getHash())) {
//            throw new EntityNotFoundException("Unable to access data");
//        }
//
//        // 만료된 경우 삭제
//        if(foundData.getExpiryDate().isBefore(LocalDateTime.now())) {
//            dataService.deleteDataById(viewDTO.getId());
//            throw new EntityNotFoundException("Data not found");
//        }
//        return foundData.getDecoded_data();
//    }

    // 보안을 개나 줘버린 get 요청
    @GetMapping("/view")
    public String view(@RequestParam String id, @RequestParam String hash) {
        Data foundData = dataService.findDataById(id);
        // id와 일치하는 데이터가 존재하지 않는 경우
        if(foundData == null) {
            throw new EntityNotFoundException("Data not found");
        }

        // hash 값이 틀린 경우
        if(!foundData.getData_key().equals(hash)) {
            throw new EntityNotFoundException("Unable to access data");
        }

        // 만료된 경우 삭제
        if(foundData.getExpiryDate().isBefore(LocalDateTime.now())) {
            dataService.deleteDataById(id);
            throw new EntityNotFoundException("Expired data");
        }
        String output = foundData.getDecoded_data().replace("\r", "\n");
        System.out.println("Found Data: " + output);
        return output;
    }
}
