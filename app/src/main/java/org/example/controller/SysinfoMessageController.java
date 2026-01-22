package org.example.controller;

import org.example.repository.SysinfoMessageRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sysinfo")
public class SysinfoMessageController {

    private final SysinfoMessageRepository sysinfoMessageRepository;

    public SysinfoMessageController(SysinfoMessageRepository sysinfoMessageRepository) {
        this.sysinfoMessageRepository = sysinfoMessageRepository;
    }

    @GetMapping("/mac-addresses")
    public List<String> getDistinctMacAddresses() {
        return sysinfoMessageRepository.findDistinctMacAddresses();
    }
}
