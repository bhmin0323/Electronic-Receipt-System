package ssu.sdj.digital_receipt.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ssu.sdj.digital_receipt.DAO.DataRepository;
import ssu.sdj.digital_receipt.entity.Data;

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
}
