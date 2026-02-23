package org.example.service;

import org.example.model.source.SourceCategory;
import org.example.model.source.device.SysinfoMessage;
import org.example.repository.SysinfoMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final SysinfoMessageRepository sysinfoMessageRepository;

    @Autowired
    public CategoryService(SysinfoMessageRepository sysinfoMessageRepository) {
        this.sysinfoMessageRepository = sysinfoMessageRepository;
    }

    public List<String> getDeviceTypes() {
        return List.of("device");
    }

    public List<String> getSourcesByCategory(SourceCategory category, String subtype) {
        if (category == SourceCategory.device) {
            return sysinfoMessageRepository.findDistinctMacAddresses();
        }
        return List.of();
    }

    public List<SysinfoMessage> getSysinfoReport(String deviceId, Long startDate, Long endDate) {
        return sysinfoMessageRepository.findByMacAddressAndReadTimeBetween(deviceId, startDate, endDate);
    }
}
