package de.morgenklar.app

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AppFlowTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Before fun reset(){ui.runOnIdle { ui.activity.deleteAll() }}
    private fun demo():AppData {
        val now=ui.activity.currentDay
        val a=Person(name="Alex",birth=now.minusYears(38),insurer="TK")
        val b=Person(name="Mutter",birth=now.minusYears(60),sex="FEMALE",prostate=false,cervix=true,breast=true,insurer="DAK")
        return AppData(listOf(a,b),a.id)
    }
    private fun seed(){ui.runOnIdle{assertTrue(ui.activity.commit(demo()))};ui.waitForIdle()}
    @Test fun aOnboardingStoresVoluntarySmokingDetails() {
        ui.onNodeWithText("Mein Profil anlegen").performScrollTo().performClick()
        ui.onNodeWithTag("profileName").performTextInput("Testprofil")
        ui.onNodeWithText("Ich rauche aktuell").performScrollTo().performClick()
        ui.onNodeWithText("Rauchjahre insgesamt (optional)").performScrollTo().performTextInput("25")
        ui.onNodeWithText("Packungsjahre (optional)").performScrollTo().performTextInput("15,5")
        ui.onNodeWithText("Profil speichern").performClick()
        ui.runOnIdle { val p=ui.activity.data.person()!!;assertEquals("CURRENT",p.smoking);assertEquals(25,p.smokeYears);assertEquals(15.5,p.packYears!!,0.001);assertTrue(p.approximate) }
    }
    @Test fun bIndependentProfilesSurviveRestart() {
        seed();ui.onNodeWithText("Mutter",substring=false).performClick()
        ui.runOnIdle { assertEquals("Mutter",ui.activity.data.person()!!.name) }
        ui.activityRule.scenario.recreate()
        ui.runOnIdle { assertEquals(2,ui.activity.data.people.size);assertEquals("Mutter",ui.activity.data.person()!!.name);assertTrue(ui.activity.data.person()!!.cervix) }
    }
    @Test fun cHistoryPersistsAndChangesSchedule() {
        seed();ui.onNodeWithText("Gesundheits-Check-up").performScrollTo().performClick()
        ui.onNodeWithText("Termin dokumentieren").performScrollTo().performClick()
        ui.onNodeWithText("Reguläre Vorsorge abgeschlossen / Impfserie vollständig").performScrollTo().performClick()
        ui.onNodeWithText("Untersuchung speichern").performScrollTo().performClick()
        ui.onNodeWithContentDescription("Details schließen").performScrollTo().performClick()
        ui.activityRule.scenario.recreate()
        ui.runOnIdle {
            val p=ui.activity.data.person()!!;assertEquals(1,p.entries.size)
            assertEquals(Status.CURRENT,Engine.evaluate(ui.activity.catalog.rules.single { it.id=="check35" },p,ui.activity.currentDay).status)
            assertTrue(ui.activity.data.people.single{it.name=="Mutter"}.entries.isEmpty())
        }
    }
    @Test fun dEncryptedStorageAndDeletion() {
        seed();ui.runOnIdle {
            val a=ui.activity;val f=File(a.filesDir,"morgenklar.vault")
            assertTrue(f.exists());assertFalse(f.readBytes().toString(Charsets.UTF_8).contains("Mutter"));assertEquals(a.data,Vault(a).read())
            a.removePerson(a.data.people.single{it.name=="Mutter"}.id);assertEquals(1,Vault(a).read().people.size)
            a.deleteAll();assertFalse(f.exists());assertTrue(a.data.people.isEmpty())
        }
    }
    @Test fun eAllNavigationAndAtlas() {
        seed();ui.onNodeWithText("Alter",useUnmergedTree=true).performClick();ui.onNodeWithText("Alter in Jahren").performTextReplacement("60")
        ui.onNodeWithText("Frauen").performClick();ui.onNodeWithTag("atlasList").performScrollToNode(hasText("Mammographie-Screening"));ui.onNodeWithText("Mammographie-Screening").assertExists()
        ui.onNodeWithText("Kassen",useUnmergedTree=true).performClick();ui.onNodeWithText("BARMER",substring=false).performClick();ui.onNodeWithText("8,18 Mio.").assertExists()
        ui.onNodeWithText("Wissen",useUnmergedTree=true).performClick();ui.onNodeWithText("Länger gut leben.").assertExists()
        ui.onNodeWithText("Personen",useUnmergedTree=true).performClick();ui.onNodeWithText("Weitere Person anlegen").performScrollTo().assertExists()
    }
    @Test fun fPregnancyQuestionIsConditionalAndPlanPersists() {
        ui.onNodeWithText("Mein Profil anlegen").performScrollTo().performClick()
        ui.onNodeWithTag("profileName").performTextInput("Lisa")
        ui.onNodeWithText("Ich bin schwanger").assertDoesNotExist()
        ui.onNodeWithText("Weiblich").performScrollTo().performClick()
        ui.onNodeWithText("Ich bin schwanger").performScrollTo().performClick()
        val due=ui.activity.currentDay.plusDays(110)
        ui.onNodeWithText("Errechneter Geburtstermin · TT.MM.JJJJ").performScrollTo().performTextInput(fmt(due))
        ui.onNodeWithText("Profil speichern").performClick()
        ui.runOnIdle { assertTrue(ui.activity.data.person()!!.pregnant);assertEquals(due,ui.activity.data.person()!!.dueDate) }
        ui.activityRule.scenario.recreate()
        ui.onNodeWithText("SSW 24+2 · 25. Schwangerschaftswoche").assertExists()
        capture("07-schwangerschaft")
    }
    private fun capture(name:String) {
        ui.waitForIdle()
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val bitmap=instrumentation.uiAutomation.takeScreenshot()
        val dir=File(ui.activity.filesDir,"previews").apply{mkdirs()}
        File(dir,"$name.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        bitmap.recycle()
    }
    @Test fun zCaptureDemoScreens() {
        capture("01-willkommen")
        seed();capture("02-plan")
        ui.onNodeWithText("Mutter",substring=false).performClick();capture("03-familie")
        ui.onNodeWithText("Kassen",useUnmergedTree=true).performClick();capture("04-kassen")
        ui.onNodeWithText("Wissen",useUnmergedTree=true).performClick();capture("05-wissen")
        ui.onNodeWithText("Personen",useUnmergedTree=true).performClick();ui.onAllNodesWithText("Profil bearbeiten").onFirst().performClick()
        ui.onNodeWithText("Rauchst du oder hast du früher geraucht?").performScrollTo();capture("06-gesundheitsfragen")
    }
}
