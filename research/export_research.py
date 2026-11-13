import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]
c=json.loads((root/'app/src/main/assets/catalog.json').read_text())
sources={s['id']:s for s in c['sources']}
def links(ids):
    def link(s):
        date = f"zuletzt geprüft {s['checked']}" if s.get('checked') else f"Katalogstand {c['reviewed']}, nicht erneut geprüft"
        edition = f"; {s['edition']}" if s.get('edition') else ''
        return f"[{s['title']}]({s['url']}) ({date}{edition})"
    return ', '.join(link(sources[s]) for s in ids)
text='''# Morgenklar – Recherche und Leistungsmodell

Stand: 13. November 2026. Deutschland; allgemeine Vorsorge für Menschen ohne Beschwerden. Schwerpunkt Erwachsene ab 30, mit einer ergänzenden Altersübersicht ab Geburt. Eine Kassenleistung ist ein Anspruch, nicht automatisch eine medizinische Empfehlung oder Pflicht. Neue Beschwerden, Schwangerschaft, bekannte Erkrankungen, frühere auffällige Befunde und genetische Hochrisiken benötigen individuelle Versorgung.

## Die drei größten einzelnen gesetzlichen Krankenkassen

Vergleich nach Zahl der Versicherten, nicht nur beitragszahlender Mitglieder, zum gemeinsamen Stichtag 01.07.2026:

| Kasse | Versicherte |
|---|---:|
| Techniker Krankenkasse | 12.449.839 |
| BARMER | 8.183.598 |
| DAK-Gesundheit | 5.390.481 |

Die AOK-Gemeinschaft besteht aus mehreren rechtlich eigenständigen Kassen und wird nicht als eine einzelne Kasse gezählt. '''+links(['ranking'])+'''

## Konkreter Vergleich

Alle drei übernehmen die gesetzlichen Früherkennungsleistungen bei erfüllten Voraussetzungen ohne Zuzahlung. Eine privat vereinbarte Zusatzleistung ist dadurch nicht mitbezahlt.

| Leistung | TK | BARMER | DAK |
|---|---|---|---|
| Gesetzlicher Check-up | 18–34 einmal; ab 35 alle 3 Kalenderjahre, 100 % | gleich | gleich |
| Haut-Screening ab 35 | alle 2 Kalenderjahre, 100 % | gleich | gleich |
| Haut-Check unter 35 | meist ab 15, in BY/HE/SH auch früher; alle 24 Monate in Vertragspraxis, 100 % | alle 2 Jahre, 100 % bei regional passenden Alters-/Vertragsbedingungen | 17–34 bei Risikofaktoren, qualifizierte Praxis, bis 60 € je 2 Kalenderjahre |
| Weiterer Check-up unter 35 | 18–34 bei Risiken bis 35 € alle 3 Jahre, Satzung § 27n | kein pauschales wiederkehrendes Extra auf geprüfter Leistungsseite belegt | 17–34 bei Risiken bis 40 € alle 2 Kalenderjahre |
| Darm-Stuhltest vor 50 | kein pauschales Extra in dieser Recherche bestätigt | kein pauschales Extra in dieser Recherche bestätigt | 39–49 bei Risiken bis 40 € alle 2 Kalenderjahre |
| Professionelle Zahnreinigung | ab 18, bis 40 €, einmal je Kalenderjahr | über Bonus, kein allgemeiner direkter Zuschuss | bis 60 € je Kalenderjahr; Antrag für Vorjahresrechnungen bis 31.03. |
| Sportmedizin | 80 %, höchstens 60 € Basis / insgesamt 120 € erweitert, alle 2 Jahre, medizinische Voraussetzungen | über Bonus | höchstens 60 € Basis / insgesamt 120 € erweitert bei Risiken, alle 2 Jahre, ärztliche Bescheinigung |
| U10, U11, J2 | übernommen in Kinder- und Jugendarztpraxen | übernommen im Kinder- und Jugend-Programm nach Einschreibung | zusätzliche Untersuchungen kostenfrei |
| PSA ohne Verdacht | keine reguläre Screening-GKV-Leistung | keine Regelleistung; Bonus ggf. nutzbar | keine reguläre Screening-GKV-Leistung |

BARMER Top-Geldprämie: 50 Punkte entsprechen 10 €, höchstens 1.000 eingelöste Punkte bzw. 200 € pro Jahr. Dieses Budget wird gemeinsam für verschiedene Leistungen verwendet und kann nicht pro Untersuchung erneut angesetzt werden. Teilnahme, erworbene Punkte und Belege sind notwendig. Andere Bonusprogramme und individuelle Verträge können weitere Erstattungen ermöglichen; diese Recherche verspricht dafür keine pauschalen Beträge.

Zuschuss bedeutet: maximal die tatsächlich angefallenen Kosten, begrenzt durch den Euro-Höchstbetrag bzw. den Prozentsatz. Beispiel TK-Sportmedizin, Basisrechnung 100 €: min(80 % × 100 €, 60 €) = 60 € Erstattung, 40 € Eigenanteil. Beispiel TK-PZR 100 €: bis 40 € Erstattung, mindestens 60 € Eigenanteil, sofern der Jahreszuschuss noch nicht verbraucht ist. BARMER-Eigenanteile sind ohne Punktestand und bereits genutztes Jahresbudget nicht berechenbar.

Belege zum Vergleich: '''+links(['gcheck','tkskin','bskin','dskin','tkstatute','dcheck','bcheck','dcolon','tkpzr','tkstatute','bbonus','bstatute','dpzr','dstatute','tksport','dsport','tkchild','bchild','dchild','psa'])+'''

## Zwei Beispielprofile

**Mann, 38:** Anspruch auf Check-up im 3-Jahres-Turnus, Haut-Screening im 2-Jahres-Turnus und einmaliges Hepatitis-B/C-Screening im Check-up prüfen. Zahnkontrolle, Zahnstein/PSI und Impfstatus berücksichtigen. Ohne Vorgeschichte ist keiner dieser Termine automatisch jetzt fällig. Reguläre Prostatafrüherkennung beginnt ab 45, allgemeine Darmkrebsfrüherkennung ab 50. Ein DAK-Darmtest ab 39 ist nur bei passenden Risiken ein Extra; nicht allein wegen des Geburtstags. Kassenextras wie PZR und Sportmedizin getrennt von medizinisch notwendigen Terminen sehen.

**Frau, 60:** Zusätzlich klinische gynäkologische Früherkennung, Brustuntersuchung, HPV-/Pap-Kotest im Dreijahresturnus und Mammographie im Zweijahresturnus prüfen. Darmkrebs: FIT oder Koloskopie, keine parallelen Routinetermine. Influenza, Pneumokokken und Gürtelrose anhand des Impfpasses besprechen. COVID-19 seit der Änderung 2026 regulär ab 75, davor nur bei entsprechender Indikation. RSV regulär erst ab 75, davor 60–74 bei besonderen Voraussetzungen. Die Bauchschlagader-Untersuchung ist kein pauschales Screeningangebot für Frauen.

## Änderungen, Widersprüche und offene Punkte

- Nachprüfung am 13.11.2026: Jede Leistungsangabe verlinkt jetzt ihre Bedingungen mit eigenem Prüfdatum. Die aktuelle Schutzimpfungs-Richtlinie (in Kraft seit 29.10.2026) setzt die COVID-19-Standardimpfung auf 75 Jahre; die frühere 60-Jahre-Angabe wurde korrigiert. Unter 75 bleiben Indikationsimpfungen möglich.
- PZR: BZÄK/KZBV empfehlen sie abhängig vom Bedarf; die Cochrane-Übersicht von 2018 zeigt bei Erwachsenen ohne schwere Parodontitis für routinemäßiges Scaling und Polishing kaum oder keinen Unterschied bei Zahnfleischentzündung und Taschentiefen. Diese begrenzte Studienfrage ist nicht mit sämtlichen PZR-Paketen oder der Parodontitis-Nachsorge gleichzusetzen.

- Seit April 2026 ist das jährliche Lungenkrebs-Screening für geeignete Personen von 50–75 verfügbar: mindestens 25 Rauchjahre und 15 Packungsjahre, aktuell rauchend oder Rauchstopp vor weniger als 10 Jahren. Lange Unterbrechungen und medizinische Eignung benötigen ärztliche Prüfung. Die App berechnet daraus keine autonome CT-Empfehlung. '''+links(['lung','lungrisk'])+'''
- Mammographie für 45–49 wurde am 15.10.2026 beschlossen. Start ist vorbehaltlich Prüfung voraussichtlich 01.08.2027; am Recherchestichtag gilt weiterhin 50 bis einschließlich 75. Die App schaltet diese Erweiterung auch in Zukunft nicht ohne redaktionell geprüften Datenstand als sicheren Anspruch frei. '''+links(['mammo45','mammo'])+'''
- Prostata: Die bestehende GKV-Leistung umfasst die Tastuntersuchung; die aktuelle S3-Leitlinie empfiehlt diese zur Früherkennung nicht mehr. PSA soll nach Beratung auf Wunsch angeboten werden, ist aber keine allgemeine GKV-Screeningleistung. Medizinische Empfehlung und Bezahlung laufen hier auseinander. '''+links(['prostate','psa'])+'''
- Eine G-BA-Themenseite zur Mammographie nennt im Fließtext die Tastuntersuchung ab 20. Die aktuelle Übersicht und Richtlinie nennen die zusätzliche Brustuntersuchung ab 30; implementiert ist 30. '''+links(['overview','kfe'])+'''
- Kassenextras unterliegen regionalen Verträgen, Praxisqualifikation, Risikofaktoren und Budgetbedingungen. „Nicht bestätigt“ wird nicht in „0 %“ umgedeutet. Vorsorge außerhalb des Altersfensters ist von diagnostischer Abklärung bei Beschwerden zu trennen.
- Die Raucherangaben sind freiwillig. Unbekannte Werte werden nicht als Null interpretiert. Beim Rauchstopp berücksichtigt die Vorschau verstrichene Jahre; fortgesetztes Rauchen und Änderungen des Gesundheitszustands werden nicht vorausgesagt.

## Longevity und Länderblick

'''
for a in c['knowledge']:
    text+=f"### {a['title']}\n\n{a['body']} {links(a['sources'])}\n\n"
text+="""## Schwangerschaft nach Wochen statt Lebensjahren

Im Profil ist die freiwillige Angabe „Ich bin schwanger“ bei weiblichen bzw. individuell angepassten Profilen verfügbar. Der optionale errechnete Geburtstermin entspricht 40+0 SSW; daraus werden Wochen und Datumsfenster berechnet. Ohne Termin gibt es nur die allgemeinen Zeitangaben. Die Praxis bestimmt bzw. korrigiert den Termin. Schwangerschaftsvorsorge wird nicht in die mehrjährige Altersvorschau fortgeschrieben.

| Thema | Regelzeit / Voraussetzung |
|---|---|
| Regelmäßige Betreuung | üblicherweise vierwöchig, ab SSW 32 zweiwöchig; individuell abweichend |
| Erstuntersuchung, Mutterpass, Infektionsscreenings | möglichst früh |
| Basis-Ultraschall 1 / 2 / 3 | 8+0–11+6 / 18+0–21+6 / 28+0–31+6 |
| Erneuter Antikörpersuchtest | 23+0–26+6 |
| Schwangerschaftsdiabetes | 24+0–27+6 |
| Fetaler Rhesusfaktor | ab 11+0 bei RhD-negativer Mutter und geeigneter Situation |
| Anti-D | 27+0–29+6 nur bei erfüllten serologischen Voraussetzungen |
| Pertussis | frühes drittes Trimenon in jeder Schwangerschaft, bei Frühgeburtsrisiko früher |
| Influenza | ab zweitem Trimenon, bei zusätzlichen Risiken schon im ersten, saisonal |
| NIPT auf Trisomien | persönliche begründete Situation, kein Routineprogramm allein wegen des Alters |

Die regulären Mutterschaftsleistungen übernimmt die GKV bei passender Indikation. Für zusätzliche Toxoplasmose-/B-Streptokokkentests unterscheidet die App: TK: Toxoplasmose ohne Verdacht privat; für B-Streptokokken keine konkrete Zusage aus der geprüften TK-Seite ableitbar. BARMER Familie Plus: bis 200 € **gemeinsames** Budget pro Schwangerschaft, auch mit Neugeborenen-Osteopathie geteilt; individueller Anlass, Toxoplasmose bei besonderer Infektionsgefahr, B-Streptokokken in SSW 35–37, Belege bis 31.03. des Folgejahres. DAK § 19a: individueller Anlass, Toxoplasmose bei besonderer Infektionsgefahr, B-Streptokokken in SSW 35–37; Rechnung und Schwangerschaftsnachweis. Ein historisches 500-€-Gesamtbudget von MamaPLUS wurde nicht als aktuelle Pauschalzusage übernommen: die aktuelle DAK-Seite nennt 500 € ausdrücklich für Hebammenrufbereitschaft, während die einzelnen Testseiten die Testübernahme beschreiben.

Abgeschlossene Untersuchungen gehören zu einer Schwangerschaftsepisode. Wird eine Schwangerschaft beendet und später eine neue angelegt, erfüllen alte Untersuchungen nicht die neuen Zeitfenster. Verstrichene Fenster ohne Eintrag bleiben „mit Praxis klären“; eine Geburt wird nicht aus dem errechneten Termin abgeleitet. Eine eigenständige Wochenbettplanung ist nicht enthalten.

"""+links(['maternity','pregnancy','pregschedule','pregscreen','nipt','pregvax','pregflu','pregtk','pregbarmer','pregdaktoxo','pregdakgbs'])+'\n\n'
text+='## Einzelne Angebote mit Quellen\n\n'
for r in c['rules']:
    text+=f"### {r['title']}\n\n{r['summary']}. {r['detail']}\n\nGrenzen: {r['harm']}\n\n**Empfehlung / fachliche Grundlage: {r['recommendationBy']}**\n\n{r['recommendation']}\n\n{links(r['recommendationSources'])}\n\n**Leistungsgrundlagen:** {links(r['coverageSources'])}\n\nWeitere Hintergrundquellen: {links(r['sources'])}\n\n"
text+='''## Grenzen dieser ersten Version

- Der ergänzte Schwangerschaftsplan enthält reguläre Zeitfenster, ersetzt jedoch keinen vollständigen Mutterpass oder individuellen Betreuungsplan. Kein vollständiger Impf-Dosis-, genetischer Hochrisiko- oder Nachsorgeplan. Kinderfenster sind ergänzende Orientierung; Toleranzgrenzen und gesonderte Neugeborenen-Screenings müssen über das Untersuchungsheft geprüft werden.
- Extras sind der konkret recherchierte relevante Ausschnitt, kein vollständiger Leistungskatalog aller Selektivverträge und Bonusvarianten. Nicht enthalten sind z. B. individuelle Reiseimpfungen, alle Präventionskursbudgets und sämtliche Varianten der Schwangerschaftsextras.
- Keine ärztliche Validierung oder Medizinprodukt-Zertifizierung. Regeltests prüfen die Umsetzung des recherchierten Katalogs, nicht klinische Wirksamkeit.
- Datenstand ist in der App sichtbar. Keine automatische Aktualisierung; Quellen vor künftigen Terminen prüfen. Nach sechs Monaten wird eine zusätzliche Aktualitätsmeldung angezeigt.
'''
(root/'research/RECHERCHE.md').write_text(text)
lines=['# Altersatlas: jedes Jahr von 0 bis 110','', 'Stand 13.11.2026. Angebote, keine jährlichen Pflichten. Wiederholungen hängen vom letzten Termin ab. Geschlechtsbezogene Standardprofile; Anatomie und persönliche Risiken können abweichen. Kinderuntersuchungen gelten nur in ihren konkreten Monats-/Tagesfenstern. Impfpass prüfen gilt durchgängig. Quellen und Details stehen in RECHERCHE.md und im App-Katalog.','', '| Alter | Männer / Jungen | Frauen / Mädchen |', '|---:|---|---|']
for age in range(111):
    cells=[]
    for sex in ['MALE','FEMALE']:
        rs=[]
        for r in c['rules']:
            if r.get('pregnancy'):continue
            if r['sex'] not in ('ALL',sex):continue
            if r['organ'] in ('cervix','breast') and sex!='FEMALE':continue
            if r['organ']=='prostate' and sex!='MALE':continue
            if not r['minAge']<=age<=r['maxAge']:continue
            if r.get('startMonths',-1)>=0:
                if r.get('startDays',-1)>=0 and age!=0:continue
                if r.get('startDays',-1)<0 and not (r['startMonths']<12*(age+1) and r['endMonths']>12*age):continue
            label=r['title']+' ('+r['summary']+')'
            if r['risk']:label+=' [Risiko prüfen]'
            if r['kind']=='EXTRA':label+=' [Kassenextra]'
            rs.append(label)
        cells.append('<br>'.join(rs))
    lines.append(f"| {age} | {cells[0]} | {cells[1]} |")
(root/'research/ALTERSATLAS.md').write_text('\n'.join(lines)+'\n')
print('Research and 111-year atlas written.')
