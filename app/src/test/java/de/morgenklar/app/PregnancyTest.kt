package de.morgenklar.app
import java.time.LocalDate
import org.junit.Test
import org.junit.Assert.*

class PregnancyTest {
    private val c=Catalog.parse(javaClass.classLoader!!.getResource("catalog.json")!!.readText())
    private val now=LocalDate.of(2026,11,13)
    private fun rule(id:String)=c.rules.single{it.id==id}
    private fun person(day:Long=170)=Person(name="Lisa",birth=now.minusYears(34),sex="FEMALE",prostate=false,cervix=true,breast=true,pregnant=true,dueDate=now.plusDays(280-day),pregnancyId="preg1")
    @Test fun gestationalAgeAndUltrasoundWindowsAreExact(){val p=person();assertEquals("SSW 24+2 · 25. Schwangerschaftswoche",Pregnancy.stage(p,now));assertEquals(now.minusDays(114),Engine.start(rule("pregus1"),p));assertEquals(now.minusDays(87),Engine.end(rule("pregus1"),p))}
    @Test fun glucoseStartsAt24AndEnds27plus6(){val r=rule("pregglucose");assertEquals(Status.FUTURE,Pregnancy.evaluate(r,person(167),now).status);assertEquals(Status.CHECK,Pregnancy.evaluate(r,person(168),now).status);assertEquals(Status.CHECK,Pregnancy.evaluate(r,person(195),now).status);assertEquals(Status.CONDITION,Pregnancy.evaluate(r,person(196),now).status)}
    @Test fun pastWindowWithoutHistoryIsNotReassuring(){assertEquals(Status.CONDITION,Pregnancy.evaluate(rule("pregus1"),person(),now).status)}
    @Test fun unknownDueDateDoesNotGuessWeeks(){val p=person().copy(dueDate=null);assertEquals(Status.ADVICE,Pregnancy.evaluate(rule("pregglucose"),p,now).status);assertEquals("Geburtstermin noch offen",Pregnancy.stage(p,now))}
    @Test fun earlierPregnancyCannotCompleteCurrentOne(){val p=person().copy(entries=listOf(Entry(rule="pregglucose",date=now,episode="old")));assertEquals(Status.CHECK,Pregnancy.evaluate(rule("pregglucose"),p,now).status);assertEquals(Status.COMPLETE,Pregnancy.evaluate(rule("pregglucose"),p.copy(pregnancyId="old"),now).status)}
    @Test fun niptAndAntiDAreConditional(){assertEquals(Status.CONDITION,Pregnancy.evaluate(rule("pregnipt"),person(),now).status);assertEquals(Status.CONDITION,Pregnancy.evaluate(rule("pregantid"),person(195),now).status);assertEquals("CONDITIONAL",Coverage.forRule(rule("pregnipt"),person(),now).type)}
    @Test fun endingPregnancyHidesPlanAndDoesNotAssumeBirth(){val p=person().copy(pregnant=false);assertFalse(Engine.matches(rule("pregglucose"),p));assertEquals(Status.CONDITION,Pregnancy.evaluate(rule("pregglucose"),p,now).status);assertEquals(Status.CONDITION,Pregnancy.evaluate(rule("pregglucose"),person(295),now).status)}
    @Test fun pregnancyPersistsWithEpisodeAndDueDate(){val p=person().copy(entries=listOf(Entry(rule="pregglucose",date=now,episode="preg1")));val data=AppData(listOf(p),p.id);assertEquals(data,Codec.decode(Codec.encode(data)))}
    @Test fun pregnancyDoesNotLeakIntoAgeAtlasOrYearForecast(){val p=person();assertFalse(Engine.atlas(c,p,34).any{it.pregnancy});assertFalse(Engine.plan(c,p,now.plusYears(1)).any{it.rule.pregnancy})}
    @Test fun extraBudgetsAreNotPerTest(){val r=rule("pregtoxo");assertEquals("PRIVATE",Coverage.forRule(r,person().copy(insurer="TK"),now).type);assertTrue(Coverage.forRule(r,person().copy(insurer="BARMER"),now).detail.contains("gemeinsames Budget"));assertEquals("CONDITIONAL",Coverage.forRule(r,person().copy(insurer="DAK"),now).type)}
    @Test fun additionalPregnancyTestsKeepStatutoryRestrictions(){
        val dak=Coverage.forRule(rule("pregtoxo"),person().copy(insurer="DAK"),now)
        assertTrue(dak.detail.contains("besonderer Infektionsgefahr"));assertTrue(dak.sources.contains("dstatute"))
        val barmer=Coverage.forRule(rule("preggbs"),person().copy(insurer="BARMER"),now)
        assertTrue(barmer.detail.contains("31.03."));assertTrue(barmer.detail.contains("Neugeborenen-Osteopathie"))
        assertEquals("UNKNOWN",Coverage.forRule(rule("preggbs"),person().copy(insurer="TK"),now).type)
        assertEquals(listOf("maternityrl"),Coverage.forRule(rule("pregus1"),person(),now).sources)
    }
}
