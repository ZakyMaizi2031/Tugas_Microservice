package com.dika.anggota.repository;
import com.dika.anggota.cqrs.command.model.Anggota;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnggotaJpaRepository extends JpaRepository<Anggota, Long> { }