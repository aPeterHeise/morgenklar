package de.morgenklar.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.Period
import java.util.UUID

fun today(): LocalDate = LocalDate.now()
fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
fun JSONArray.strings() = (0 until length()).map { getString(it) }
data class Source(val id: String, val title: String, val url: String, val checked: LocalDate? = null, val edition: String = "")
data class Article(val label: String, val title: String, val body: String, val sources: List<String>)
data class Rule(val id: String, val title: String, val minAge: Int, val maxAge: Int, val interval: String,
    val category: String, val summary: String, val detail: String, val harm: String, val sources: List<String>,
    val sex: String, val organ: String, val risk: String, val kind: String, val coverage: String,
    val pregnancy: Boolean = false, val pregStartDay: Int = 0, val pregEndDay: Int = 294, val startMonths: Int = -1, val endMonths: Int = -1, val startDays: Int = -1, val endDays: Int = -1,
    val coverageSources: List<String> = emptyList(), val recommendationBy: String = "", val recommendation: String = "", val recommendationSources: List<String> = emptyList())
data class Catalog(val reviewed: LocalDate, val rules: List<Rule>, val sources: List<Source>, val articles: List<Article>) {
    companion object {
        fun parse(raw: String): Catalog {
            val j = JSONObject(raw)
            return Catalog(LocalDate.parse(j.getString("reviewed")), j.getJSONArray("rules").objects().map {
                Rule(it.getString("id"),it.getString("title"),it.getInt("minAge"),it.getInt("maxAge"),it.getString("interval"),
                    it.getString("category"),it.getString("summary"),it.getString("detail"),it.getString("harm"),it.getJSONArray("sources").strings(),
                    it.getString("sex"),it.getString("organ"),it.getString("risk"),it.getString("kind"),it.getString("coverage"),
                    it.optBoolean("pregnancy",false),it.optInt("pregStartDay",0),it.optInt("pregEndDay",294),it.optInt("startMonths",-1),it.optInt("endMonths",-1),it.optInt("startDays",-1),it.optInt("endDays",-1),
                    it.getJSONArray("coverageSources").strings(),it.getString("recommendationBy"),it.getString("recommendation"),it.getJSONArray("recommendationSources").strings())
            }, j.getJSONArray("sources").objects().map { Source(it.getString("id"),it.getString("title"),it.getString("url"),it.optString("checked").takeIf(String::isNotBlank)?.let(LocalDate::parse),it.optString("edition")) },
                j.getJSONArray("knowledge").objects().map { Article(it.getString("label"),it.getString("title"),it.getString("body"),it.getJSONArray("sources").strings()) })
        }
    }
}
data class Entry(val id: String = UUID.randomUUID().toString(), val rule: String, val date: LocalDate, val episode: String = "")
data class Person(val id: String = UUID.randomUUID().toString(), val name: String, val birth: LocalDate,
    val approximate: Boolean = false, val sex: String = "MALE", val insurer: String = "NONE", val region: String = "",
    val cervix: Boolean = false, val prostate: Boolean = true, val breast: Boolean = false,
    val chronic: Boolean = false, val family: Boolean = false, val followup: Boolean = false,
    val smoking: String = "UNKNOWN", val smokeYears: Int? = null, val packYears: Double? = null, val quitYears: Int? = null,
    val smokingAsOf: LocalDate = today(), val colonChoice: String = "UNDECIDED", val entries: List<Entry> = emptyList(), val pregnant: Boolean = false, val dueDate: LocalDate? = null, val pregnancyId: String = "") {
    fun age(on: LocalDate) = Period.between(birth,on).years
}
data class AppData(val people: List<Person> = emptyList(), val selected: String = "") {
    fun person() = people.find { it.id == selected } ?: people.firstOrNull()
}
object Codec {
    fun encode(d: AppData): String = JSONObject().put("version",1).put("selected",d.selected).put("people",JSONArray(d.people.map { p ->
        JSONObject().put("id",p.id).put("name",p.name).put("birth",p.birth).put("approximate",p.approximate).put("sex",p.sex).put("insurer",p.insurer).put("region",p.region)
            .put("cervix",p.cervix).put("prostate",p.prostate).put("breast",p.breast).put("chronic",p.chronic).put("family",p.family).put("followup",p.followup)
            .put("smoking",p.smoking).put("smokeYears",p.smokeYears).put("packYears",p.packYears).put("quitYears",p.quitYears).put("smokingAsOf",p.smokingAsOf).put("colonChoice",p.colonChoice)
            .put("pregnant",p.pregnant).put("dueDate",p.dueDate?.toString()).put("pregnancyId",p.pregnancyId)
            .put("entries",JSONArray(p.entries.map { JSONObject().put("id",it.id).put("rule",it.rule).put("date",it.date).put("episode",it.episode) }))
    })).toString()
    fun decode(raw: String): AppData {
        val j = JSONObject(raw); require(j.getInt("version") == 1) { "Unbekannte Datenversion" }
        return AppData(j.getJSONArray("people").objects().map { p ->
            fun n(k: String): Int? = if(p.isNull(k)) null else p.getInt(k)
            Person(p.getString("id"),p.getString("name"),LocalDate.parse(p.getString("birth")),p.getBoolean("approximate"),p.getString("sex"),p.getString("insurer"),p.optString("region"),
                p.getBoolean("cervix"),p.getBoolean("prostate"),p.getBoolean("breast"),p.getBoolean("chronic"),p.getBoolean("family"),p.getBoolean("followup"),
                p.getString("smoking"),n("smokeYears"),if(p.isNull("packYears")) null else p.getDouble("packYears"),n("quitYears"),LocalDate.parse(p.getString("smokingAsOf")),p.getString("colonChoice"),p.getJSONArray("entries").objects().map {
                    Entry(it.getString("id"),it.getString("rule"),LocalDate.parse(it.getString("date")),it.optString("episode","")) },p.optBoolean("pregnant",false),p.optString("dueDate","").takeIf { it.isNotBlank() }?.let(LocalDate::parse),p.optString("pregnancyId",""))
        },j.getString("selected"))
    }
}
