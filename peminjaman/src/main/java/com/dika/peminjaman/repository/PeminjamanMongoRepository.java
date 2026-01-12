package com.dika.peminjaman.repository;
import com.dika.peminjaman.cqrs.query.model.PeminjamanDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface PeminjamanMongoRepository extends MongoRepository<PeminjamanDocument, String> {}