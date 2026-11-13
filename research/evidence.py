"""Manually reviewed provenance. Dates change only after checking the source.

Coverage sources establish entitlement, not a medical recommendation. Sources
without an individual check date retain the separately labelled old catalogue date.
"""
CHECKED = '2026-11-13'

NEW_SOURCES = {
    'gurl': ('G-BA · Gesundheitsuntersuchungs-Richtlinie', 'https://www.g-ba.de/richtlinien/10/', 'In Kraft seit 12.02.2021'),
    'okferl': ('G-BA · Organisierte Krebsfrüherkennung: Richtlinie', 'https://www.g-ba.de/richtlinien/104/', 'In Kraft seit 02.10.2026'),
    'sir': ('G-BA · Schutzimpfungs-Richtlinie, Anlage 1', 'https://www.g-ba.de/richtlinien/60/', 'In Kraft seit 29.10.2026'),
    'maternityrl': ('G-BA · Mutterschafts-Richtlinie', 'https://www.g-ba.de/richtlinien/19/', 'In Kraft seit 25.08.2026'),
    'childrl': ('G-BA · Kinder-Richtlinie (U1–U9)', 'https://www.g-ba.de/richtlinien/15/', 'In Kraft seit 20.08.2026'),
    'youthrl': ('G-BA · Jugendgesundheitsuntersuchungs-Richtlinie', 'https://www.g-ba.de/richtlinien/14/', 'In Kraft seit 01.01.2017'),
    'childdentalrl': ('G-BA · Zahnärztliche Früherkennungs-Richtlinie', 'https://www.g-ba.de/richtlinien/29/', 'In Kraft seit 01.01.2026'),
    'chlamydiarl': ('G-BA · Chlamydien-Screening: Richtlinie, Abschnitt B', 'https://www.g-ba.de/richtlinien/9/', 'In Kraft seit 24.09.2025'),
    'dentalgba': ('G-BA · Zahnvorsorge: Leistungen und Richtlinien', 'https://www.g-ba.de/themen/zahnaerztliche-versorgung/prophylaxe-frueherkennung/', ''),
    'bstatute': ('BARMER · Satzung: §§ 28d, 28f, 37 und Bonus-Anlage', 'https://www.barmer.de/resource/blob/1022552/90bdcd25998b6738d7ea10183a0a00cf/barmer-satzung-der-barmer-barrierefrei-19-data.pdf', '39./40. Nachtrag · Stand 15.10.2026'),
    'dstatute': ('DAK · Satzung: §§ 19 und 19a', 'https://caas.content.dak.de/caas/v1/media/227064/data/a7fdcf5826965a284c8e25e049c6dfbf/satzung-der-dak-gesundheit-vom-01-07-2016-in-der-fassung-67-nachtrag-stand-01-09-2026.pdf', '67. Nachtrag · Stand 01.09.2026'),
    'dpzr': ('DAK · PZR: Höhe des Zuschusses', 'https://www.dak.de/dak/ihr-anliegen/zaehne/professionelle-zahnreinigung/hoehe-zuschuss-pzr-dak_15030', 'Seitenstand 01.10.2026'),
    'pzrprofession': ('Bundeszahnärztekammer · Empfehlung zur PZR', 'https://www.bzaek.de/service/positionen-statements/einzelansicht/die-professionelle-zahnreinigung-zur-prophylaxe-von-zahnkaries-und-parodontalerkrankungen.html', ''),
    'pzrreview': ('Cochrane Oral Health · Studien zu Scaling und Polishing', 'https://www.cochrane.org/evidence/CD004625_routine-scale-and-polish-periodontal-health-adults', 'Review 2018 · Recherche bis 10.01.2018'),
    'covidstiko': ('BIÖG · STIKO-Empfehlung zur COVID-19-Impfung', 'https://www.infektionsschutz.de/impfen/fuer-erwachsene/corona-impfung-bei-erwachsenen', 'STIKO-Änderung Juli 2026'),
}

# Source-specific checks, never inferred from the current date or a build time.
VERIFIED = {
    'kfe': 'In Kraft seit 12.03.2026', 'tkpzr': 'Seitenstand 01.07.2026',
    'tkstatute': '§ 27e Sportmedizin · § 27n Risikovorsorge · § 27o PZR',
    'dental': 'Seitenstand Juni 2025', 'psi': '', 'prostate': 'Version 8.0 · Juli 2025',
    'vaccines': 'Impfkalender 2026; COVID-19-Änderung Juli 2026 separat beachten',
    'tkskin': 'Seitenstand 20.07.2026', 'bskin': '', 'dskin': 'Seitenstand 01.10.2026',
    'dcheck': '', 'dcolon': '', 'dsport': '', 'bcheck': '', 'bbonus': '',
    'tkchild': 'Seitenstand 14.07.2025', 'bchild': '', 'dchild': '',
    'pregtk': 'Seitenstand 29.04.2026', 'pregbarmer': '',
    'pregdaktoxo': 'Seitenstand 01.10.2026', 'pregdakgbs': 'Seitenstand 01.10.2026',
    'nipt': '', 'mammo45': 'Beschlussmitteilung 15.10.2026', 'psa': 'Mitteilung 16.10.2025',
    'j1': '', 'maternity': 'In Kraft seit 25.08.2026',
}

def enrich(sources, rules):
    for key, (title, url, _) in NEW_SOURCES.items():
        sources[key] = (title, url)
    # Use the current consolidated rule instead of the old April 2025 version.
    sources['colonrl'] = ('G-BA · Darm-/Gebärmutterhals-Screening, Fassung Oktober 2026',
        'https://www.g-ba.de/downloads/62-492-4233/oKFE-RL-2026-08-20-iK-2026-10-02.pdf')
    VERIFIED['colonrl'] = 'In Kraft seit 02.10.2026'
    legal = {
        **dict.fromkeys(['check18', 'check35', 'hep', 'aaa'], ['gurl']),
        **dict.fromkeys(['skin', 'gyn', 'breast', 'mammo', 'prostate', 'lung'], ['kfe']),
        **dict.fromkeys(['pap', 'cotest', 'fit', 'colo'], ['okferl']),
        **dict.fromkeys(['dental', 'tartar'], ['dental', 'dentalgba']),
        'psi': ['psi', 'dental', 'dentalgba'], 'childteeth': ['childdentalrl'],
        'j1': ['youthrl'], 'chlamydia': ['chlamydiarl'], 'mammo45': ['mammo45'],
        'skinextra': ['tkskin', 'bskin', 'dskin'], 'checkextra': ['tkstatute', 'bcheck', 'dcheck'],
        'fitextra': ['dcolon', 'dstatute'], 'pzr': ['dental', 'tkpzr', 'dpzr', 'bbonus'],
        'sport': ['tkstatute', 'dsport', 'bbonus'], 'psaextra': ['psa', 'kfe'],
        **dict.fromkeys(['u10', 'u11', 'j2'], ['tkchild', 'bchild', 'dchild']),
        'pregtoxo': ['maternityrl', 'pregtk', 'bstatute', 'dstatute'],
        'preggbs': ['maternityrl', 'bstatute', 'dstatute'],
    }
    vaccine_ids = {'vaccinereview', 'td', 'hpv', 'flu', 'covid', 'pneumo', 'zoster', 'rsv', 'rsvrisk', 'pregpertussis', 'pregflu'}
    for r in rules:
        key = r['id']
        if key in vaccine_ids:
            r['coverageSources'] = ['sir']
            r['recommendationBy'] = 'STIKO am Robert Koch-Institut'
            r['recommendation'] = 'Die STIKO empfiehlt Impfungen nach Alter, Vorimpfungen und konkreten Risiken. Der hier beschriebene Anlass muss zu dir passen; die Praxis prüft Impfstoff und Zeitpunkt. Den GKV-Anspruch regelt gesondert die Schutzimpfungs-Richtlinie.'
            r['recommendationSources'] = ['vaccines', 'sir']
        elif r.get('pregnancy') and r['coverage'] != 'PREG_EXTRA':
            r['coverageSources'] = ['maternityrl']
        elif key in {'u1','u2','u3','u4','u5','u6','u7','u7a','u8','u9'}:
            r['coverageSources'] = ['childrl']
        else:
            r['coverageSources'] = legal[key]
        if 'recommendationBy' not in r:
            r['recommendationBy'] = 'Gemeinsamer Bundesausschuss (G-BA) · Vorsorgeprogramm'
            r['recommendation'] = 'Der G-BA legt dieses Untersuchungsangebot und seine Voraussetzungen in der verlinkten Grundlage fest. Das ist eine Regelung des Versorgungsprogramms, keine persönliche Empfehlung zur Teilnahme. Nutzen und mögliche Nachteile gemeinsam mit der Praxis abwägen.'
            r['recommendationSources'] = r['coverageSources']
        if r['kind'] == 'EXTRA':
            r['recommendationBy'] = 'Informationen der genannten Krankenkassen · Zusatzangebot'
            r['recommendation'] = 'Dieses Angebot wird hier aufgrund der verlinkten Kassenbedingungen gezeigt. Eine eigenständige fachliche Empfehlung für eine routinemäßige Untersuchung aller Personen dieses Alters wird damit nicht behauptet. Den individuellen Bedarf mit der Praxis besprechen.'
            r['recommendationSources'] = r['coverageSources']
        if key in {'dental', 'psi', 'tartar'}:
            r['recommendationBy'] = 'Kassenzahnärztliche Bundesvereinigung (KZBV)'
            r['recommendation'] = 'Die KZBV beschreibt regelmäßige Kontrollen, Zahnsteinentfernung bei Bedarf und den PSI zur Früherkennung von Zahnfleischerkrankungen. Der konkrete Bedarf kann vom regelmäßigen Kassenintervall abweichen; eine PZR ist eine andere Leistung.'
            r['recommendationSources'] = ['dental', 'psi']
        if key in {'prostate', 'psaextra'}:
            r['recommendationBy'] = 'Leitlinienprogramm Onkologie · S3-Leitlinie Prostatakarzinom'
            r['recommendation'] = 'Version 8.0, Empfehlungen 4.1–4.4: keine rektale Tastuntersuchung zur Früherkennung; PSA nach ergebnisoffener Beratung auf Wunsch ab 45 bei mindestens zehn Jahren erwarteter Lebenszeit. Die Leitlinienempfehlung ist nicht mit dem GKV-Leistungskatalog identisch.'
            r['recommendationSources'] = ['prostate']
        if key == 'pzr':
            r['recommendationBy'] = 'Bundeszahnärztekammer (BZÄK); ergänzend KZBV'
            r['recommendation'] = 'Die BZÄK empfiehlt bei gesunden Zähnen und gesundem Zahnfleisch eine PZR ein- bis zweimal jährlich, mit individuell angepasstem Abstand. Auch die KZBV nennt ein bis zwei Termine nach Bedarf. Einordnung: Cochrane fand 2018 bei regelmäßig zahnärztlich betreuten Erwachsenen ohne schwere Parodontitis für routinemäßiges Scaling und Polishing kaum oder keinen Unterschied bei Zahnfleischentzündung und Taschentiefen über zwei bis drei Jahre. Das untersuchte Verfahren bildet nicht jedes deutsche PZR-Paket ab und die Ergebnisse gelten nicht pauschal für Parodontitis-Nachsorge. Bedarf und Häufigkeit persönlich besprechen.'
            r['recommendationSources'] = ['pzrprofession', 'dental', 'pzrreview']
        if key == 'pregnipt':
            r['recommendation'] = 'Der G-BA beschreibt den NIPT ausdrücklich als Einzelfallleistung, nicht als Routineempfehlung nach Alter. Qualifizierte Beratung und eine informierte persönliche Entscheidung sind erforderlich.'
            r['recommendationSources'] = ['nipt', 'maternityrl']
        if key == 'mammo45':
            r['recommendation'] = 'Der G-BA hat die Erweiterung beschlossen, aber am Prüfdatum gilt für 45–49-Jährige noch kein regulärer Anspruch. Die Ankündigung ist keine Aufforderung, das Screening bereits privat durchführen zu lassen.'
        if key == 'covid':
            r['recommendationSources'] = ['covidstiko', 'sir']

    reviewed = VERIFIED | {key: value[2] for key, value in NEW_SOURCES.items()}
    return [dict(id=key, title=value[0], url=value[1],
                 **(dict(checked=CHECKED, edition=reviewed[key]) if key in reviewed else {}))
            for key, value in sources.items()]
