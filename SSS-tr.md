# FuckDPI — Sık Sorulan Sorular (Türkçe)

## Bu uygulama bir VPN mi?

**Hayır.** FuckDPI trafiğinizi uzak bir sunucuya göndermez, şifrelemez ve IP adresinizi gizlemez.

Teknik olarak olan şey şudur: uygulama, Android'in `VpnService` arayüzünü yalnızca cihaz trafiğini yakalamak için kullanır. Tüm trafik cihazınızın içinde çalışan yerel bir SOCKS5 proxy'sine (`127.0.0.1:1080`) yönlendirilir; bu proxy'yi çalıştıran yerel motor paketleri DPI sistemlerini yanıltacak şekilde işler (bölme, sahte paket, sıra bozma, out-of-band veri vb.) ve **doğrudan hedef siteye**, sizin gerçek IP'nizle gönderir. Cihazınız ile internet arasında üçüncü bir taraf yoktur.

VPN simgesi ve "VPN çalışıyor" bildirimi bu yüzden görünür — Android'de sistem genelinde trafik yakalamanın tek yolu bu arayüzdür.

## Öyleyse IP adresim gizleniyor mu?

Hayır. Siteler ve operatörünüz sizin gerçek IP adresinizi görmeye devam eder. Anonimleşmek veya coğrafi kısıtlaması sunucu tarafında uygulanan hizmetleri açmak istiyorsanız gerçek bir VPN/proxy gerekir — FuckDPI bunu sağlamaz.

## İnterneti yavaşlatır mı?

Trafik uzak bir sunucuya gitmediği için VPN'lerdeki gibi bir "sunucu hızı" darboğazı yoktur; hızınız esas olarak hattınızın hızıdır. Tüm işleme cihaz üzerinde yapılır ve ek yük çok küçüktür. Ancak:

- Bazı agresif stratejiler (çok sayıda bölme, sahte paketler, SACK düşürme) belirli sitelerde veya zayıf hatlarda kararsızlık/yavaşlama yaratabilir.
- Bir site veya uygulama sorun çıkarırsa strateji değiştirin ya da o uygulamayı *Ayarlar → Gelişmiş → Uygulama filtresi* ile tünel dışında bırakın.

## Hangi sitelerde işe yarar?

FuckDPI, operatörünüzün **DPI tabanlı** uyguladığı engelleme ve yavaşlatmalarda işe yarar. Uygulamadaki test listeleri tipik hedefleri gösterir:

- **Türkiye listesi** (Türkçe arayüzde varsayılan olarak testte aktif): roblox.com, wattpad.com, pastebin.com, wikileaks.org, bit.ly, cutt.ly, t2m.io, imgur.com, gist.github.com, torproject.org, archive.today, imvu.com, tango.me, duckdns.org, protonvpn.com, nordvpn.com
- Discord, YouTube (video CDN dahil), sosyal medya (X, Instagram, Facebook, Snapchat, LinkedIn, Medium, SoundCloud, Proton), Telegram, Cloudflare ve genel liste

**İşe yaramadığı durumlar:**

- Sitenin kendisi Türkiye IP'lerini engelliyorsa (DPI aşma IP'nizi değiştirmez).
- Operatör engeli IP düzeyinde veya trafiği tamamen keserek uyguluyorsa.
- Site kapalı/çökmüşse ya da sorun sizin bağlantınızdaysa.

## Operatörüme göre hangi stratejiyi seçmeliyim?

Hiçbir şey seçmenize gerek yok — **Akıllı Mod** bunu sizin için yapar. İlk bağlanışta uygulama arka planda küratörlü stratejileri sizin hattınızda gerçek sitelere karşı hızlıca dener ve en çok işe yarayanı otomatik seçer. Ağınız veya operatörünüz değişirse strateji menüsünden **"Otomatik"** öğesini seçerek testi yeniden çalıştırabilirsiniz.

Elle denemek isterseniz: strateji düğmesinden sırayla **TR Genel → TR TTL-Fake → TR TTL3 → TR OOB → TR Agresif** presetlerini deneyin. En ayrıntılı yol ise *Ayarlar → Gelişmiş → Proxy testi* ile **Proxy testi**ni çalıştırmaktır — hazır komut setleri gerçek sitelere karşı denenir, sonuçlar başarı yüzdesine göre sıralanır; bir komuta dokunup **"Uygula"** deyin.

Testte hangi listelerin kullanılacağını test ekranının sağ üstündeki ayar (⚙) simgesinden **Alan Adı Listeleri** öğesinden seçersiniz — açmak istediğiniz servise göre (örn. yalnızca Discord için Discord listesi).

## İlk bağlanışta "Kontrol ediliyor x/12" yazması normal mi?

Evet — bu **Akıllı Mod**'un hızlı testidir. Uygulama ilk bağlanışta küratörlü stratejileri sizin ağınızda gerçek sitelere karşı dener ve en iyisini otomatik seçer; bir sonraki bağlanışlar anında olur. Güç düğmesine tekrar basarak iptal edebilirsiniz. Kapatmak isterseniz: *Ayarlar → Otomasyon → Akıllı mod*.

## Discord'a bağlanıyorum ama sesli sohbet çalışmıyor

Ses trafiği UDP kullanır. **TR Discord/UDP** presetini deneyin (`-Ku` gibi UDP odaklı seçenekler içerir). UI editöründeyseniz *Protokoller* altında **UDP desenkronizasyonu**'nun açık olduğundan emin olun.

## Aynı anda gerçek bir VPN ile kullanabilir miyim?

VPN modunda **hayır** — Android aynı anda tek bir VPN servisine izin verir; ikisinden biri diğerinin yerine geçer. Alternatifler:

- FuckDPI'yi **Proxy moduna** alın ve trafiğini aşmak istediğiniz uygulamalarda `127.0.0.1:1080` SOCKS5 proxy'sini elle tanımlayın; VPN'iniz ayrıca çalışmaya devam eder.
- Ya da diğer VPN'i kapatın.

## Pil tüketimi ne kadar?

Servis arka planda yerel işlem yaptığı için küçük bir ek tüketim olur; uzak sunucu trafiği olmadığından tipik VPN'lerden farklı değildir. Asıl önemli nokta tersidir: sistemin uygulamayı **öldürmemesi** için *Ayarlar → İzinler → Pil optimizasyonu* muafiyetini verin (ilk açılışta bir kez otomatik sorulur).

## Bildirimdeki "Duraklat" ne yapıyor?

Servisi tüneli kapatarak askıya alır; bildirim "Hizmet duraklatıldı" olur ve **Başlat** ile kaldığı yerden devam eder. **Durdur** ise servisi tamamen kapatır.

## Telefon yeniden başlayınca kendiliğinden açılır mı?

Evet: *Ayarlar → Otomasyon → "Cihaz başlangıcında servisi otomatik başlat"*. İki koşulu vardır:

1. VPN izni daha önce verilmiş olmalı (en az bir kez elle bağlanın).
2. Bazı üretici arayüzlerinde (Xiaomi, Huawei vb.) sistem ayarlarından uygulamaya ayrıca "otomatik başlatma" izni vermeniz gerekebilir.

Ek olarak *"FuckDPI açılınca otomatik bağlan"* seçeneği uygulamayı her açışta bağlar; Android sistem ayarlarından **"Her zaman açık VPN"** de desteklenir.

## Verilerim nereye gidiyor, güvenli mi?

Trafiğiniz cihazınızdan çıkıp doğrudan hedef siteye gider — arada FuckDPI'ye ait ya da başka bir sunucu yoktur; uygulama kendi internet trafiğini de tünelin dışında tutar. Kaynak kodu herkese açıktır: [github.com/winzysss/FuckDPI](https://github.com/winzysss/FuckDPI). Yine de unutmayın: trafik **şifrelenmez** — siteyle aranızdaki koruma, sitenin kendi HTTPS/TLS şifrelemesidir.

## Kullanmak yasal mı?

FuckDPI yalnızca ağ paketlerinin gönderilme biçimini değiştiren bir araçtır; yasal durum ülkenize ve kullanım amacınıza göre değişir. Yerel mevzuata uymak ve ortaya çıkabilecek sonuçlar kullanıcının sorumluluğundadır.

## Güncellemeler nasıl oluyor?

Uygulama yalnızca GitHub Releases üzerinden dağıtılır. *Ayarlar → Hakkında → "Güncellemeleri kontrol et"* açıksa açılışta kontrol eder (en fazla 6 saatte bir); **"Şimdi kontrol et"** ile elle de bakabilirsiniz. Yeni sürüm çıkarsa uygulama içinden APK'yı indirip kurabilirsiniz (cihazınıza uygun mimari otomatik seçilir). Ayrıntılar `KURULUM-tr.md` Bölüm 8.

## Ana ekrandaki strateji listesi boş / presetlerim kayboldu

Strateji seçici yalnızca **sabitlenmiş** komutları gösterir. TR presetleri ilk kurulumda otomatik sabitlenir; yanlışlıkla sildiyseniz *Ayarlar → Gelişmiş → Komut satırı editörü → komut geçmişi* üzerinden yeniden ekleyip **Sabitle** deyin. Preset komutları `KURULUM-tr.md` Bölüm 5'teki tabloda mevcuttur.

## "Komut satırı editörü" ile "UI editörü" farkı ne?

Varsayılan olarak **komut satırı editörü** aktiftir: motor argümanlarını doğrudan yazarsınız (presetler bu formattadır). *Ayarlar → Gelişmiş → "Komut satırı editörünü kullan"* anahtarını kapatırsanız hazır alanlardan oluşan **UI editörü** (desenkronizasyon yöntemi, bölme pozisyonu, sahte paket TTL'i, sahte SNI, OOB veri, protokol seçimleri, host kara/beyaz listesi) devreye girer.

## Proxy adresi neden 127.0.0.1:1080 yazıyor, değiştirebilir miyim?

Bu, motorun cihaz içinde dinlediği yerel SOCKS5 adresidir (Ayarlar → Gelişmiş → Dinleme IP'si ve Port ayarları). VPN modunda doğrudan kullanmazsınız — tünel otomatik bağlanır; Proxy modunda ise uygulamalarınıza bu adresi tanımlarsınız. Komut satırına `-i`/`-p` (ya da `--ip`/`--port`) yazarak da değiştirilebilir.

## Hangi motor sürümünü kullanıyor?

*Ayarlar → Hakkında* bölümünde uygulama sürümü ve motor sürümü (bu sürümde **0.17.3**) listelenir.
