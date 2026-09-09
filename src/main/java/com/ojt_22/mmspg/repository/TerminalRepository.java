package com.ojt_22.mmspg.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ojt_22.mmspg.entity.Terminal;

public interface TerminalRepository extends JpaRepository<Terminal, UUID> {

}
