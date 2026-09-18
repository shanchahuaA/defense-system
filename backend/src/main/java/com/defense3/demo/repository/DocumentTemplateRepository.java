package com.defense3.demo.repository;

import com.defense3.demo.entity.DocumentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

    Optional<DocumentTemplate> findByTemplateType(String templateType);

    boolean existsByTemplateType(String templateType);
}
