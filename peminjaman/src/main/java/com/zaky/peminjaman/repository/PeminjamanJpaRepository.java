package com.zaky.peminjaman.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.zaky.peminjaman.cqrs.command.model.Peminjaman;
public interface PeminjamanJpaRepository extends JpaRepository<Peminjaman, Long> {}