package de.morgenklar.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class EngineTest {
    private val c=Catalog.parse(javaClass.classLoader!!.getResource("catalog.json")!!.readText())
    private val now=LocalDate.of(2026,11,13)
    private fun person(age:Int=38, female:Boolean=false) = Person(name="Test",birth=now.minusYears(age.toLong()),sex=if(female)"FEMALE" else "MALE",prostate=!female,cervix=female,breast=female)
    private fun rule(id:String)=c.rules.single{it.id==id}
    private fun evaluate(id:String,p:Person,on:LocalDate=now)=Engine.evaluate(rule(id),p,on)
    @Test fun sourceIntegrity() { assertEquals(63,c.rules.size);assertEquals(c.rules.size,c.rules.map{it.id}.toSet().size);c.rules.forEach { r -> assertTrue(r.sources.isNotEmpty());r.sources.forEach { s -> assertTrue(c.sources.any { it.id==s && it.url.startsWith("https://") }) } } }
    @Test fun man38DoesNotGetProstateOrColonoscopy() { val ids=Engine.plan(c,person(),now).map{it.rule.id};assertTrue(ids.containsAll(listOf("check35","skin","hep","dental")));assertFalse(ids.contains("prostate"));assertFalse(ids.contains("colo"));assertFalse(ids.contains("cotest")) }
    @Test fun woman60GetsRelevantScreeningsAndVaccines() { val ids=Engine.plan(c,person(60,true),now).map{it.rule.id};assertTrue(ids.containsAll(listOf("mammo","cotest","flu","pneumo","zoster","fit")));assertFalse(ids.contains("aaa"));assertFalse(ids.contains("prostate")) }
    @Test fun allYearsHaveAnHonestOverview() { for(age in 0..110) for(female in listOf(false,true)) { val p=person(age,female);assertTrue(Engine.atlas(c,p,age).isNotEmpty());assertTrue(Engine.plan(c,p,now).isNotEmpty()) } }
    @Test fun unknownHistoryNeverPretendsDue() { assertEquals(Status.CHECK,evaluate("check35",person()).status) }
    @Test fun checkupUsesCalendarYearsAndRealHistory() { val p=person().copy(entries=listOf(Entry(rule="check35",date=LocalDate.of(2024,12,20))));assertEquals(Status.CURRENT,evaluate("check35",p).status);assertEquals(LocalDate.of(2027,1,1),evaluate("check35",p).date);assertEquals(Status.DUE,evaluate("check35",p,LocalDate.of(2027,1,1)).status) }
    @Test fun onceOnlyHepatitisDoesNotRepeat() { val p=person().copy(entries=listOf(Entry(rule="hep",date=now.minusYears(1))));assertEquals(Status.COMPLETE,evaluate("hep",p).status) }
    @Test fun mammographyEndsAt76Not75() { assertTrue(Engine.plan(c,person(75,true),now).any{it.rule.id=="mammo"});assertFalse(Engine.plan(c,person(76,true),now).any{it.rule.id=="mammo"}) }
    @Test fun announcedMammographyNeverBecomesUnverifiedEntitlement() { val p=person(45,true);assertEquals(Status.PLANNED,evaluate("mammo45",p).status);assertEquals("UNKNOWN",Coverage.forRule(rule("mammo45"),p,now.plusYears(1)).type) }
    @Test fun lungScreeningNeedsAllCriteriaAndQuitBoundary() { val p=person(60).copy(smoking="FORMER",smokeYears=25,packYears=15.0,quitYears=9,smokingAsOf=now);assertTrue(evaluate("lung",p).note.startsWith("Rauchangaben passen"));assertFalse(evaluate("lung",p.copy(quitYears=10)).note.startsWith("Rauchangaben passen"));assertFalse(evaluate("lung",p.copy(packYears=14.9)).note.startsWith("Rauchangaben passen"));assertFalse(evaluate("lung",p.copy(smokeYears=24)).note.startsWith("Rauchangaben passen"));assertFalse(evaluate("lung",p.copy(quitYears=null)).note.startsWith("Rauchangaben passen"));assertFalse(evaluate("lung",p,now.plusYears(1)).note.startsWith("Rauchangaben passen")) }
    @Test fun colonAlternativesAreNotTwoDueTests() { val p=person(55);assertEquals(Status.CHOICE,evaluate("fit",p).status);assertEquals(Status.ADVICE,evaluate("colo",p.copy(colonChoice="FIT")).status) }
    @Test fun colonoscopyPausesFitAndCapsRoutineCount() { val p=person(65).copy(colonChoice="FIT",entries=listOf(Entry(rule="colo",date=now.minusYears(6))));assertEquals(Status.CURRENT,evaluate("fit",p).status);assertEquals(LocalDate.of(2030,1,1),evaluate("fit",p).date);val older=p.copy(entries=listOf(Entry(rule="colo",date=now)));assertEquals(Status.COMPLETE,evaluate("colo",older).status) }
    @Test fun twoColoscopiesDoNotProduceThird() { val p=person(62).copy(colonChoice="COLO",entries=listOf(Entry(rule="colo",date=now.minusYears(12)),Entry(rule="colo",date=now.minusYears(2))));assertEquals(Status.COMPLETE,evaluate("colo",p).status) }
    @Test fun followupOverridesRoutineSchedule() { assertEquals(Status.ADVICE,evaluate("check35",person().copy(followup=true)).status) }
    @Test fun organOverridesWork() { val p=person(60,true).copy(cervix=false,prostate=true);val ids=Engine.plan(c,p,now).map{it.rule.id};assertFalse(ids.contains("cotest"));assertTrue(ids.contains("prostate")) }
    @Test fun exactBirthdaysAndUpcomingThisYear() { val p=person(34).copy(birth=LocalDate.of(1991,12,20));assertEquals(Status.FUTURE,evaluate("check35",p).status);assertTrue(Engine.plan(c,p,now).any{it.rule.id=="check35" && it.status==Status.FUTURE});assertEquals(Status.CHECK,evaluate("check35",p,LocalDate.of(2026,12,20)).status) }
    @Test fun pediatricWindowsAreNotYearLong() { val p=Person(name="Kind",birth=now.minusDays(5));assertEquals(now.minusDays(3),Engine.start(rule("u2"),p));assertEquals(now.plusDays(4),Engine.end(rule("u2"),p));assertFalse(Engine.plan(c,p,now.plusDays(5)).any{it.rule.id=="u2"}) }
    @Test fun dentalRespectsHalfYearAndFourMonths() { assertEquals(LocalDate.of(2026,9,20),Engine.next(rule("dental"),LocalDate.of(2026,5,20))) }
    @Test fun entryValidationRejectsFutureDuplicateAndWrongAge() { val p=person();assertFalse(Engine.validEntry(rule("check35"),p,now.plusDays(1),now));assertFalse(Engine.validEntry(rule("check35"),p,now.minusYears(10),now));assertFalse(Engine.validEntry(rule("check35"),p.copy(entries=listOf(Entry(rule="check35",date=now))),now,now)) }
    @Test fun insurerExtrasKeepConditionsAndSharedBonus() { val r=rule("pzr");assertEquals("UNKNOWN",Coverage.forRule(r,person(),now).type);assertEquals("PARTIAL",Coverage.forRule(r,person().copy(insurer="TK"),now).type);assertEquals("BONUS",Coverage.forRule(r,person().copy(insurer="BARMER"),now).type);assertTrue(Coverage.forRule(r,person().copy(insurer="BARMER"),now).detail.contains("Gemeinsam")) }
    @Test fun childrenSkinLimits() { assertEquals("UNKNOWN",Coverage.forRule(rule("skinextra"),person(12).copy(insurer="DAK"),now).type);assertEquals("FULL",Coverage.forRule(rule("skinextra"),person(12).copy(insurer="TK",region="Bayern"),now).type) }
    @Test fun codecRoundtripKeepsProfilesAndIndependentHistory() { val a=person().copy(entries=listOf(Entry(rule="check35",date=now)));val b=person(60,true).copy(name="Mutter",smoking="FORMER",smokeYears=30,packYears=15.5,quitYears=7);val d=AppData(listOf(a,b),a.id);assertEquals(d,Codec.decode(Codec.encode(d)));assertTrue(d.people[1].entries.isEmpty()) }
}
