package com.zaky.buku.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zaky.buku.model.Buku;

@Repository
public interface BukuRepository extends JpaRepository<Buku, Long> {

}