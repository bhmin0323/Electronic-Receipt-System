package ssu.sdj.digital_receipt.tasks;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ssu.sdj.digital_receipt.DAO.DataRepository;

import java.time.LocalDateTime;

@Component
public class ScheduledTasks {
    @Autowired
    private DataRepository dataRepository;

    @Scheduled(fixedRate = 20000) // 20초마다 실행
    @Transactional
    public void deleteExpiredData() {
        dataRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
