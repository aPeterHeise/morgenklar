# GitHub → Firebase App Distribution

Der Workflow `.github/workflows/firebase-app-distribution.yml` übernimmt bei jedem **veröffentlichten GitHub-Release** (auch Pre-Releases) die bereits signierte APK und die Release-Notizen. Entwürfe und reine Git-Pushes lösen keinen Upload aus. Dieselbe APK wird verteilt; ein zweiter Build oder ein anderer Signierschlüssel ist nicht erforderlich.

**Einrichtungsstand:** Workflow vorbereitet. Die einmalige Einrichtung der Google-Cloud-Identität und ihrer IAM-Berechtigungen wartet auf Zustimmung. Erst nach Einrichtung und erfolgreichem Probelauf ist die automatische Verteilung betriebsbereit.

## Firebase-Ziel

- Projekt: `peter-heise-website-2026` (`166111565018`)
- Android-Paket: `de.morgenklar.app`
- Firebase App-ID: `1:166111565018:android:e9a8d6ab09ec29a4e581e3`
- Servicekonto: `morgenklar-distribution@peter-heise-website-2026.iam.gserviceaccount.com`
- Workload Identity Provider: `projects/166111565018/locations/global/workloadIdentityPools/morgenklar-github/providers/github`

GitHub erhält kurzlebige Google-Zugangsdaten per OpenID Connect. Es werden keine privaten Servicekonto-Schlüssel und keine persönlichen Firebase-Login-Tokens in GitHub gespeichert. Das Servicekonto benötigt `roles/firebaseappdistro.admin` im genannten Projekt; diese Rolle gilt für App Distribution aller Apps dieses Projekts, nicht für andere Firebase-Dienste.

Die Vertrauensbedingung begrenzt den Zugriff auf Repository-ID `1456414456`, Eigentümer-ID `416712` und genau diesen Workflow: Release-Ereignisse mit `refs/tags/v…` sowie manuelle Starts auf `main`. Pull Requests und andere Repositorys erhalten keinen Zugriff. Das Servicekonto erlaubt diesem Provider nur `roles/iam.workloadIdentityUser`.

## Ein Release erstellen

1. Android-Version (`versionName`, `versionCode`) erhöhen, prüfen und die Release-APK mit demselben Schlüssel bauen.
2. Code nach GitHub pushen. Der Release-Commit muss den Workflow enthalten.
3. Einen GitHub-Release mit Tag `v<version>` erstellen und **vor Veröffentlichung beide Dateien anhängen**:
   - `morgenklar-<version>.apk`
   - `SHA256-<version>.txt` mit genau einer Zeile: `<sha256>  morgenklar-<version>.apk`
4. Release veröffentlichen. Der Workflow prüft SHA-256, Dateiname und Größe und lädt die APK mit den GitHub-Release-Notizen hoch.
5. Unter GitHub Actions den Erfolg prüfen. Die Zusammenfassung verlinkt die Firebase-Releases; ausgewählte Tester erhalten den Installationslink per E-Mail.

Bei verspäteten Assets wartet der Workflow bis zu einer Minute. Fehlen Dateien oder stimmt die Prüfsumme nicht, schlägt er sichtbar fehl und lädt keine APK hoch. Bei einem Fehler kann der Lauf wiederholt werden.

## Empfänger und erneute Verteilung

In GitHub → Settings → Secrets and variables → Actions → **Variables**:

- `FIREBASE_TESTERS`: durch Kommas getrennte Tester-E-Mail-Adressen.
- `FIREBASE_GROUPS`: optional durch Kommas getrennte Firebase-Gruppen-Aliase.

Sind beide leer, wird nur hochgeladen; es erfolgt keine Einladung an Tester. Tester müssen ihre Firebase-Einladung annehmen, bevor sie die APK installieren können.

Ein bereits veröffentlichter Release kann über **Actions → Firebase App Distribution → Run workflow** mit seinem Tag erneut verteilt werden. Hierbei `main` als Branch verwenden. Bei identischer APK verwendet Firebase den bestehenden Release.

## google-services.json und Gradle

Für App Distribution allein reichen die App-Registrierung und die signierte APK. Die bereitgestellte `google-services.json` wurde genutzt, um Projekt, App-ID und Paketnamen zuzuordnen. Der Google-Services-Gradle-Plugin und Firebase-Runtime-SDKs sind für diesen Ablauf nicht erforderlich. Die Datei wird nicht in das Repository aufgenommen. Sollte später ein Firebase-SDK in der App benötigt werden, wird dessen Einrichtung separat ergänzt.

## Quellen

- [Firebase: APKs mit der CLI verteilen](https://firebase.google.com/docs/app-distribution/android/distribute-cli)
- [Firebase: Servicekonto für App Distribution](https://firebase.google.com/docs/app-distribution/authenticate-service-account?platform=android)
- [Google: GitHub Actions mit Workload Identity Federation](https://github.com/google-github-actions/auth)
- [GitHub: Release-Ereignisse](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#release)
