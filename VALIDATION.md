# Validierung

Stand: 13. November 2026.

## Build und Regeln

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
