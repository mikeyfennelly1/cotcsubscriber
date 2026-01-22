package org.example.repository;

import org.example.model.SysinfoMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysinfoMessageRepository extends JpaRepository<SysinfoMessage, Long> {
}
