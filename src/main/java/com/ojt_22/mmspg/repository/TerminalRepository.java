package com.ojt_22.mmspg.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ojt_22.mmspg.entity.Terminal;
import com.ojt_22.mmspg.enums.TerminalStatus;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, UUID> {
	long countByStatus(TerminalStatus status);

	@Query("SELECT t FROM Terminal t JOIN FETCH t.branch b JOIN FETCH b.merchant m ")
	List<Terminal> findAllWithDetails();

	boolean existsByTerminalCode(String terminalCode);
}
