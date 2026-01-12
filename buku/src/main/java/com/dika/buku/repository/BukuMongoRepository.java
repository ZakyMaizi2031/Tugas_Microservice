package com.dika.buku.repository;
import com.dika.buku.cqrs.query.model.BukuDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BukuMongoRepository extends MongoRepository<BukuDocument, String> { }