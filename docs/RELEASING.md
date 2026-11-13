# Manuelle Firebase-Verteilung

Uploads zu Firebase App Distribution bleiben auf Wunsch des Eigentümers manuell. GitHub-Releases lösen keinen automatischen Upload aus. Es gibt kein GitHub-Servicekonto, keinen Workload-Identity-Provider und keine in GitHub gespeicherten Firebase-Anmeldedaten für diesen Ablauf.

Version 0.1.1 (Versionscode 2) ist bereits in Firebase verfügbar. Das manuelle Werkzeug verteilt die signierte APK aus dem GitHub-Release unverändert und übernimmt dessen Release-Notizen.

## Ein Release verteilen

Nach Veröffentlichung des GitHub-Releases im Projektverzeichnis ausführen:

```bash
bash scripts/distribute_firebase_release.sh v0.1.1
```

Für eine neue Version den Tag entsprechend ersetzen. Voraussetzungen sind Python 3, Node.js 20 oder neuer, die GitHub-CLI und eine gültige lokale Firebase-Anmeldung. Bei Bedarf anmelden:

```bash
gh auth login
npx -y firebase-tools@latest login
```

Die Firebase-Anmeldung bleibt in der lokalen CLI-Konfiguration außerhalb des Repositorys. Das Skript benötigt keine privaten Servicekonto-Schlüssel. Temporäre APK- und Notizdateien werden nach dem Lauf entfernt.

## GitHub-Release vorbereiten

1. Android-Version (`versionName`, `versionCode`) erhöhen, prüfen und die Release-APK mit demselben Schlüssel bauen.
2. Code nach GitHub pushen.
3. Release mit Tag `v<version>` und diesen beiden Dateien veröffentlichen:
   - `morgenklar-<version>.apk`
   - `SHA256-<version>.txt` mit genau einer Zeile: `<sha256>  morgenklar-<version>.apk`
4. Den manuellen Verteilungsbefehl mit diesem Tag ausführen.

Das Werkzeug prüft SHA-256, Dateiname, Dateigröße und den GitHub-Asset-Digest, soweit vorhanden. Entwürfe, fehlende Dateien oder abweichende Prüfsummen werden nicht verteilt. Bei verspäteten Assets wartet es bis zu einer Minute. Ein zweiter Build ist nicht erforderlich; das Update behält die vorhandene Signatur.

## Empfänger

Die GitHub-Repositoryvariable `FIREBASE_TESTERS` enthält die ausgewählten Tester-E-Mail-Adressen. Sie kann unter Settings → Secrets and variables → Actions → Variables geändert werden. Sie enthält Empfängerangaben, keine Zugangsdaten.

Alternativ lassen sich Empfänger für einen einzelnen manuellen Aufruf über die Umgebungsvariablen `FIREBASE_TESTERS` und optional `FIREBASE_GROUPS` angeben. Mehrere E-Mail-Adressen oder Firebase-Gruppen-Aliase mit Kommas trennen. Das Skript liest standardmäßig `FIREBASE_TESTERS` aus GitHub; eine fehlende Variable führt zu einem Fehler statt zu einem unbemerkten Upload ohne Empfänger. Eine vorhandene, leere Variable erlaubt einen reinen Upload.

Tester müssen die Firebase-Einladung annehmen. Der Befehl zeigt nach erfolgreicher Verteilung Links zur Firebase-Konsole und zur Installation. Eine identische APK wird bei erneutem Upload dem bestehenden Firebase-Release zugeordnet.

## Firebase-Ziel und Konfigurationsdatei

- Projekt: `peter-heise-website-2026` (`166111565018`)
- Android-Paket: `de.morgenklar.app`
- Firebase App-ID: `1:166111565018:android:e9a8d6ab09ec29a4e581e3`

Diese Kennungen sind keine Zugangsdaten. Die bereitgestellte `google-services.json` wurde nur zur Zuordnung dieser Kennungen gelesen und nicht in das Repository kopiert. Für App Distribution allein reichen die Registrierung und die signierte APK; der Google-Services-Gradle-Plugin und Firebase-Runtime-SDKs sind dafür nicht erforderlich.

`.gitignore` schließt Google-Services-Dateien, lokale Umgebungsdateien, private Schlüssel und Zertifikatscontainer, typische Servicekonto-/Anmeldedateien, Firebase-Debuglogs und generierte GitHub-OIDC-Anmeldedateien aus. Zugangsdaten unter abweichenden Namen müssen ebenfalls vor jedem Commit geprüft werden; `.gitignore` entfernt keine bereits getrackten Dateien.

## Quellen

- [Firebase: APKs mit der CLI verteilen](https://firebase.google.com/docs/app-distribution/android/distribute-cli)
- [Firebase: Servicekonto und Zugangsdaten](https://firebase.google.com/docs/app-distribution/authenticate-service-account?platform=android)
