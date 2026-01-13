package com.example.portfolio.repositories;

import com.example.portfolio.models.PlaidItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaidItemRepository extends JpaRepository<PlaidItem, Integer> {
    List<PlaidItem> findByOwner_Id(Integer userId);
}