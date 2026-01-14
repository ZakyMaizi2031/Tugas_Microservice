package com.zaky.anggota.repository;
import com.zaky.anggota.cqrs.command.model.Anggota;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnggotaJpaRepository extends JpaRepository<Anggota, Long> { }