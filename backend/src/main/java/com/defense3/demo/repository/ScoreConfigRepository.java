package com.defense3.demo.repository;

import com.defense3.demo.entity.ScoreConfig;
import com.defense3.demo.entity.ThesisType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreConfigRepository extends JpaRepository<ScoreConfig, Long> {

    List<ScoreConfig> findByThesisTypeOrderByItemIndex(ThesisType thesisType);

    void deleteByThesisType(ThesisType thesisType);
}
