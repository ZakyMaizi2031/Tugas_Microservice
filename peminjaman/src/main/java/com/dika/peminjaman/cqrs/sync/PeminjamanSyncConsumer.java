package com.dika.peminjaman.cqrs.sync;

import com.dika.peminjaman.cqrs.command.model.Peminjaman;
import com.dika.peminjaman.cqrs.query.model.PeminjamanDocument;
import com.dika.peminjaman.repository.PeminjamanMongoRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PeminjamanSyncConsumer {
    @Autowired private PeminjamanMongoRepository mongoRepo;

    // Menangani Sinkronisasi Create/Update
    @RabbitListener(queues = "q.peminjaman.sync")
    public void processSync(Peminjaman mysqlData) {
        PeminjamanDocument doc = new PeminjamanDocument();
        doc.setId(String.valueOf(mysqlData.getId()));
        doc.setAnggotaId(mysqlData.getAnggotaId());
        doc.setBukuId(mysqlData.getBukuId());
        doc.setTanggalPinjam(mysqlData.getTanggalPinjam());
        doc.setStatus(mysqlData.getStatus());
        mongoRepo.save(doc);
    }

    // Menangani Sinkronisasi Delete
    @RabbitListener(queues = "q.peminjaman.delete")
    public void processDeleteSync(Long mysqlId) {
        mongoRepo.deleteById(String.valueOf(mysqlId));
    }
}