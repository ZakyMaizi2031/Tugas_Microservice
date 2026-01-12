package com.dika.pengembalian_service.repository;
import com.dika.pengembalian_service.cqrs.query.model.PengembalianDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface PengembalianMongoRepository extends MongoRepository<PengembalianDocument, String> {}