package com.zaky.peminjaman.cqrs.event;

public class PeminjamanCreatedEvent {
    private Long anggotaId;
    private Long bukuId;

    public PeminjamanCreatedEvent(Long anggotaId, Long bukuId) {
        this.anggotaId = anggotaId;
        this.bukuId = bukuId;
    }

    public Long getAnggotaId() { return anggotaId; }
    public Long getBukuId() { return bukuId; }
}
