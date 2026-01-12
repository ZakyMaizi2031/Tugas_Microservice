package com.dika.anggota.repository;
import com.dika.anggota.cqrs.query.model.AnggotaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnggotaMongoRepository extends MongoRepository<AnggotaDocument, String> { }