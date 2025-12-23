package com.zaky.anggota.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zaky.anggota.model.Anggota;
import com.zaky.anggota.repository.AnggotaRepository;

@Service
public class AnggotaService {
    @Autowired
    private AnggotaRepository anggotaRepository;

    public List<Anggota> getAllAnggota(){
        return anggotaRepository.findAll();
    }
    public Anggota getAnggotaById(Long id){
        return anggotaRepository.findById(id).orElse(null);
    }
    public Anggota createAnggota(Anggota anggota){
        return anggotaRepository.save(anggota);
    }
    public void deleteAnggota (Long id){
        anggotaRepository.deleteById(id);
    }
}

