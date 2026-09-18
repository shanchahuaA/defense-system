package com.defense3.demo.repository;

import com.defense3.demo.entity.DefenseYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DefenseYearRepository extends JpaRepository<DefenseYear, Long> {

    // 返回 List 以支持多条记录的情况，避免 IncorrectResultSizeDataAccessException
    List<DefenseYear> findByIsCurrent(Boolean isCurrent);

    Optional<DefenseYear> findByName(String name);

    boolean existsByName(String name);
}
