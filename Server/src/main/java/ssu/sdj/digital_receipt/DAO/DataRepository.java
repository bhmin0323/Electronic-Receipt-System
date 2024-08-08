package ssu.sdj.digital_receipt.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ssu.sdj.digital_receipt.entity.Data;

import java.time.LocalDateTime;

@Repository
public interface DataRepository extends JpaRepository<Data, Long> {
    Data findDataById(Long id);
    void deleteByExpiryDateBefore(LocalDateTime now);
}
