# Laporan UAS Arsitektur Berbasis Layanan
## Implementasi Arsitektur Microservices Modern dengan CQRS Hybrid, Event-Driven Architecture, dan Automated Deployment

**Nama** : Dika Jefrianto  
**Kelas** : TRPL 3D  
**Mata Kuliah** : Arsitektur Berbasis Layanan  

---

## 1. Pendahuluan

Proyek ini merupakan implementasi **arsitektur Microservices** pada sistem **Manajemen Perpustakaan** yang dirancang untuk mengatasi permasalahan skalabilitas, performa, dan maintainability pada sistem terdistribusi. Pendekatan utama yang digunakan adalah **Command Query Responsibility Segregation (CQRS)** dengan pemisahan jalur tulis dan jalur baca data.

Sistem menerapkan konsep **Hybrid Database**, di mana **MySQL** digunakan sebagai database transaksi (*Write Side*) dan **MongoDB** digunakan sebagai database pembacaan (*Read Side*). Sinkronisasi data dilakukan secara asinkron melalui **Event-Driven Architecture** menggunakan **RabbitMQ**, serta seluruh layanan dikelola dan dideploy menggunakan **Kubernetes** sebagai platform orkestrasi kontainer.

---

## 2. Teknologi yang Digunakan

| Kategori | Teknologi | Deskripsi |
|--------|----------|----------|
| Backend Framework | Spring Boot 3.x | Framework utama berbasis Java 17 |
| Message Broker | RabbitMQ | Media komunikasi asinkron dan sinkronisasi event |
| Write Database | MySQL 8.0 | Menjamin konsistensi dan integritas data transaksi |
| Read Database | MongoDB | Optimasi query baca dengan struktur fleksibel |
| Service Discovery | Netflix Eureka | Registrasi dan penemuan layanan secara dinamis |
| API Gateway | Spring Cloud Gateway | Routing dan single entry point API |
| Orchestration | Kubernetes | Manajemen deployment dan resource |
| Logging | ELK Stack | Logging terpusat lintas layanan |
| Monitoring | Prometheus & Grafana | Monitoring performa dan resource |
| CI/CD | Jenkins | Otomatisasi build dan deployment |

---

## 3. Arsitektur Sistem dan Implementasi

### 3.1 Penerapan CQRS Hybrid

Pola CQRS diterapkan tidak hanya pada level kode, tetapi juga pada pemisahan database fisik untuk meningkatkan efisiensi sistem.

- **Command Side (Write Side)**  
  Seluruh operasi Create, Update, dan Delete diproses oleh Command Handler dan disimpan ke database MySQL.

- **Event Publication**  
  Setelah data berhasil dipersistenkan, sistem mengirimkan event ke RabbitMQ dalam format `Map<String, Object>` untuk menghindari permasalahan serialisasi Hibernate Proxy.

- **Query Side (Read Side)**  
  Sync Consumer mendengarkan event dari RabbitMQ dan memproyeksikan data ke MongoDB sebagai *read model*.

Pendekatan ini memungkinkan proses pembacaan data berjalan lebih cepat karena tidak membebani database transaksi utama.

---

### 3.2 Komunikasi Event-Driven Antar Layanan

Setiap layanan berkomunikasi menggunakan mekanisme event sehingga memiliki keterikatan rendah (*loosely coupled*).

- **Proses Peminjaman**  
  Service Peminjaman mengirim event setelah transaksi tersimpan. Service Buku menerima event tersebut dan memperbarui status buku menjadi *Dipinjam*.

- **Proses Pengembalian**  
  Service Pengembalian menangani proses bisnis utama seperti perhitungan denda dan notifikasi email. Event yang dihasilkan digunakan oleh:
  - Service Peminjaman untuk mengubah status transaksi menjadi *SELESAI*
  - Service Buku untuk mengubah status buku menjadi *Tersedia*

---

## 4. Observability Sistem

### 4.1 Logging Terpusat (ELK Stack)

Sistem logging terpusat diimplementasikan menggunakan **ELK Stack** dengan mekanisme sebagai berikut:

- Log dikirim ke Logstash melalui TCP port 5000
- Setiap layanan menggunakan konfigurasi `logback-spring.xml`
- Penambahan field `app_name` untuk memudahkan filtering log di Kibana

Pendekatan ini mempermudah proses debugging dan analisis kesalahan sistem.

---

### 4.2 Monitoring Performa (Prometheus dan Grafana)

Monitoring performa aplikasi dilakukan dengan:

- Micrometer Prometheus melalui endpoint `/actuator/prometheus`
- Akses NodePort Kubernetes agar Prometheus eksternal dapat melakukan scraping
- Visualisasi metrik menggunakan Grafana Dashboard (ID: 11378)

Metrik yang dipantau meliputi penggunaan CPU, memory, dan latency HTTP request.

---

## 5. Deployment dan Optimasi Resource

### 5.1 Deployment di Kubernetes

Deployment dilakukan menggunakan file manifest Kubernetes (YAML) yang mencakup:

- Namespace untuk isolasi resource
- Secrets untuk penyimpanan kredensial
- Konfigurasi environment melalui `SPRING_APPLICATION_JSON`

---

### 5.2 Strategi Optimasi RAM

Untuk menyesuaikan dengan keterbatasan resource atau RAM Device yang Terbatas, dilakukan optimasi sebagai berikut:

- Pembatasan heap memory JVM (`-Xmx256M` dan `-Xms128M`)
- Batas maksimal memory Pod sebesar `384Mi`
- Pemisahan infrastruktur monitoring dan database menggunakan Docker Compose

---

## 6. CI/CD Pipeline Menggunakan Jenkins

Pipeline CI/CD diimplementasikan menggunakan **Jenkins Native** dengan tahapan:

1. Build aplikasi menggunakan Maven  
2. Build dan push Docker Image ke Docker Hub  
3. Deployment otomatis ke Kubernetes menggunakan mekanisme *rolling update*  

Pendekatan ini memastikan proses pengembangan dan deployment berjalan konsisten dan minim downtime.

---

## 7. Akses Dashboard Sistem

| Komponen | URL |
|--------|-----|
| API Gateway | http://localhost:30000 |
| Eureka Server | http://localhost:8761 |
| Kibana | http://localhost:5601 |
| Grafana | http://localhost:3000 |
| RabbitMQ | http://localhost:15672 |
| phpMyAdmin | http://localhost:32000 |
| Mongo Express | http://localhost:8085 |

---

## 8. Kesimpulan

Sistem Manajemen Perpustakaan ini berhasil mengimplementasikan **arsitektur Microservices modern** dengan pendekatan **CQRS Hybrid dan Event-Driven Architecture**. Pemisahan database meningkatkan performa pembacaan data tanpa mengorbankan integritas transaksi.

Dukungan **Kubernetes**, **CI/CD Jenkins**, serta **Logging dan Monitoring terpusat** menjadikan sistem ini siap dikembangkan lebih lanjut dan dioperasikan dalam lingkungan produksi yang terdistribusi.

---
