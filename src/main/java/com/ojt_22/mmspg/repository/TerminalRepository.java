package com.ojt_22.mmspg.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ojt_22.mmspg.entity.Terminal;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, UUID> {
    long countByStatus(String status);
    
    @Query("SELECT t FROM Terminal t JOIN FETCH t.merchant m JOIN FETCH t.branch b ORDER BY t.createdAt DESC")
    List<Terminal> findAllWithDetails();
    
    boolean existsByTerminalCode(String terminalCode);
}
