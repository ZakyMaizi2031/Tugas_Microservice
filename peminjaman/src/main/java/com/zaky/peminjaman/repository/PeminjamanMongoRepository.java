package com.zaky.peminjaman.repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.zaky.peminjaman.cqrs.query.model.PeminjamanDocument;
public interface PeminjamanMongoRepository extends MongoRepository<PeminjamanDocument, String> {}