package de.morgenklar.app

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class Status(val label: String) { CHECK("Termin prüfen"), DUE("Fällig laut Eintrag"), CURRENT("Noch nicht fällig"), COMPLETE("Dokumentiert"), FUTURE("Kommt hinzu"), CONDITION("Voraussetzung prüfen"), CHOICE("Alternative wählen"), ADVICE("Individuell besprechen"), PLANNED("Angekündigt") }
data class PlanItem(val rule: Rule, val status: Status, val note: String, val date: LocalDate? = null)
object Engine {
    fun matches(r: Rule,p: Person): Boolean = (!r.pregnancy || p.pregnant) && (r.sex == "ALL" || r.sex == p.sex) && when(r.organ) {
        "cervix" -> p.cervix; "breast" -> p.breast; "prostate" -> p.prostate; else -> true
    }
    fun start(r: Rule,p: Person): LocalDate = when {
        r.pregnancy -> p.dueDate?.minusDays(280)?.plusDays(r.pregStartDay.toLong()) ?: p.birth
        r.startDays >= 0 -> p.birth.plusDays(r.startDays.toLong())
        r.startMonths >= 0 -> p.birth.plusMonths(r.startMonths.toLong())
        else -> p.birth.plusYears(r.minAge.toLong())
    }
    fun end(r: Rule,p: Person): LocalDate = when {
        r.pregnancy -> p.dueDate?.minusDays(280)?.plusDays(r.pregEndDay.toLong()) ?: p.birth.plusYears(121)
        r.endDays >= 0 -> p.birth.plusDays(r.endDays.toLong()).minusDays(1)
        r.endMonths >= 0 -> p.birth.plusMonths(r.endMonths.toLong()).minusDays(1)
        else -> p.birth.plusYears(r.maxAge.toLong()+1).minusDays(1)
    }
    fun next(r: Rule, last: LocalDate): LocalDate? = when {
        r.id=="flu" -> LocalDate.of(last.year+1,9,1)
        r.id=="covid" -> maxOf(LocalDate.of(last.year+1,9,1),last.plusMonths(6))
        else -> when(r.interval) {
        "Y1" -> LocalDate.of(last.year+1,1,1)
        "Y2" -> LocalDate.of(last.year+2,1,1)
        "Y3" -> LocalDate.of(last.year+3,1,1)
        "Y10" -> LocalDate.of(last.year+10,1,1)
        "M12" -> last.plusMonths(12)
        "M24" -> last.plusMonths(24)
        "M120" -> last.plusYears(10)
        "HALF" -> maxOf(if(last.monthValue <= 6) LocalDate.of(last.year,7,1) else LocalDate.of(last.year+1,1,1),last.plusMonths(4))
        else -> null
        }
    }
    fun evaluate(r: Rule,p: Person,on: LocalDate): PlanItem {
        if(r.pregnancy) return Pregnancy.evaluate(r,p,on)
        val since=start(r,p)
        if(on.isBefore(since)) return PlanItem(r,Status.FUTURE,"Ab ${fmt(since)} · ${r.summary}",since)
        val history=p.entries.filter { it.rule==r.id && !it.date.isAfter(on) && it.date>=since }.sortedBy { it.date }
        val last=history.lastOrNull()?.date
        if(r.kind=="PLANNED") return PlanItem(r,Status.PLANNED,"Voraussichtlich 01.08.2027 · Startstatus vor Buchung prüfen")
        if(p.followup) return PlanItem(r,Status.ADVICE,"Individueller Nachsorgeplan hat Vorrang; keine automatische Fälligkeit.")
        if(r.id=="colo" || r.id=="fit") {
            val colos=p.entries.filter { it.rule=="colo" && !it.date.isAfter(on) }.sortedBy { it.date }
            val recent=colos.lastOrNull()?.date
            if(recent != null) {
                val resume=LocalDate.of(recent.year+10,1,1)
                if(r.id=="colo" && (colos.size>=2 || p.age(recent)>=65)) return PlanItem(r,Status.COMPLETE,"Regulärer Koloskopieanspruch dokumentiert ausgeschöpft. Nachsorge bei Befunden separat.")
                if(on.isBefore(resume)) return PlanItem(r,Status.CURRENT,"Nach Vorsorgekoloskopie regulär frühestens ${fmt(resume)}. Auffällige Befunde ausgenommen.",resume)
            }
            if(p.colonChoice=="UNDECIDED") return PlanItem(r,Status.CHOICE,"Stuhltest ODER Darmspiegelung wählen; gemeinsam Nutzen und Risiken abwägen.")
            if((r.id=="fit" && p.colonChoice=="COLO") || (r.id=="colo" && p.colonChoice=="FIT")) return PlanItem(r,Status.ADVICE,"Alternative zum gewählten Verfahren, kein zusätzlicher Routine-Termin.")
        }
        if(r.risk.isNotEmpty()) {
            val eligible=when(r.risk) {
                "lung" -> p.smoking in listOf("CURRENT","FORMER") && (p.smokeYears?:-1)>=25 && (p.packYears?:-1.0)>=15.0 && (p.smoking=="CURRENT" || (p.quitYears!=null && p.quitYears+java.time.Period.between(p.smokingAsOf,on).years<10))
                "chronic" -> p.chronic
                "family" -> p.family
                else -> false
            }
            if(!eligible) return PlanItem(r,Status.CONDITION,when(r.risk) {
                "lung" -> "Nur bei passenden Rauchkriterien; Profilangaben fehlen oder erfüllen sie nicht."
                "sexual" -> "Nur nach Beginn sexueller Aktivität; nicht aus dem Alter ableitbar."
                else -> "Nur bei passenden Risiken; bisher keine passende Angabe im Profil."
            })
            if(r.risk=="lung") return PlanItem(r,Status.CONDITION,"Rauchangaben passen zu den Schwellen. Unterbrechungen und medizinische Eignung ärztlich prüfen" + (last?.let { "; letzter Eintrag ${fmt(it)}." } ?: "."),last?.plusYears(1))
        }
        if(r.interval=="NONE") return PlanItem(r,Status.ADVICE,last?.let { "Zuletzt dokumentiert: ${fmt(it)}. Individuelles Intervall." } ?: "Kein pauschaler jährlicher Termin.")
        if(r.coverage=="SKIN" && Coverage.forRule(r,p,on).type=="UNKNOWN") return PlanItem(r,Status.CONDITION,"Regionale Altersgrenze und Vertragspraxis bestätigen lassen.")
        if(last==null) return PlanItem(r,Status.CHECK,"Keine Untersuchung dokumentiert. Letzten Termin prüfen, bevor du neu buchst.")
        if(r.interval=="ONCE") return PlanItem(r,Status.COMPLETE,"Erledigt am ${fmt(last)}. Bei Impfserien muss die ganze Serie abgeschlossen sein.")
        val due=next(r,last) ?: return PlanItem(r,Status.ADVICE,"Intervall mit Praxis klären.")
        return PlanItem(r,if(due<=on) Status.DUE else Status.CURRENT,"Letzter Eintrag ${fmt(last)} · regulär ab ${fmt(due)}",due)
    }
    fun plan(c: Catalog,p: Person,on: LocalDate): List<PlanItem> {
        val yearEnd=LocalDate.of(on.year,12,31)
        return c.rules.filter { !it.pregnancy && matches(it,p) && end(it,p)>=on && start(it,p)<=yearEnd }.map { evaluate(it,p,on) }
    }
    fun atlas(c: Catalog,p: Person,age: Int): List<Rule> {
        val a=p.birth.plusYears(age.toLong()); val b=a.plusYears(1).minusDays(1)
        return c.rules.filter { !it.pregnancy && matches(it,p) && start(it,p)<=b && end(it,p)>=a }
    }
    fun validEntry(r: Rule,p: Person,date: LocalDate,now: LocalDate): Boolean = date>=p.birth && date<=now && date>=start(r,p) && date<=end(r,p) && p.entries.none { it.rule==r.id && it.date==date && (!r.pregnancy || it.episode==p.pregnancyId) }
}
fun fmt(d: LocalDate): String = d.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
val insurers = linkedMapOf("NONE" to "Ohne Angabe", "TK" to "Techniker", "BARMER" to "BARMER", "DAK" to "DAK-Gesundheit")
data class Benefit(val type: String,val label: String,val detail: String,val sources: List<String> = emptyList())
object Coverage {
    fun forRule(r: Rule,p: Person,on: LocalDate): Benefit {
        val age=p.age(on); val k=p.insurer
        if(r.pregnancy) return Pregnancy.coverage(r,p)
        if(r.kind=="PLANNED") return Benefit("UNKNOWN","Noch kein Regelanspruch","Erweiterung angekündigt; endgültigen Startstatus prüfen.",r.coverageSources)
        if(age<r.minAge || age>r.maxAge) return Benefit("UNKNOWN","Außerhalb Angebotsfenster","Dieses App-Angebot ist für ${r.minAge} bis ${r.maxAge} Jahre erfasst. Weitere Ansprüche, Kassenextras oder medizinisch begründete Diagnostik sind dadurch nicht ausgeschlossen; Bedingungen mit der Kasse prüfen.",r.coverageSources)
        if(r.coverage=="STANDARD" && (r.risk.isNotEmpty() || (r.organ in listOf("cervix","breast") && p.sex!="FEMALE") || (r.organ=="prostate" && p.sex!="MALE"))) return Benefit("CONDITIONAL","GKV: Voraussetzungen prüfen","Persönliche Eignung, relevante Anatomie und konkreten Anspruch mit der Praxis klären; keine automatische Zusage allein aufgrund der Profilangabe.",r.coverageSources)
        if(r.coverage=="STANDARD") return Benefit("FULL","GKV-Regelleistung","Die in der verlinkten Richtlinie vorgesehene Leistung wird bei erfülltem Alter, Intervall und weiteren Voraussetzungen über die Gesundheitskarte abgerechnet. Privat vereinbarte Extras sind nicht eingeschlossen.",r.coverageSources)
        if(r.coverage=="CONDITIONAL") return Benefit("CONDITIONAL","GKV: bei Indikation","Nach aktueller Schutzimpfungs-Richtlinie und ärztlich bestätigten Voraussetzungen. Keine Zusage für beliebige Impfungen oder Tests.",r.coverageSources)
        if(k=="NONE") return Benefit("UNKNOWN","Kasse ergänzen","Kein allgemeiner GKV-Regelanspruch für diese Zusatzleistung. Mit der Kasse lassen sich Extras genauer zuordnen.",r.coverageSources)
        return when(r.coverage) {
            "SKIN" -> when(k) {
                "TK" -> if(age>=15 || p.region in listOf("Bayern","Hessen","Schleswig-Holstein")) Benefit("FULL","Vertrag: 100 %","Alle 24 Monate in teilnehmender Hautarztpraxis. Regionale Vertragsbedingungen prüfen.",listOf("tkskin")) else Benefit("UNKNOWN","Region prüfen","Unter 15 nur in bestimmten Ländern; Bundesland und Praxis klären.",listOf("tkskin"))
                "BARMER" -> Benefit("CONDITIONAL","Vertrag: 100 %","Unter 35 alle zwei Jahre bei erfüllten regionalen Alters- und Vertragsbedingungen. Teilnahme der Praxis vorab klären.",listOf("bskin","bstatute"))
                else -> if(age>=17) Benefit("PARTIAL","Bis 60 € / 2 Jahre","17–34 Jahre, nur bei bestehenden Risikofaktoren und qualifizierter Praxis (§ 19 Abs. 3). Rechnung einreichen. Über 60 € bleibt ein Eigenanteil.",listOf("dskin","dstatute")) else Benefit("UNKNOWN","Kein bestätigtes Extra","DAK-Zuschuss in dieser Recherche erst ab 17 belegt. Bei auffälligen Hautstellen handelt es sich um Abklärung.",listOf("dskin","dstatute"))
            }
            "CHECK" -> when(k) {
                "TK" -> if(age>=18) Benefit("PARTIAL","Bis 35 € / 3 Jahre","18–34 bei bestehenden Risiken, spezifizierte Rechnung mit Risikofaktoren; § 27n. Einmaligen GKV-Check vorher nutzen.",listOf("tkstatute")) else Benefit("UNKNOWN","Kein bestätigtes Extra","TK-Satzungsleistung hier erst ab 18 belegt.",listOf("tkstatute"))
                "DAK" -> Benefit("PARTIAL","Bis 40 € / 2 Jahre","17–34, mindestens ein Risikofaktor. Je zwei Kalenderjahre, tatsächliche Rechnungskosten bis zum Höchstbetrag.",listOf("dcheck","dstatute"))
                else -> Benefit("UNKNOWN","Kein pauschales Extra belegt","Einmaliger GKV-Check 18–34 ist bezahlt. Keine zusätzliche regelmäßige Erstattung aus der geprüften Leistungsseite ableitbar.",listOf("bcheck"))
            }
            "FIT" -> if(k=="DAK") Benefit("PARTIAL","Bis 40 € / 2 Jahre","39–49 bei erhöhtem Risiko, ärztlicher Stuhltest, Rechnung einreichen. Individuelle Risikovorsorge separat planen.",listOf("dcolon","dstatute")) else Benefit("UNKNOWN","Kein pauschales Extra belegt","Bei familiärem Risiko medizinisch begründete frühere Diagnostik mit der Praxis klären. Keine pauschale Ablehnung der Kostenübernahme.",r.coverageSources)
            "PZR" -> when(k) {
                "TK" -> Benefit("PARTIAL","Zuschuss: bis 40 € / Jahr","Keine GKV-Regelleistung. TK ab 18: einmal je Kalenderjahr bis 40 € (§ 27o), höchstens die tatsächlichen Kosten. Rechnung nach Bezahlung mit PZR/GOZ 1040 einreichen. Bei 100 € Rechnung bleiben mindestens 60 € Eigenanteil; ein bereits genutzter Zuschuss steht nicht erneut zur Verfügung.",listOf("tkpzr","tkstatute"))
                "DAK" -> Benefit("PARTIAL","Zuschuss: bis 60 € / Jahr","Keine GKV-Regelleistung. DAK: bis 60 € je Kalenderjahr (§ 19 Abs. 6), höchstens die tatsächlichen Kosten bei zugelassenen Leistungserbringern. Antrag für Vorjahresrechnungen bis 31.03. des Folgejahres. Bei 100 € Rechnung bleiben mindestens 40 € Eigenanteil. Besondere Schwangerschaftsleistung separat prüfen.",listOf("dpzr","dstatute"))
                else -> bonus()
            }
            "SPORT" -> when(k) {
                "TK" -> Benefit("PARTIAL","80 %, bis 60 / 120 €","Alle zwei Jahre: Basis höchstens 60 €, erweitert insgesamt höchstens 120 €. Ärztlich bestätigte Notwendigkeit, bei Erweiterung Risiken; zugelassene Sportmedizinpraxis.",listOf("tkstatute"))
                "DAK" -> Benefit("PARTIAL","Bis 60 / 120 €","Alle zwei Jahre bis 60 €, bei nachgewiesenen Risiken und erweiterter Untersuchung insgesamt bis 120 €. Bescheinigung und Rechnung einer qualifizierten Praxis.",listOf("dsport","dstatute"))
                else -> bonus()
            }
            "CHILD" -> when(k) {
                "TK" -> Benefit("FULL","Vertragsleistung","U10/U11/J2 im Altersfenster über Gesundheitskarte in Kinder- und Jugendarztpraxen.",listOf("tkchild"))
                "BARMER" -> Benefit("CONDITIONAL","Programm: 100 %","Teilnahme am Kinder- und Jugend-Programm und Vertragspraxis erforderlich.",listOf("bchild"))
                else -> Benefit("CONDITIONAL","Vertragsleistung prüfen","U10/U11/J2 zusätzlich kostenfrei; Teilnahme und Praxis vorab klären.",listOf("dchild"))
            }
            "PRIVATE" -> if(k=="BARMER") Benefit("BONUS","Privat / Bonus möglich","Kein regulärer Screeninganspruch. Bonusbudget kann für Krebsfrüherkennungsleistungen nutzbar sein, Bedingungen vorher bestätigen. Bei Verdacht andere Abrechnung.",listOf("psa","bbonus","bstatute")) else Benefit("PRIVATE","Regulär selbst bezahlt","PSA ohne Verdacht ist keine reguläre GKV-Früherkennung. Sonderprogramme/Bonus vorher erfragen; medizinisch begründete Diagnostik ist getrennt zu beurteilen.",listOf("psa"))
            else -> Benefit("UNKNOWN","Vorab klären","Für dieses Extra liegt keine verlässliche pauschale Zusage vor.")
        }
    }
    private fun bonus() = Benefit("BONUS","Bonusbudget: bis 200 €","BARMER Top-Geldprämie: 50 Punkte = 10 €, max. 1.000 eingelöste Punkte/Jahr. Gemeinsam genutztes Budget für alle Bonusleistungen, kein eigener 200-€-Anspruch je Untersuchung. Teilnahme, Punkte und Rechnung erforderlich. Ohne verfügbare Punkte selbst bezahlen; zusätzliche Schwangerschaftsleistungen nach § 28d separat prüfen.",listOf("bbonus","bstatute"))
}
