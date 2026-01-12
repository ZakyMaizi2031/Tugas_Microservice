package com.dika.anggota.cqrs.sync;

import com.dika.anggota.config.RabbitMQConfig;
import com.dika.anggota.cqrs.command.model.Anggota;
import com.dika.anggota.cqrs.query.model.AnggotaDocument;
import com.dika.anggota.repository.AnggotaMongoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@Slf4j
public class AnggotaSyncConsumer {

    @Autowired private AnggotaMongoRepository mongoRepo;

    // Listener untuk Sinkronisasi MySQL ke MongoDB
    @RabbitListener(queues = RabbitMQConfig.SYNC_QUEUE)
    public void receiveSync(Map<String, Object> data) {
        try {
            if (data == null || data.get("id") == null) return;
            
            AnggotaDocument doc = new AnggotaDocument();
            doc.setId(data.get("id").toString());
            doc.setNim((String) data.get("nim"));
            doc.setNama((String) data.get("nama"));
            doc.setEmail((String) data.get("email"));
            doc.setAlamat((String) data.get("alamat"));
            doc.setJenis_kelamin((String) data.get("jenis_kelamin"));

            mongoRepo.save(doc);
            log.info("SYNC SUCCESS: MongoDB Anggota ID {} sinkron.", doc.getId());
        } catch (Exception e) {
            log.error("SYNC ERROR: {}", e.getMessage());
        }
    }

    // Listener untuk Update Status (Jika ada perintah dari service lain)
    @RabbitListener(queues = RabbitMQConfig.UPDATE_STATUS_QUEUE)
    public void receiveStatusUpdate(Map<String, Object> event) {
        log.info("EXTERNAL EVENT: Menerima perintah update status anggota: {}", event);
        // Tambahkan logic update status anggota di sini jika diperlukan
    }

    @RabbitListener(queues = RabbitMQConfig.DELETE_QUEUE)
    public void processDeleteSync(Long mysqlId) {
        mongoRepo.deleteById(String.valueOf(mysqlId));
        log.info("SYNC DELETE: Anggota ID {} dihapus dari MongoDB", mysqlId);
    }
}