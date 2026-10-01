package com.example.servicebus_receiver.repository;

import com.example.servicebus_receiver.model.BasicDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BasicDetailsRepository extends JpaRepository<BasicDetails, UUID> {
}
