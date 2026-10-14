# FuckDPI — Kurulum ve Kullanım Kılavuzu (Türkçe)

Bu belge, **FuckDPI** Android uygulamasının (`io.github.winzysss.fuckdpi`, GitHub: [winzysss/FuckDPI](https://github.com/winzysss/FuckDPI)) kurulumunu ve temel kullanımını anlatır. Uygulama, yerel DPI-desync motorunu (sürüm 0.17.3) cihazınızda çalıştırarak DPI (Derin Paket İnceleme) tabanlı engelleme ve yavaşlatmaları aşar.

> **Önemli:** FuckDPI bir VPN değildir. Trafiği yönlendirmek için Android'in VPN arayüzünü kullanır ama verileriniz uzak bir sunucuya gönderilmez, şifrelenmez ve IP adresiniz gizlenmez. Ayrıntılar için `SSS-tr.md` dosyasına bakın.

- **Minimum Android sürümü:** 5.0 (API 21)
- **Dağıtım:** Yalnızca GitHub Releases üzerinden APK olarak

---

## 1. APK'yı indirme

1. Telefonun tarayıcısından Releases sayfasını açın:
   **https://github.com/winzysss/FuckDPI/releases**
2. En üstteki (en yeni) sürümün **Assets** bölümünü açın.
3. Cihazınıza uygun APK'yı indirin. Dosya adları şu biçimdedir:
   `FuckDPI-<sürüm>-<mimari>-release.apk`

   | Dosya adındaki mimari | Kimin için |
   |---|---|
   | `arm64-v8a` | Neredeyse tüm modern telefonlar (önerilen) |
   | `armeabi-v7a` | Eski 32-bit cihazlar |
   | `x86_64` / `x86` | Intel işlemcili cihazlar / emülatör |
   | `universal` | Emin olamıyorsanız bunu seçin (tüm mimariler, daha büyük dosya) |

4. (İsteğe bağlı) `checksums.txt` dosyasındaki SHA-256 özeti ile indirdiğiniz APK'yı karşılaştırarak bütünlüğü doğrulayabilirsiniz.

## 2. "Bilinmeyen kaynaklardan yükleme" izni

APK, Google Play dışından yüklendiği için Android izin ister:

1. İndirilen `.apk` dosyasına dokunun (İndirilenler / Dosyalar uygulamasından).
2. **"Bilinmeyen kaynaklardan yükleme"** tarzı bir uyarı çıkarsa **Ayarlar**'a gidin ve kullandığınız tarayıcı/dosya yöneticisi için **"Bu kaynaktan yüklemeye izin ver"** anahtarını açın.
   - Yol genellikle: *Ayarlar → Uygulamalar → Özel uygulama erişimi → Bilinmeyen uygulamaları yükle* (üreticiye göre değişir; Samsung'da *Biyometrik veriler ve güvenlik → Bilinmeyen uygulamaları yükle*).
3. Geri dönüp APK'ya tekrar dokunun → **Yükle**.

> Uygulama içi güncellemede de aynı izin gerekirse FuckDPI sizi doğrudan ilgili ayar ekranına yönlendirir (bkz. Bölüm 8).

## 3. İlk açılış

Uygulamayı ilk açtığınızda sırayla şunlar olur:

1. **Bildirim izni** (Android 13+): Servis çalışırken "VPN çalışıyor" bildirimini gösterebilmek için istenir. Verilmezse uygulama yine çalışır ama bildirim görünmez.
2. **Pil optimizasyonu istisnası**: Bildirim izni tamamlandıktan sonra sistem, "FuckDPI pil optimizasyonundan muaf tutulsun mu?" diye sorar. Servisin arka planda sistem tarafından kapatılmaması için **izin vermeniz önerilir**. Bu diyalog yalnızca bir kez otomatik gösterilir; daha sonra *Ayarlar → İzinler → Pil optimizasyonu* üzerinden tekrar açabilirsiniz.
3. **Hazır stratejiler yüklenir**: İlk açılışta Türkiye odaklı hazır stratejiler (TR presetleri) komut geçmişine sabitlenmiş olarak eklenir (bkz. Bölüm 5).
4. **Güncelleme kontrolü**: Açılışta GitHub'da yeni sürüm olup olmadığı kontrol edilir (varsayılan olarak açık, bkz. Bölüm 8).

## 4. Bağlanma ve VPN izni diyaloğu

1. Ana ekrandaki büyük yuvarlak **güç düğmesine** dokunun.
2. **İlk bağlanışta Akıllı Mod devreye girer** (varsayılan): uygulama arka planda hızlı bir test yapar — küratörlü stratejileri gerçek sitelere karşı dener, yüksek başarılı birini bulunca durur ve o stratejiyle bağlanır. Ekranda "Kontrol ediliyor x/12" ilerlemesi görünür; düğmeye tekrar basarak iptal edebilirsiniz. Test bitince bulunan strateji kaydedilir ve sonraki bağlanışlar anında olur.
   - Akıllı Mod'u *Ayarlar → Otomasyon → Akıllı mod* anahtarından kapatabilirsiniz.
   - Ağınız değişirse strateji menüsünden **"Otomatik"** öğesini seçerek testi yeniden çalıştırabilirsiniz.
3. İlk bağlantıda Android'in standart **"Bağlantı isteği"** (VPN) diyaloğu çıkar → **Tamam** deyin.
   - Reddederseniz "VPN açma yetkisi yok." uyarısı görüntülenir; düğmeye tekrar basarak diyaloğu yeniden açabilirsiniz.
   - Bu izin bir kez verildikten sonra bir daha sorulmaz.
4. Bağlanınca düğme yeşile döner ve durum metni **"Bağlandı"** olur. (Yerel proxy adresi yalnızca Proxy modunda gösterilir.)
5. Bildirim çubuğunda **"FuckDPI — VPN çalışıyor"** bildirimi belirir; bildirimden **Duraklat** ve **Durdur** düğmeleriyle servisi yönetebilirsiniz. Duraklatınca "Hizmet duraklatıldı" bildirimi üzerinden **Başlat** ile devam ettirebilirsiniz.
6. (Android 7+) Hızlı ayarlar paneline **FuckDPI** kutucuğunu ekleyerek uygulamayı açmadan açıp kapatabilirsiniz.
7. Uygulama simgesine uzun basınca çıkan kısayollarda **"Aç/Kapat"** ve sabitlenmiş ilk 3 strateji yer alır; strateji kısayolu servisi o stratejiyle başlatır/değiştirir.

> **Not:** Bağlantı açıkken **Ayarlar** ekranı kilitlidir ("Ayarları açmak için bağlantıyı kesin").

## 5. Hazır strateji seçimi (TR presetleri)

Ana ekranda durum metninin altındaki hap biçimli **strateji düğmesine** dokunun — strateji listesi açılır. En üstte **"Otomatik"** öğesi (Akıllı Mod testini yeniden çalıştırır), altında sabitlenmiş stratejiler görünür:

| Strateji | Komut satırı argümanları | Ne zaman denenmeli |
|---|---|---|
| **TR Genel** | `-d1 -s1+s -d3+s -s6+s -a1` | İlk deneme; genel web trafiği için bölme/desenkronizasyon |
| **TR TTL-Fake** | `-f-1 -t5 -a1 -At,r,s -d1` | TTL=5 sahte paketler; genel çalışmazsa |
| **TR TTL3** | `-f-1 -t3 -a1` | TTL=3 sahte paketler; TTL-Fake'in daha düşük TTL'li hâli |
| **TR Discord/UDP** | `-Ku -a2 -An -f-1 -t5 -a1 -At,r,s -d1` | Discord ses/UDP trafiği için UDP odaklı seçenekler içerir |
| **TR OOB** | `-o1 -a1 -r-5+se` | Out-of-band veri gönderimi — **uygulamanın varsayılan stratejisi** |
| **TR Agresif** | `-d1 -d3+s -s6+s -d9+s -s12+s -d15+s -r1+s -a1` | Diğerleri yetmezse; çok sayıda bölme noktası + TLS kaydı parçalama |

- Bir stratejiye dokununca uygulanır; servis çalışıyorsa otomatik olarak yeni parametrelerle yeniden başlatılır ("Servis yeni parametrelerle yeniden başlatıldı").
- Seçili strateji hap düğme üzerinde adıyla gösterilir ("TR Genel" gibi); özel bir komut kullanıyorsanız "Özel ayar" yazar.
- Argümanların anlamı için uygulamadaki **Ayarlar → Hakkında → Talimatlar** (GitHub README) veya [motor belgelerine](https://github.com/winzysss/FuckDPI) bakın.

### Kendi stratejinizi ekleme / sabitleme

Komut satırı editörüne **Ayarlar → Gelişmiş → Komut satırı editörü** yolundan ulaşılır:

1. **"Komut satırı argümanları"** alanına dokunup komutu yazın (Temizle / Yapıştır düğmeleri mevcut).
2. Kaydedilen her komut **"Komut geçmişi"** listesine düşer (en fazla 40 kayıt).
3. Geçmişteki bir kayda dokununca menü açılır: **Uygula**, **Sabitle / Sabitlemeyi kaldır**, **Yeniden adlandır**, **Düzenle**, **Kopyala**, **Sil**.
4. Ana ekrandaki strateji seçici **yalnızca sabitlenmiş** komutları listeler (en üstte "Otomatik" öğesi de vardır). Akıllı mod kapalıyken liste boşsa "İhtiyacınız olan stratejileri düzenleyicide sabitleyin" uyarısı çıkar.
5. Geçmiş başlığına dokunarak "Sabitlenmemişleri sil" veya "Tüm geçmişi sil" diyebilirsiniz.

> *Ayarlar → Gelişmiş* bölümündeki **"Komut satırı editörünü kullan"** anahtarını kapatırsanız komut satırı yerine **UI editörü** (desenkronizasyon yöntemi, bölme pozisyonu, sahte paket TTL'i, SNI, protokol seçimleri gibi hazır alanlar) kullanılır.

## 6. DNS ayarı

**Ayarlar → Gelişmiş → DNS** öğesine dokunun. Şu seçenekler sunulur:

| Seçenek | DNS sunucusu |
|---|---|
| **Sistem** | DNS sunucusu değiştirilmez; ağın/operatörün DNS'i kullanılır |
| **Google** | `8.8.8.8` |
| **Cloudflare** | `1.1.1.1` — **varsayılan** |
| **Quad9** | `9.9.9.9` |
| **Yandex** | `77.88.8.8` |
| **Özel** | İstediğiniz IP'yi el yazısın — IPv4 veya IPv6 (örn. Yandex IPv6: `2a02:6b8::feed:0ff`). Yerel/loopback adresler reddedilir |

DNS'in çalışma biçimi ve sınırlamaları:

- DNS yalnızca **VPN modunda** geçerlidir; Proxy modunda bu ayar gizlenir. VPN tünelinin DNS sunucusu olarak atanır, yani DNS sorguları tünelden geçerek seçtiğiniz sunucuya gider.
- Sistemde **Özel DNS** (Private DNS) yapılandırılmışsa uygulama içi DNS devre dışı kalır; DNS öğesine dokununca sizi Android'in ağ ayarlarına yönlendirir ve ayarlar listesinde "Özel DNS" satırı görünür.
- IPv6 DNS kullanacaksanız **Ayarlar → Gelişmiş → IPv6** anahtarını da açmanız önerilir (tünele IPv6 adresi ve `::/0` rotası eklenir).
- Operatör DNS zehirlenmesi/korsanlığı yapıyorsa DNS'i **Sistem** dışında bir seçeneğe almak engelli sitelerin IP'sinin doğru çözülmesini sağlar.

## 7. Mod ve diğer önemli ayarlar

*Ayarlar → Genel* bölümünden:

- **Dil / Tema:** Türkçe dahil 5 dil; Sistem/Açık/Karanlık tema.

*Ayarlar → Gelişmiş* bölümünden (teknik ayarlar — çoğu kullanıcının dokunmasına gerek yok):

- **Mod:** `VPN` (varsayılan) veya `Proxy`.
  - **VPN:** Tüm cihaz trafiği yerel tünele alınır — çoğu kullanıcı için doğru seçim.
  - **Proxy:** Tünel kurulmaz; motor yalnızca yerel SOCKS5 proxy olarak çalışır (`127.0.0.1:1080`, aynı bölümdeki Dinleme IP'si ve Port ayarlarından değiştirilebilir). Trafiğini yönlendirmek istediğiniz uygulamalarda bu proxy'yi elle tanımlamanız gerekir; VPN izni istenmez.
- **Uygulama filtresi** (yalnızca VPN modu): `Kara liste` seçerseniz **"Uygulama seç"** ile işaretlediğiniz uygulamalar tünelin dışında kalır; `Beyaz liste` seçerseniz yalnızca seçtikleriniz tünelden geçer. (FuckDPI'nin kendi trafiği her durumda tünel dışında tutulur.)
- **IPv6:** IPv6 trafiğini de tünele alır.

*Ayarlar → Otomasyon*:

- **Akıllı mod:** İlk bağlanışta çalışan stratejiyi otomatik bulur (bkz. Bölüm 4).
- **"Cihaz başlangıcında servisi otomatik başlat":** Telefon yeniden başladığında servisi açar. Çalışması için VPN izninin önceden verilmiş olması gerekir — otomatik başlatmadan önce en az bir kez elle bağlanıp VPN iznini onaylayın.
- **"FuckDPI açılınca otomatik bağlan":** Uygulama ekranını her açtığınızda servisi başlatır.

*Ayarlar → İzinler*:

- **Pil optimizasyonu:** Sistemin pil optimizasyonu muafiyet ekranını açar. Muafiyet verilmişse "Devre dışı bırakıldı" yazar.
- **Depolama erişimi:** Stratejilerde dosya argümanları (örn. host listesi dosyaları) kullanacaksanız dosya erişimi verir.

Ayrıca FuckDPI, Android'in **"Her zaman açık VPN"** özelliğini destekler: *Sistem Ayarları → Ağ → VPN → FuckDPI* yanındaki dişliden "Her zaman açık VPN"i (ve dilerseniz "VPN olmadan bağlantıları engelle"yi) açabilirsiniz.

## 8. Uygulama içi güncelleme

- **Otomatik kontrol:** *Ayarlar → Hakkında → "Güncellemeleri kontrol et"* varsayılan olarak açıktır; uygulama açılışta (en fazla 6 saatte bir) GitHub'daki en son sürümü denetler.
- **Elle kontrol:** *Ayarlar → Hakkında → "Şimdi kontrol et"*. Güncel iseniz "Güncel sürümü kullanıyorsunuz" çıkar.
- Yeni sürüm bulunursa **"Güncelleme mevcut: x.y.z"** diyaloğu sürüm notlarıyla gösterilir:
  - **İndir:** Cihaz mimarinize uyan APK'yı (yoksa universal'i) indirir ("Güncelleme indiriliyor…"), ardından kurulumu başlatır. "Bilinmeyen kaynaklardan yükleme" izni yoksa önce izin ekranına yönlendirilirsiniz; izni verdikten sonra **İndir**'e tekrar dokunarak kurulumu tamamlayın.
  - **Bu sürümü atla:** Aynı sürüm için bir daha otomatik uyarı gösterilmez.
  - **İptal:** Diyaloğu kapatır.
- Kontrol/indirme hataları "Güncelleme kontrolü başarısız" / "Güncelleme indirilemedi" uyarılarıyla bildirilir (internet bağlantısı ve GitHub erişimi gerekir).

## 9. Hangi strateji çalışıyor? — Proxy testi

"Proxy testi" ekranına **Ayarlar → Gelişmiş → Proxy testi** yolundan ulaşılır. Bu özellik (Beta), uygulamaya gömülü hazır komut setlerini sırayla yerel proxy üzerinden deneyip seçili alan adlarına erişim başarılarını ölçer (Akıllı Mod'un yaptığı hızlı testin ayrıntılı sürümüdür):

1. Test ekranında test ayarlarından **"Alan Adı Listeleri"** ile test edilecek listeleri seçin. Hazır listeler: **Türkiye** (roblox.com, wattpad.com, pastebin.com vb.), **Discord**, **YouTube**, **Googlevideo**, **Social** (X, Instagram, Facebook…), **Telegram**, **Cloudflare**, **General** ve kendi oluşturduğunuz özel listeler. Türkçe arayüzde varsayılan olarak **Türkiye + Discord** listeleri aktiftir.
2. **"Testi başlat"** deyin; her strateji için "Kontrol ediliyor x/y" ilerlemesi ve site bazında başarı sayıları gösterilir.
3. Test bitince sonuç listesindeki (başarı yüzdesine göre sıralanır) bir komuta **dokunun**: **"Uygula"** komutu etkin strateji yapar (servis çalışıyorsa yeni parametrelerle yeniden başlatır); **"Kopyala"** panoya alır.
4. Test ekranı menüsünden test ayarlarına (komutlar arası gecikme, alan adı istek sayısı, paralel istek sınırı, zaman aşımı, sahte paket SNI'si, özel komut listesi) ve günlük kopyalama düğmesine ulaşılır.

> Test sırasında ana servis kullanılamaz; test bittiğinde sonuçlar saklanır. Başarılı test gerçek kullanım garantisi vermez — resmi uyarı bu şekildedir.

## 10. Sık karşılaşılan sorunlar

### Başka bir VPN açıkken FuckDPI çalışmıyor
Android'de aynı anda **yalnızca bir VPN** servisi etkin olabilir. Başka bir VPN açıksa FuckDPI başlatıldığında o VPN'in yerini alır (veya "Her zaman açık VPN" başka uygulamaya atanmışsa çakışma olur). Diğer VPN'i kapatın veya FuckDPI'yi **Proxy modunda** kullanıp trafiği uygulama bazında yönlendirin.

### Servis bir süre sonra kendiliğinden kapanıyor
- *Ayarlar → İzinler → Pil optimizasyonu*'ndan muafiyet verin.
- Xiaomi/MIUI, Huawei/EMUI, Oppo/vivo gibi agresif arka plan kısıtlamalı arayüzlerde sistem ayarlarından FuckDPI için ayrıca "otomatik başlatma / arka planda çalışmaya izin ver" seçeneklerini de açmanız gerekebilir.
- Servis kilitlenirse ana ekran menüsünden (⋮) **"Zorla Durdur"** ile uygulamayı tamamen kapatıp yeniden açabilirsiniz.

### "Bağlandı" ama engelli siteler hâlâ açılmıyor
- Strateji menüsünden **"Otomatik"** seçerek Akıllı Mod testini yeniden çalıştırın veya elle farklı bir strateji deneyin (önce TR Genel, sonra TR TTL-Fake / TR OOB, olmazsa TR Agresif); *Ayarlar → Gelişmiş → Proxy testi* ile de ayrıntılı test yapabilirsiniz.
- DNS'i değiştirin (örn. Yandex veya Quad9); operatör DNS'i engelli alan adlarını yanlış çözüyor olabilir.
- **IPv6** anahtarını açıp deneyin.
- Bazı engeller IP düzeyindedir; DPI aşma tek başına yetmeyebilir (bkz. SSS).

### Bazı siteler/uygulamalar FuckDPI açıkken bozuluyor
- *Ayarlar → Gelişmiş → Uygulama filtresi → Kara liste* ile sorunlu uygulamayı tünel dışına alın.
- UI editöründe **Hostlar → Kara liste / Beyaz liste** ile desenkronizasyonu alan adı bazında sınırlandırabilirsiniz.

### VPN izni bir türlü sorulmuyor / otomatik başlatma çalışmıyor
VPN izni yalnızca elle bağlanırken sorulabilir. Önce uygulamadan bir kez bağlanıp izni onaylayın; izin olmadan hızlı ayar kutucuğu, kısayollar ve cihaz başlangıcında otomatik başlatma sessizce çalışmaz.

### Sorun bildirirken
Ana ekran menüsünden **Tanılama** (cihaz, sürüm, strateji, izin durumları — "Kopyala" ile paylaşılabilir) ve **Günlükleri kaydet** (logcat dosyası) çıktılarını ekleyin.
