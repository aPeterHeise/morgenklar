package de.morgenklar.app

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object Pregnancy {
    fun day(p:Person,on:LocalDate):Long? = p.dueDate?.let { ChronoUnit.DAYS.between(it.minusDays(280),on) }
    fun stage(p:Person,on:LocalDate):String {
        val d=day(p,on) ?: return "Geburtstermin noch offen"
        if(d !in 0..294) return "Schwangerschaftsstatus prüfen"
        return "SSW ${d/7}+${d%7} · ${d/7+1}. Schwangerschaftswoche"
    }
    fun evaluate(r:Rule,p:Person,on:LocalDate):PlanItem {
        if(!p.pregnant) return PlanItem(r,Status.CONDITION,"Keine aktuelle Schwangerschaft im Profil.")
        val history=p.entries.filter { it.rule==r.id && it.episode==p.pregnancyId && it.date<=on }
        val last=history.maxByOrNull { it.date }?.date
        if(p.followup) return PlanItem(r,Status.ADVICE,"Individueller ärztlicher Betreuungsplan hat Vorrang.")
        if(last!=null && r.interval=="ONCE") return PlanItem(r,Status.COMPLETE,"Für diese Schwangerschaft dokumentiert am ${fmt(last)}.")
        val day=day(p,on) ?: return PlanItem(r,Status.ADVICE,"Geburtstermin ergänzen für konkrete Zeitfenster. ${r.summary}")
        if(day !in 0..294) return PlanItem(r,Status.CONDITION,"Geburtstermin oder Schwangerschaftsstatus aktualisieren. Keine automatische Annahme einer Geburt.")
        if(r.id=="preginitial") return PlanItem(r,Status.CHECK,"Möglichst früh klären; wenn noch nicht erfolgt, jetzt mit der Praxis besprechen.")
        val start=Engine.start(r,p);val end=Engine.end(r,p)
        if(day<r.pregStartDay) return PlanItem(r,Status.FUTURE,"Zeitfenster ${fmt(start)} bis ${fmt(end)}",start)
        if(day>r.pregEndDay) return PlanItem(r,Status.CONDITION,"Reguläres Zeitfenster endete ${fmt(end)}. Falls nicht erfolgt, mit der Praxis klären.")
        if(r.risk.isNotEmpty()) return PlanItem(r,Status.CONDITION,"Persönliche Voraussetzungen ärztlich prüfen. ${r.summary}")
        if(r.interval=="NONE") return PlanItem(r,Status.ADVICE,(last?.let { "Zuletzt ${fmt(it)}. " } ?: "")+r.summary)
        return PlanItem(r,Status.CHECK,"Im Zeitfenster bis ${fmt(end)} · Mutterpass und Praxistermin abgleichen.",end)
    }
    fun coverage(r:Rule,p:Person):Benefit {
        if(!p.pregnant) return Benefit("UNKNOWN","Nur in der Schwangerschaft","Kein allgemeiner altersbezogener Anspruch.")
        if(r.coverage=="PREG_CONDITION") return Benefit("CONDITIONAL","Bei Indikation: 100 %","Bei erfüllten Voraussetzungen reguläre GKV-Leistung. Individuelle Eignung und richtigen Zeitpunkt ärztlich bestätigen lassen.",r.sources)
        if(r.coverage=="PREG_EXTRA") return when(p.insurer) {
            "TK" -> Benefit("PRIVATE","Ohne Verdacht: privat","Kein allgemeiner Vorsorgezuschuss in der geprüften TK-Leistung. Bei Infektionsverdacht kann eine medizinische Indikation zur Abrechnung bestehen.",listOf("pregtk"))
            "BARMER" -> Benefit("PARTIAL","Familie Plus: 200 € gesamt","Gemeinsames Extra-Budget pro Schwangerschaft für verschiedene Leistungen, nicht 200 € pro Test. Restbudget, Teilnahme und Rechnung mit der BARMER klären.",listOf("pregbarmer"))
            "DAK" -> Benefit("CONDITIONAL","MamaPLUS: Kostenübernahme","Die DAK nennt den Test als übernommenen MamaPLUS-Bestandteil. Rechnung der Frauenarztpraxis einreichen; aktuelle Teilnahme- und Erstattungsbedingungen bestätigen.",listOf(if(r.id=="preggbs")"pregdakgbs" else "pregdaktoxo"))
            else -> Benefit("UNKNOWN","Kasse ergänzen","Kein allgemeiner GKV-Routinescreeninganspruch. Extras hängen von der Kasse ab.",r.sources)
        }
        return Benefit("FULL","Kasse zahlt","100 % der vorgesehenen Mutterschaftsvorsorge bei erfüllten Voraussetzungen; privat vereinbarte Extras getrennt betrachten.",r.sources)
    }
}
