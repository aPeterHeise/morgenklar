package de.morgenklar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    var data by mutableStateOf(AppData()); private set
    var notice by mutableStateOf(""); private set
    var loadError by mutableStateOf(false); private set
    var currentDay by mutableStateOf(today()); private set
    lateinit var catalog: Catalog; private set
    private lateinit var vault: Vault
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        vault=Vault(this)
        catalog=Catalog.parse(assets.open("catalog.json").bufferedReader().use { it.readText() })
        readData()
        setContent { MorgenTheme { App(this) } }
    }
    override fun onResume() { super.onResume(); currentDay=today() }
    fun readData() { try { data=vault.read(); loadError=false; notice="" } catch(_:Exception) { loadError=true;notice="Die gespeicherten Daten konnten nicht geöffnet werden. Sie wurden nicht überschrieben." } }
    fun commit(next: AppData): Boolean = try { vault.save(next); data=next; notice="";true } catch(_:Exception) { notice="Speichern fehlgeschlagen. Bitte erneut versuchen; die Änderung wurde nicht übernommen.";false }
    fun savePerson(p: Person): Boolean = commit(data.copy(people=if(data.people.any { it.id==p.id }) data.people.map { if(it.id==p.id) p else it } else data.people+p,selected=p.id))
    fun removePerson(id: String) = commit(data.copy(people=data.people.filterNot { it.id==id },selected=if(data.selected==id) "" else data.selected))
    fun deleteAll() { try { vault.delete();data=AppData();notice="";loadError=false } catch(_:Exception) { notice="Löschen fehlgeschlagen." } }
    fun addEntry(personId: String,rule: Rule,date: LocalDate): Boolean {
        val p=data.people.find { it.id==personId } ?: return false
        if(!Engine.validEntry(rule,p,date,currentDay)) { notice="Datum passt nicht zum Altersfenster, liegt in der Zukunft oder ist bereits eingetragen.";return false }
        return savePerson(p.copy(entries=p.entries+Entry(rule=rule.id,date=date)))
    }
}
