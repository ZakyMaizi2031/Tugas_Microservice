package com.dika.peminjaman.repository;
import com.dika.peminjaman.cqrs.command.model.Peminjaman;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PeminjamanJpaRepository extends JpaRepository<Peminjaman, Long> {}