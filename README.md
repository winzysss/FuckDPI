<div align="center">
  <p>
    <img src="https://github.com/winzysss/FuckDPI/raw/master/.github/images/app.png" alt="FuckDPI logosu" width="200" />
  </p>
  <h1>FuckDPI</h1>
  <p><em>Özgür internete açılan kanat — Android için DPI aşımı aracı</em></p>
  <p>
    Türkçe |
    <a href="README-en.md">English</a>
  </p>
  <p>
    <a href="https://github.com/winzysss/FuckDPI/releases/latest"><img src="https://img.shields.io/github/v/release/winzysss/FuckDPI" alt="Son Sürüm" /></a>
    <a href="https://github.com/winzysss/FuckDPI/releases"><img src="https://img.shields.io/github/downloads/winzysss/FuckDPI/total" alt="İndirme Sayısı" /></a>
    <a href="https://github.com/winzysss/FuckDPI/blob/master/LICENSE"><img src="https://img.shields.io/github/license/winzysss/FuckDPI" alt="Lisans" /></a>
    <a href="https://github.com/winzysss/FuckDPI"><img src="https://img.shields.io/github/languages/code-size/winzysss/FuckDPI" alt="Kod boyutu"/></a>
  </p>
</div>

FuckDPI, Android cihazlarda **DPI (Derin Paket İnceleme) tabanlı engellemeleri aşmak** için geliştirilmiş, tamamen açık kaynak ve **eğitim amaçlı** bir uygulamadır. Cihazınızda yerel bir desenkronizasyon motoru çalıştırır ve tüm trafiği onun üzerinden geçirir — uzak sunucu yok, hesap yok, kayıt yok.

> **Bu proje eğitim ve araştırma amacıyla geliştirilmiştir.** Kullanımdan doğabilecek tüm sorumluluk kullanıcıya aittir. Ayrıntılar için [Yasal Uyarı](#yasal-uyarı) bölümüne bakın.

---

## Nasıl çalışır?

```
Uygulamaların trafiği → [Android VpnService / TUN] → Cihaz içindeki yerel tünel
→ DPI desync motoru (paketleri parçalar / zamanlar) → Hedef siteye doğrudan
```

- **Bu uygulama klasik bir VPN değildir.** Trafiğiniz cihazınızdan çıkıp üçüncü parti bir sunucuya gitmez; tünel tamamen cihazınızın içinde çalışır.
- Trafiği **şifrelemez** ve **IP adresinizi gizlemez** — amacı gizlilik değil, DPI manipülasyonudur.
- Motor açık kaynaklıdır; kullanılan teknikler paket parçalama, sahte paketler ve TTL manipülasyonu gibi yaygın desenkronizasyon yöntemleridir.

## Neden VPN izni istiyor?

Android'de bir uygulamanın diğer uygulamaların internet trafiğini yakalayabilmesinin tek yasal yolu `VpnService` API'sidir. İzin ekranındaki "ağ trafiğini izleyebilir" uyarısı Android'in standart metnidir — trafik uygulamadan geçer ama **cihazınızdan dışarı çıkmaz**. İzin vermek istemezseniz Ayarlar → Gelişmiş → Mod'dan **Proxy modu**nu seçebilirsiniz (trafiği kendiniz yönlendirmeniz gerekir).

## Akıllı Mod — tek tuşla çalışır

Hiçbir ayar bilmenize gerek yok:

1. Uygulamayı açın, ortadaki büyük butona basın.
2. İlk seferde uygulama arka planda **hızlı bir test** yapar: 12 küratörlü stratejiyi gerçek sitelere karşı dener, yüksek başarılı birini bulduğunda durur.
3. En iyi strateji otomatik seçilir ve bağlanırsınız. **Seçim yok, ayar yok.**
4. Operatörünüz veya ağınız değişirse, strateji menüsünden "Otomatik"i seçerek testi yeniden çalıştırabilirsiniz.

## Özellikler

- **Tek tuşla bağlanma** — sadeleştirilmiş ana ekran, teknik detay yok
- **Akıllı mod** — ağınızda gerçekten çalışan stratejiyi kendisi bulur
- **Hazır stratejiler** — ilk açılışta strateji listenize sabitlenir
- **VPN ve Proxy modları** — tüm trafiği yakala ya da bağımsız SOCKS5 proxy çalıştır
- **Uygulama bazında filtreleme** — hangi uygulamaların tünelden geçeceğini seçin
- **Otomatik başlatma** — cihaz açılınca hizmeti kendiliğinden başlat
- **Uygulama içi güncelleme** — yeni sürümleri GitHub Releases üzerinden denetler
- **Çoklu dil** — Türkçe, İngilizce, Rusça, Kazakça, Vietnamca
- **Android TV/BOX uyumlu** — Quick Settings kutucuğu dahil
- **Gelişmiş ayarlar** — DNS, IPv6, strateji editörü, test listeleri (hepsi Gelişmiş altında)

## Kurulum

1. [Releases](https://github.com/winzysss/FuckDPI/releases/latest) sayfasını açın.
2. Cihazınıza uygun APK'yı indirin:

| Dosya | Hangi cihaz |
|---|---|
| `*-arm64-v8a-release.apk` | Çoğu modern telefon (önerilen) |
| `*-armeabi-v7a-release.apk` | Eski 32-bit cihazlar |
| `*-x86-release.apk` / `*-x86_64-release.apk` | Emülatör / x86 tablet |
| `*-universal-release.apk` | Emin değilseniz bunu indirin |

3. Android'in "bilinmeyen kaynaklar" uyarısını onaylayıp APK'yı kurun.
4. Açın, butona basın, VPN iznini verin — gerisini Akıllı Mod halleder.

İndirdiğiniz dosyayı doğrulamak için release'deki `checksums.txt` ile SHA-256 özetini karşılaştırabilirsiniz.

## Hangi ülkelerde çalışır?

Teknikler ülkeye bağımlı değildir — Akıllı Mod **bulunduğunuz ağda** test eder, o yüzden Türkiye dışında da çalışabilir. Ancak dürüst olalım:

- Hiçbir strateji her yerde çalışmaz; İSS, bölge ve zamana göre değişir.
- Aktif probing ve IP engelleme yapan sistemlerde (ör. Çin'in Büyük Güvenlik Duvarı) tek başına yeterli olmayabilir — bu, bu sınıftaki tüm araçların sınırıdır.
- Hazır strateji isimleri Türkiye odaklıdır ama herkes kullanabilir; test listesini kendi ülkenize göre özelleştirebilirsiniz (Ayarlar → Gelişmiş → Proxy testi → ⚙ → Alan Adı Listeleri).

## Derleme

Gereksinimler: JDK 17 · Android SDK 36 (build-tools `36.0.0`) · NDK `27.2.12479018` · CMake 3.22.1

```bash
git clone --recurse-submodules https://github.com/winzysss/FuckDPI.git
cd FuckDPI
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/`

> Yerel tünel bileşeni Windows'ta doğrudan derlenemez — WSL kullanın ya da CI'ın ürettiği APK'ları indirin. İmzalı release derlemeleri GitHub Actions ile yapılır (`.github/workflows/release.yml`): bir `v*` tag'i itin, gerisini CI halleder.

## Proje yapısı

- `app/src/main/java/...` — Kotlin kaynak kodu (VpnService, akıllı mod, arayüz)
- `app/src/main/cpp/core` — DPI desync motoru (C, MIT lisanslı üçüncü parti bileşen)
- `app/src/main/jni/tunnel` — TUN↔SOCKS5 tünel katmanı
- `fastlane/` — mağaza metadata'sı · `.github/workflows/` — release CI

## Uyarılar

- Bu **gizlilik aracı değildir**: şifrelemez, sunucu üzerinden tünellemez, IP gizlemez. Anonimlik istiyorsanız gerçek VPN veya Tor kullanın.
- Etkinliği İSS'nize ve ağınıza bağlıdır; garanti edilmez.
- Uygulama **yalnızca bu GitHub deposundan** dağıtılır — başka kaynaklardan inen APK'lara güvenmeyin.

## Yasal Uyarı

Bu proje eğitim ve araştırma amacıyla geliştirilmiş açık kaynak bir yazılımdır. DPI tabanlı engellemeleri aşmak bazı ülkelerde yasalara veya internet servis sağlayıcınızın kullanım koşullarına aykırı olabilir. Yazılımı kullanmakla doğabilecek tüm sonuçların sorumluluğu kullanıcıya aittir; geliştirici hiçbir sorumluluk kabul etmez. Bu uyarı bilgilendirme amaçlıdır ve [LICENSE](LICENSE) dosyasındaki GNU GPL v3 lisansının tanıdığı hakları sınırlamaz.

## Lisans

Bu proje [GNU General Public License v3.0](LICENSE) ile lisanslanmıştır — tamamen açık kaynak. Üçüncü parti bileşenler MIT/BSD lisanslarıyla gelir; ayrıntılar için [NOTICE](NOTICE) dosyasına bakın.

## Katkı

Hata raporları ve özellik istekleri için [Issues](https://github.com/winzysss/FuckDPI/issues), genel sorular için [Discussions](https://github.com/winzysss/FuckDPI/discussions). Pull request'ler memnuniyetle karşılanır.
