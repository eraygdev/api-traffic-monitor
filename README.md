# 🚀 API Traffic Monitor & Rate Limiter

[![Java](https://img.shields.io/badge/Java-21-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.x-brightgreen.svg)]()
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Docker-blue.svg)]()

Modern ve ölçeklenebilir bir backend altyapısı ile API trafik yönetimi ve güvenlik denetimi.

---

## 🎯 Proje Amacı
Sistemin kötüye kullanımını engellemek, API trafiğini anlık olarak izlemek ve **Rate Limiting** (istek sınırlama) mekanizması ile sunucuyu aşırı yükten koruyan sağlam bir backend sistemi geliştirmek.

---

## 🛠️ Teknoloji Yığını (Tech Stack)
*   **Java 21 (LTS)** - Modern dil özellikleri ve performans.
*   **Spring Boot 3.5.x** - Hızlı ve güvenli uygulama geliştirme.
*   **PostgreSQL** - Veri kalıcılığı için güvenilir veritabanı.
*   **Docker** - Konteynerize edilmiş veritabanı ve uygulama ortamı.
*   **Lombok** - Temiz ve boilerplate içermeyen kod yapısı.
*   **Spring Data JPA** - Nesne odaklı veritabanı yönetimi.

---

## 🏗️ Mimari Akış
1.  **Request Layer:** Kullanıcıdan gelen HTTP isteği yakalanır.
2.  **Interceptor Layer:** `RateLimitInterceptor` ile istemcinin IP'si ve istek sıklığı analiz edilir.
3.  **Persistence Layer:** İzin verilen istekler PostgreSQL veritabanına loglanır.
4.  **Response Layer:**
    *   ✅ **İzin verilirse:** İstek işlenir.
    *   ❌ **Limit aşılırsa:** `429 Too Many Requests` hatası döndürülür.

---

## 📚 Teknik Terimler Sözlüğü
| Terim | Açıklama |
| :--- | :--- |
| **Request Interceptor** | İstekler Controller'a ulaşmadan önce araya giren yapı. |
| **Rate Limiting** | Belirli sürede maksimum istek sınırı belirleme. |
| **HTTP 429** | İstek limitinin aşıldığını belirten hata kodu. |
| **Middleware** | İstek/yanıt döngüsünü yöneten ara katman. |
| **Concurrency** | Eşzamanlı istek yönetimi. |

---

## 📋 Yapılacaklar (Roadmap)
- [ ] `docker-compose.yml` kurulumu ve veritabanı bağlantısı.
- [ ] `RateLimitInterceptor` sınıfının tasarlanması.
- [ ] PostgreSQL `request_logs` tablosunun modellenmesi.
- [ ] İstek sınırlama mantığının (limit logic) kodlanması.
- [ ] API istatistikleri için `/api/stats` endpoint'inin yazılması.

---

## 👤 İletişim
**Eray Günlü** - [GitHub Profili:](https://github.com/eraygdev)

*Bu proje, backend geliştirme becerilerimi geliştirmek amacıyla oluşturulmuştur.*