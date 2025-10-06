package com.example.demo.Repository;

import com.example.demo.entity.BasicDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BasicDetailRepository extends JpaRepository<BasicDetails,Integer> {
    Optional<BasicDetails> findByEmployeeId(UUID employeeId);
}
