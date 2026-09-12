package com.ojt_22.mmspg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ojt_22.mmspg.entity.MccCode;

@Repository
public interface MccCodeRepository extends JpaRepository<MccCode, Integer> {
}
