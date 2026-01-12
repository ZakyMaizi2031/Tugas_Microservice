package com.dika.buku.repository;
import com.dika.buku.cqrs.command.model.Buku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BukuJpaRepository extends JpaRepository<Buku, Long> { }