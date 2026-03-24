package com.ootd.fitme.domain.clothes.repository;

import com.ootd.fitme.domain.clothes.entity.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClothesRepository extends JpaRepository<Clothes, UUID> {
}
