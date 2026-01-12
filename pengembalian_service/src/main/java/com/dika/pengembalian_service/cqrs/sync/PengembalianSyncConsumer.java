package com.dika.pengembalian_service.cqrs.sync;

import com.dika.pengembalian_service.cqrs.query.model.PengembalianDocument;
import com.dika.pengembalian_service.repository.PengembalianMongoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@Slf4j
public class PengembalianSyncConsumer {

    @Autowired private PengembalianMongoRepository mongoRepo;

    @RabbitListener(queues = "q.pengembalian.sync")
    public void processSync(Map<String, Object> data) {
        if (data == null || data.get("id") == null) return;

        PengembalianDocument doc = new PengembalianDocument();
        doc.setId(data.get("id").toString());
        doc.setPeminjamanId(Long.valueOf(data.get("peminjamanId").toString()));
        doc.setTanggal_dikembalikan(data.get("tanggal_dikembalikan").toString());
        doc.setTerlambat(data.get("terlambat").toString());
        doc.setDenda(Double.valueOf(data.get("denda").toString()));

        mongoRepo.save(doc);
        log.info("SYNC SUCCESS: MongoDB Pengembalian ID {} updated.", doc.getId());
    }

    @RabbitListener(queues = "q.pengembalian.delete")
    public void processDelete(Long id) {
        mongoRepo.deleteById(String.valueOf(id));
        log.info("SYNC DELETE: MongoDB Pengembalian ID {} deleted.", id);
    }
}