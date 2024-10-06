package ssu.sdj.digital_receipt.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ssu.sdj.digital_receipt.DAO.DataRepository;
import ssu.sdj.digital_receipt.entity.Data;
import ssu.sdj.digital_receipt.utils.HashUtils;

import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class DataService {
    private final DataRepository dataRepository;

    public DataService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @Transactional
    public void save(Data data) {
        dataRepository.save(data);
    }

    public boolean checkId(String id) {
        return dataRepository.findDataById(Long.parseLong(id)) != null;
    }

    public Data findDataById(String id) {
        return dataRepository.findDataById(Long.parseLong(id));
    }

    public void deleteDataById(String id) {
        dataRepository.deleteById(Long.parseLong(id));
    }

    public String getData(String id, String hash) throws Exception {
        Data foundData = findDataById(id);
        // id와 일치하는 데이터가 존재하지 않는 경우
        if(foundData == null) {
            throw new EntityNotFoundException("Data not found");
        }

        // hash 값이 틀린 경우
        byte[] IV = Base64.getDecoder().decode(foundData.getIV());
        if(!foundData.getCheck_val().equals(HashUtils.AES_Encrypt("check", HashUtils.digestHash(hash), IV))) {
            throw new EntityNotFoundException("Unable to access data");
        }

        // 만료된 경우 삭제
        if(foundData.getExpiryDate().isBefore(LocalDateTime.now())) {
            deleteDataById(id);
            throw new EntityNotFoundException("Expired data");
        }

        String output = HashUtils.AES_Decrypt(foundData.getEncrypted_data(), hash, foundData.getIV());
        System.out.println("Found Data: " + output);
        return output;
    }

    @Transactional
    public String saveData(String data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Bad Request: Missing data parameter");
        }

        String decodedData;
        String randomNumber;

        try {
            // '+' 문자를 공백으로 변환하지 않도록 처리
            data = data.replace(" ", "+");

            // Base64 디코딩하여 출력
            decodedData = HashUtils.base64DecodeToCP949(data);
            decodedData = decodedData.replace("\r\n", "\n").replace("\r", "\n");
            System.out.println("Decoded Data: " + decodedData);

            // 8자리 랜덤 숫자 생성
            do {
                randomNumber = HashUtils.generateRandomNumber();
            } while (checkId(randomNumber));

            System.out.println("Generated Random Number: " + randomNumber);
            String userKey = HashUtils.generateBase64(data);
            byte[] hash = HashUtils.digestHash(userKey);
            byte[] IV = HashUtils.generateIV();

            String encryptedData = HashUtils.AES_Encrypt(decodedData, hash, IV);

            Data newData = Data.builder()
                    .id(Long.parseLong(randomNumber))
                    .encrypted_data(encryptedData)
                    .IV(Base64.getEncoder().encodeToString(IV))
                    .check_val(HashUtils.AES_Encrypt("check", hash, IV))
                    .expiryDate(LocalDateTime.now().plusMinutes(20))
                    .build();

            save(newData);
            // 응답 반환
            return randomNumber + "," + userKey;
        } catch (Exception e) {
            // 에러가 발생한 경우 500 응답
            throw new RuntimeException("Internal Server Error");
        }
    }
}
