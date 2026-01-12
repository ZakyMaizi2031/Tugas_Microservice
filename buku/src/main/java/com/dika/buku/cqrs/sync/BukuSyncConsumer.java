package com.dika.buku.cqrs.sync;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dika.buku.config.RabbitMQConfig;
import com.dika.buku.cqrs.command.handler.BukuCommandHandler;
import com.dika.buku.cqrs.command.model.Buku;
import com.dika.buku.cqrs.query.model.BukuDocument;
import com.dika.buku.dto.PengembalianEvent;
import com.dika.buku.repository.BukuMongoRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BukuSyncConsumer {
    @Autowired private BukuMongoRepository mongoRepo;
    @Autowired private BukuCommandHandler commandHandler;

    @RabbitListener(queues = RabbitMQConfig.SYNC_QUEUE)
    public void processSync(Buku buku) {
        try {
            if (buku == null || buku.getId() == null) {
                log.error("SYNC_ERROR: Payload kosong atau ID null.");
                return;
            }

            BukuDocument doc = new BukuDocument();
            doc.setId(buku.getId().toString());
            doc.setJudul(buku.getJudul());
            doc.setPengarang(buku.getPengarang());
            doc.setPenerbit(buku.getPenerbit());
            doc.setTahun_terbit(buku.getTahun_terbit());
            doc.setStatus(buku.getStatus());

            mongoRepo.save(doc);
            log.info("SYNC_SUCCESS: MongoDB Buku ID {} berhasil diperbarui.", doc.getId());
        } catch (Exception e) {
            log.error("SYNC_CRASH: {}", e.getMessage(), e);
        }
    }


    @RabbitListener(queues = RabbitMQConfig.UPDATE_STATUS_QUEUE)
    public void receiveStatusUpdate(PengembalianEvent event) {
        if (event == null || event.getIdBuku() == null) {
            log.error("EXTERNAL_EVENT_ERROR: Menerima event update status tapi ID Buku NULL");
            return;
        }

        log.info("EXTERNAL_EVENT: Menerima instruksi update Buku ID {} menjadi {}", 
                 event.getIdBuku(), event.getStatus());
        
        commandHandler.updateStatus(event.getIdBuku(), event.getStatus());
    }

    @RabbitListener(queues = RabbitMQConfig.DELETE_QUEUE)
    public void processDeleteSync(Long mysqlId) {
        if (mysqlId != null) {
            mongoRepo.deleteById(String.valueOf(mysqlId));
            log.info("SYNC_DELETE: Buku ID {} dihapus dari MongoDB", mysqlId);
        }
    }
}