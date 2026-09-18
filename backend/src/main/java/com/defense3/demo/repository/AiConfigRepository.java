package com.defense3.demo.repository;

import com.defense3.demo.entity.AiConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiConfigRepository extends JpaRepository<AiConfig, Long> {
    AiConfig findTopByOrderByIdAsc();
}
