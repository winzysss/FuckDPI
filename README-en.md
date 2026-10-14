<div align="center">
  <p>
    <img src="https://github.com/winzysss/FuckDPI/raw/master/.github/images/app.png" alt="FuckDPI logo" width="200" />
  </p>
  <h1>FuckDPI</h1>
  <p><em>Spread your wings to a freer internet — a DPI circumvention tool for Android</em></p>
  <p>
    <a href="README.md">Türkçe</a> |
    English
  </p>
  <p>
    <a href="https://github.com/winzysss/FuckDPI/releases/latest"><img src="https://img.shields.io/github/v/release/winzysss/FuckDPI" alt="Latest Release" /></a>
    <a href="https://github.com/winzysss/FuckDPI/releases"><img src="https://img.shields.io/github/downloads/winzysss/FuckDPI/total" alt="Downloads" /></a>
    <a href="https://github.com/winzysss/FuckDPI/blob/master/LICENSE"><img src="https://img.shields.io/github/license/winzysss/FuckDPI" alt="License" /></a>
    <a href="https://github.com/winzysss/FuckDPI"><img src="https://img.shields.io/github/languages/code-size/winzysss/FuckDPI" alt="GitHub code size in bytes"/></a>
  </p>
</div>

FuckDPI is a fully open-source Android app built **for educational purposes** that bypasses **DPI (Deep Packet Inspection) based blocking**. It runs a local desync engine on your device and routes all traffic through it — no remote server, no accounts, no logging.

> **This project is developed for education and research.** You are solely responsible for how you use it. See the [Disclaimer](#disclaimer) section for details.

---

## How it works

```
Your apps' traffic → [Android VpnService / TUN] → Local tunnel inside the device
→ DPI desync engine (fragments / reshapes packets) → Direct to the target site
```

- **This is not a conventional VPN.** Your traffic never leaves your device for a third-party server; the tunnel runs entirely locally.
- It does **not encrypt** your traffic and does **not hide** your IP — its goal is DPI manipulation, not privacy.
- The engine is open source; it uses well-known desynchronization techniques such as packet fragmentation, fake packets and TTL manipulation.

## Why does it ask for VPN permission?

`VpnService` is the only supported way for an app to intercept other apps' traffic on Android. The "may monitor network traffic" prompt is Android's standard text — traffic passes through the app but **never leaves your device**. If you prefer not to grant it, switch to **Proxy mode** under Settings → Advanced → Mode (you then route traffic yourself).

## Smart Mode — one tap, that's it

You don't need to know anything about configuration:

1. Open the app and tap the big button.
2. On first run, it performs a **quick test** in the background: 12 curated strategies against real sites, stopping early once a high-success one is found.
3. The best strategy is selected automatically and you connect. **No choices, no setup.**
4. If your carrier or network changes, pick "Automatic" in the strategy menu to re-run the test.

## Features

- **One-tap connect** — a clean main screen with no technical clutter
- **Smart Mode** — discovers which strategy actually works on your network
- **Ready-made strategies** — pinned to your strategy list on first launch
- **VPN and Proxy modes** — capture all traffic, or run a standalone SOCKS5 proxy
- **Per-app filtering** — choose which apps go through the tunnel
- **Autostart** — starts the service automatically on boot
- **In-app updates** — checks GitHub Releases for new versions
- **Multi-language** — Turkish, English, Russian, Kazakh, Vietnamese
- **Android TV/BOX support** — including a Quick Settings tile
- **Advanced settings** — DNS, IPv6, strategy editor, test lists (all under Advanced)

## Installation

1. Open the [Releases](https://github.com/winzysss/FuckDPI/releases/latest) page.
2. Download the APK matching your device:

| File | Which device |
|---|---|
| `*-arm64-v8a-release.apk` | Most modern phones (recommended) |
| `*-armeabi-v7a-release.apk` | Older 32-bit devices |
| `*-x86-release.apk` / `*-x86_64-release.apk` | Emulators / x86 tablets |
| `*-universal-release.apk` | Get this if you're unsure |

3. Allow installation from unknown sources when Android asks, then install.
4. Open it, press the button, grant VPN permission — Smart Mode handles the rest.

To verify your download, compare its SHA-256 hash with `checksums.txt` in the release.

## Which countries does it work in?

The techniques are not country-specific — Smart Mode tests **your actual network**, so it can work outside Turkey too. To be honest though:

- No single strategy works everywhere; it varies by ISP, region and time.
- Against systems with active probing and IP blocking (e.g. China's Great Firewall) it may not be sufficient on its own — a limitation of this entire class of tools.
- Preset names are Turkey-oriented but usable by anyone; you can customize the test site list for your country (Settings → Advanced → Proxy test → ⚙ → Domain Name Lists).

## Building

Requirements: JDK 17 · Android SDK 36 (build-tools `36.0.0`) · NDK `27.2.12479018` · CMake 3.22.1

```bash
git clone --recurse-submodules https://github.com/winzysss/FuckDPI.git
cd FuckDPI
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/`

> The native tunnel component cannot be built directly on Windows — use WSL, or grab the CI-built APKs. Signed release builds run on GitHub Actions (`.github/workflows/release.yml`): push a `v*` tag and CI handles the rest.

## Project layout

- `app/src/main/java/...` — Kotlin source (VpnService, smart mode, UI)
- `app/src/main/cpp/core` — DPI desync engine (C, third-party MIT-licensed component)
- `app/src/main/jni/tunnel` — TUN↔SOCKS5 tunnel layer
- `fastlane/` — store metadata · `.github/workflows/` — release CI

## Warnings

- This is **not a privacy tool**: no encryption, no remote tunnel, no IP hiding. For anonymity use a real VPN or Tor.
- Effectiveness depends on your ISP and network; it is not guaranteed.
- The app is distributed **only via this GitHub repository** — do not trust APKs from other sources.

## Disclaimer

This project is open-source software developed for educational and research purposes. Circumventing DPI-based blocking may be restricted by the laws of your country or your ISP's terms of service. By using this software you accept full responsibility for any consequences; the developer assumes no liability. This notice is informational and does not limit the rights granted by the GNU GPL v3 license in the [LICENSE](LICENSE) file.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE) — fully open source. Third-party components ship under MIT/BSD licenses; see [NOTICE](NOTICE) for details.

## Contributing

Bug reports and feature requests go to [Issues](https://github.com/winzysss/FuckDPI/issues), general questions to [Discussions](https://github.com/winzysss/FuckDPI/discussions). Pull requests are welcome.
