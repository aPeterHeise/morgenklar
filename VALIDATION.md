# Validierung

## Firebase-Verteilung, 13.11.2026

- Version 0.1.1 (Versionscode 2) erfolgreich zu Firebase App Distribution hochgeladen und an den vom Eigentümer ausgewählten Tester verteilt; Release-ID `34fe10uvi4erg` per API bestätigt.
- Das manuelle Release-Werkzeug übernimmt die veröffentlichte APK unverändert. Download und SHA-256-Prüfung am echten GitHub-Release v0.1.1 erfolgreich.
- 4 Python-Tests für ungültige Tags, Entwürfe, fehlende Assets, falsche Dateinamen und manipulierte APKs bestanden; Shell-Syntax und `git diff --check` ohne Befund.
- Manueller Release-Befehl im Trockenlauf gegen den echten GitHub-Release geprüft: korrekte App-/Empfängerargumente, geprüfte APK und anschließende Bereinigung der temporären Dateien. Dabei kein weiterer Firebase-Upload und keine erneute Einladung.
- Arbeitsdateien und 72 vorhandene historische Git-Blobs auf private Schlüssel, typische GitHub-/Google-Zugangsdaten und die Werte aus der bereitgestellten Firebase-Konfiguration geprüft: keine Treffer. 15 relevante Ignore-Fälle geprüft; keine Zugangsdaten-Dateien getrackt.
- Auf ausdrücklichen Wunsch bleiben Uploads manuell. Der zunächst vorbereitete GitHub-Workflow wurde entfernt. Es wurden weder Servicekonto noch Workload-Identity-Provider angelegt oder IAM-Berechtigungen vergeben.
- Keine Änderung am Android-Code oder an der APK für diese Verteilungsintegration.

Stand: 13. November 2026.

## Version 0.1.1: Leistungen und Empfehlungen

- 37 JVM-Tests bestanden, darunter Vollständigkeit der Quellen für jede bezifferte oder bestätigte Kostenübernahme über alle Kassen und repräsentative Altersgrenzen, explizite Prüfdaten und Empfehlungsherausgeber.
- Regressionen zu TK-PZR als begrenztem Zuschuss, DAK-Fristen und Hautscreening-Risiken, geteiltem Schwangerschaftsbudget und zusätzlichen Testvoraussetzungen sowie COVID-19 ab 75 und geändertem Impfabstand geprüft.
- 8 Android-UI-Tests auf Android 16 / API 36 bestanden. Der neue PZR-Test prüft Zuschuss, Empfehlungsherausgeber, erreichbare Quellen-Schaltflächen und sichtbares Prüfdatum in den Details.
- PZR-Ansichten nach einem separaten erfolgreichen Screenshotlauf visuell geprüft: `dist/previews/08-pzr-empfehlung.png` und `dist/previews/09-pzr-kosten.png`. Getestet auf einem temporären Read-only-Emulator; gespeicherte Daten anderer Test-Apps bleiben unverändert.
- `testDebugUnitTest connectedDebugAndroidTest assembleRelease --offline`: erfolgreich. Die obligatorische Release-Prüfung (`lintVitalRelease`) lief mit; ein neuer vollständiger Lint-Lauf war nicht Teil dieser Änderung.
- Release 0.1.1, Versionscode 2, unter `dist/morgenklar-0.1.1.apk`; Prüfsumme in `dist/SHA256-0.1.1.txt`. Der Signaturfingerabdruck stimmt mit der bisher verteilten Vorschau 0.1.0 überein. Das Update benötigt daher keine Deinstallation.
- Ein erster Gerätetest startete wegen eines abweichenden temporären Debugschlüssels nicht; nach Nutzung des Originalschlüssels konnte das Update installiert werden. Ein weiterer Durchlauf scheiterte nur am mehrdeutigen Quellenschalter im neuen Test. Der abschließende Lauf selektiert die TK-Kostenkarte ausdrücklich und bestand vollständig.
- 63 Angebote mit getrennten Leistungs- und Empfehlungsquellen; 70 Quellen insgesamt, davon 43 am 13.11.2026 konkret nachgeprüft. Andere Hintergrundquellen zeigen ausdrücklich nur den bisherigen Katalogstand. Datum und Versionsstand sind keine automatische Aktualisierung.

## Ursprüngliche Version 0.1.0: Build und Regeln

- Optimierte `release`-APK gebaut, ungefähr 2,2 MB; Android 9 / API 28 als Mindestversion, Ziel-API 36.
- 32 JVM-Tests bestanden: 22 für Alters-/Historien-/Kassenlogik, 10 für Schwangerschaft.
- Geprüft sind unter anderem jedes Altersjahr 0–110, Profile Mann 38 / Frau 60, genaue Altersgrenzen, einmalige Leistungen, Kalenderintervalle, Mammographie-Ende mit 76, angekündigte Änderungen, Rauchschwellen und Rauchstopp, Darmvorsorge-Alternativen und Pausen, Organanpassungen, falsche/duplizierte Daten, verschlüsselt speicherbare Datenstruktur und getrennte Schwangerschaftsepisoden.
- Schwangerschaft: Glukose-Screening 24+0–27+6 an beiden Grenzen, Ultraschallfenster, fehlender Geburtstermin, verstrichene Zeitfenster, NIPT-/Anti-D-Bedingungen und separate Historie geprüft.
- Android Lint: 0 Fehler, 7 Hinweise (neue Bibliotheksversionen und URI-KTX-Empfehlung). Keine Baseline zum Unterdrücken von Fehlern.
- APK-Signatur mit `apksigner` verifiziert, v2-Signatur. Private Vorschau mit lokalem Debugschlüssel.
- APK-Manifest geprüft: keine Internet-, Standort-, Kamera-, Kontakte- oder Speicherberechtigung. Nur interne Signaturberechtigung für nicht exportierte Receiver.
- App-Daten werden über Android Keystore / AES-GCM und `AtomicFile` geschrieben. Android-Backup deaktiviert; interne Dateien explizit aus Cloud-Backup und Geräteübertragung ausgeschlossen.

## Gerätetests

Die automatisch erzeugten Screenshots verwenden ausschließlich Testprofile. Die App selbst enthält keine vorangelegten Personen.

Ein erster Durchlauf vor der Schwangerschaftserweiterung bestätigte Onboarding mit Rauchangaben, unabhängige Profile mit Neustart, echte Terminhistorie, verschlüsselte Speicherung und Löschung. Ein Test musste für das Scrollen in einer erst bei Bedarf aufgebauten Liste korrigiert werden.

Ein späterer gemeinsamer Emulatorlauf wurde abgebrochen, weil gleichzeitig ein Test einer anderen App die Vordergrund-Activity wechselte. Dieser abgebrochene Lauf zählt nicht als bestandene Validierung. Der abschließende Durchlauf auf dem wieder freien Emulator bestand: **7 Tests, 0 Fehler**, Laufzeit 34,702 Sekunden, Android 16 / API 36 ARM64. Geprüft wurden Onboarding mit Rauchangaben, unabhängige Profile nach Neustart, echte Termindokumentation mit neuer Fälligkeit, verschlüsselte Speicherung/Löschung, alle fünf Ansichten einschließlich Altersatlas, Schwangerschaftsfrage samt Wochenplan nach Neustart sowie Screenshot-Erzeugung.

Die Screenshots in `dist/previews/` wurden visuell geprüft. Sie zeigen den tatsächlichen Android-Build mit Testprofilen, keine Web-Mockups.

## Grenzen

Kein Test ersetzt eine fachärztliche Prüfung oder belegt klinische Wirksamkeit. Nicht auf einem physischen Telefon und nicht auf jeder unterstützten Android-Version geprüft. Extrem große Schrift, Herstellerbesonderheiten und externe Browser müssen gegebenenfalls separat geprüft werden. Der historische Katalog wird nicht automatisch aktualisiert. Keine systematische medizinische Nachsorge-, genetische Hochrisiko-, vollständige Impf-Dosis- oder Wochenbettplanung.
