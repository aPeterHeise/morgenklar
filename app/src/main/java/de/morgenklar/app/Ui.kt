@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package de.morgenklar.app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.testTag
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.time.temporal.ChronoUnit

val Forest=Color(0xFF174F43)
val Ink=Color(0xFF203B34)
val Muted=Color(0xFF65736B)
val Cream=Color(0xFFF6F5EF)
val Lime=Color(0xFFDCECBA)
val Line=Color(0xFFE1E5DB)
val Amber=Color(0xFF8A542D)
val Warm=Color(0xFFF2E8D9)
@Composable fun MorgenTheme(content:@Composable ()->Unit) {
    MaterialTheme(colorScheme=lightColorScheme(primary=Forest,onPrimary=Color.White,secondary=Forest,secondaryContainer=Lime,onSecondaryContainer=Ink,primaryContainer=Lime,onPrimaryContainer=Forest,outline=Muted,background=Cream,surface=Cream,onSurface=Ink,surfaceVariant=Line),content=content)
}
@Composable fun Eyebrow(text:String,color:Color=Muted) { Text(text.uppercase(),color=color,fontSize=11.sp,fontWeight=FontWeight.SemiBold,letterSpacing=1.5.sp) }
@Composable fun Heading(text:String) { Text(text,fontSize=30.sp,fontFamily=FontFamily.Serif,lineHeight=36.sp,color=Ink) }
@Composable fun Body(text:String,color:Color=Muted) { Text(text,color=color,fontSize=14.sp,lineHeight=21.sp) }
@Composable fun Panel(modifier:Modifier=Modifier,color:Color=Color.White,compact:Boolean=false,content:@Composable ColumnScope.()->Unit) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(color).border(1.dp,Line,RoundedCornerShape(22.dp)).padding(if(compact) 14.dp else 20.dp),verticalArrangement=Arrangement.spacedBy(if(compact) 6.dp else 12.dp),content=content)
}
@Composable fun Sun(modifier:Modifier=Modifier,color:Color=Lime) {
    Canvas(modifier) {
        val w=size.width; val h=size.height
        drawArc(color,180f,180f,true,Offset(w*.18f,h*.3f),androidx.compose.ui.geometry.Size(w*.64f,h*.64f))
        drawLine(color,Offset(w*.12f,h*.72f),Offset(w*.88f,h*.72f),3.dp.toPx(),StrokeCap.Round)
        drawLine(color,Offset(w*.5f,h*.06f),Offset(w*.5f,h*.18f),2.dp.toPx(),StrokeCap.Round)
        drawLine(color,Offset(w*.13f,h*.2f),Offset(w*.22f,h*.29f),2.dp.toPx(),StrokeCap.Round)
        drawLine(color,Offset(w*.87f,h*.2f),Offset(w*.78f,h*.29f),2.dp.toPx(),StrokeCap.Round)
    }
}
@Composable fun Tag(text:String,type:String="FULL") {
    val c=when(type) { "PRIVATE","PARTIAL","BONUS","CONDITIONAL","UNKNOWN"->Amber;else->Forest }
    Text(text,Modifier.clip(RoundedCornerShape(8.dp)).background(c.copy(alpha=.08f)).padding(horizontal=9.dp,vertical=5.dp),color=c,fontSize=11.sp,fontWeight=FontWeight.Medium)
}
@Composable fun App(a:MainActivity) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var editor by remember { mutableStateOf<Person?>(null) }
    var creating by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<Rule?>(null) }
    var previewPerson by remember { mutableStateOf<Person?>(null) }
    var logImmediately by remember { mutableStateOf(false) }
    var deleteId by remember { mutableStateOf<String?>(null) }
    var wipe by remember { mutableStateOf(false) }
    val person=a.data.person(); val now=a.currentDay
    fun showRule(rule:Rule, preview:Person?=null, log:Boolean=false) {
        previewPerson=preview
        logImmediately=log
        detail=rule
    }
    if(a.loadError) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),verticalArrangement=Arrangement.Center) {
            Heading("Daten bleiben geschützt");Spacer(Modifier.height(20.dp));Body(a.notice)
            Button(onClick=a::readData) { Text("Erneut öffnen") }
        };return
    }
    Scaffold(containerColor=Cream, bottomBar={ if(person!=null) NavigationBar(containerColor=Color.White) {
        val names=listOf("Mein Plan","Alter","Kassen","Wissen","Personen")
        val icons=listOf(Icons.Outlined.Dashboard,Icons.Outlined.Timeline,Icons.Outlined.AccountBalance,Icons.Outlined.AutoStories,Icons.Outlined.PeopleOutline)
        names.forEachIndexed { i,n -> NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(icons[i],n)},label={Text(n,fontSize=10.sp)},colors=NavigationBarItemDefaults.colors(indicatorColor=Lime)) }
    } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal=22.dp,vertical=14.dp),verticalAlignment=Alignment.CenterVertically) {
                Sun(Modifier.size(31.dp),Forest);Spacer(Modifier.width(8.dp));Text("morgenklar",fontSize=20.sp,fontWeight=FontWeight.SemiBold,letterSpacing=(-.5).sp)
                Spacer(Modifier.weight(1f));Eyebrow("DE · VORSORGE")
            }
            if(a.notice.isNotEmpty()) Text(a.notice,Modifier.fillMaxWidth().background(Warm).padding(16.dp),color=Amber,fontSize=13.sp)
            if(person==null) Welcome { creating=true }
            else when(tab) {
                0 -> Plan(a,person,{ showRule(it) },{ showRule(it,log=true) },{creating=true})
                1 -> Atlas(a.catalog,person,now) { r,p -> showRule(r,p) }
                2 -> Insurers(a.catalog,person,now) { r,p -> showRule(r,p) }
                3 -> Knowledge(a.catalog)
                4 -> People(a,{editor=it},{creating=true},{deleteId=it},{wipe=true})
            }
        }
    }
    if(creating || editor!=null) ProfileEditor(editor,now,{creating=false;editor=null}) { if(a.savePerson(it)) { creating=false;editor=null;tab=0 } }
    detail?.let { r ->
        val p=previewPerson ?: a.data.person()
        if(p!=null) RuleSheet(a,r,p,previewPerson==null,logImmediately,{detail=null})
    }
    if(deleteId!=null || wipe) AlertDialog(onDismissRequest={deleteId=null;wipe=false},title={Text(if(wipe) "Alle lokalen Daten löschen?" else "Person löschen?")},text={Body("Das entfernt das Profil und seine Untersuchungshistorie dauerhaft von diesem Gerät.")},confirmButton={TextButton(onClick={if(wipe) a.deleteAll() else a.removePerson(deleteId!!);deleteId=null;wipe=false}){Text("Löschen")}},dismissButton={TextButton(onClick={deleteId=null;wipe=false}){Text("Behalten")}})
}
@Composable fun Welcome(start:()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(22.dp)) {
        Box(Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(28.dp)).background(Forest),contentAlignment=Alignment.Center) { Sun(Modifier.size(170.dp)) }
        Eyebrow("DEIN VORSORGEKOMPASS")
        Text("Heute wissen.\nMorgen gut leben.",fontSize=40.sp,lineHeight=45.sp,fontFamily=FontFamily.Serif,color=Ink)
        Body("Welche Vorsorge passt zu dir? Entdecke Angebote für dein Alter, behalte Termine im Blick und sieh, was deine Krankenkasse übernimmt.")
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { Tag("Nur auf deinem Gerät");Tag("Für dich & Familie") }
        Button(onClick=start,Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(16.dp)) { Text("Mein Profil anlegen") }
        Body("Vorsorge ist eine Entscheidung. Morgenklar informiert über Angebote für Menschen ohne Beschwerden; persönliche Risiken und auffällige Befunde gehören in die ärztliche Beratung.")
    }
}
@Composable fun Plan(a:MainActivity,p:Person,open:(Rule)->Unit,log:(Rule)->Unit,add:()->Unit) {
    var offset by rememberSaveable(p.id) { mutableIntStateOf(0) }
    var pregnancyView by rememberSaveable(p.id) { mutableStateOf(p.pregnant) }
    var showAll by rememberSaveable(p.id,offset,pregnancyView) { mutableStateOf(false) }
    val isPreg=p.pregnant && pregnancyView
    val on=if(isPreg) a.currentDay else a.currentDay.plusYears(offset.toLong())
    val items=if(isPreg) a.catalog.rules.filter { it.pregnancy }.map { Pregnancy.evaluate(it,p,on) }.sortedBy { when(it.status) { Status.CHECK -> 0;Status.ADVICE -> 1;Status.CONDITION -> 2;Status.FUTURE -> 3;else -> 4 } } else Engine.plan(a.catalog,p,on)
    val needs=items.filter { it.status in listOf(Status.CHECK,Status.DUE,Status.CHOICE) && it.rule.kind!="EXTRA" }
    val others=items.filterNot { it in needs }
    LazyColumn(Modifier.fillMaxSize().testTag("planList"),contentPadding=PaddingValues(start=22.dp,end=22.dp,bottom=24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) {
            a.data.people.forEach { person -> FilterChip(selected=person.id==p.id,onClick={a.commit(a.data.copy(selected=person.id))},label={Text(person.name)},leadingIcon={Icon(Icons.Outlined.Person,null,Modifier.size(16.dp))}) }
            IconButton(onClick=add) { Icon(Icons.Outlined.Add,"Person hinzufügen") }
        } }
        if(p.pregnant) item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            FilterChip(!pregnancyView,{pregnancyView=false},label={Text("Allgemeine Vorsorge")})
            FilterChip(pregnancyView,{pregnancyView=true},label={Text("Schwangerschaft")},leadingIcon={Icon(Icons.Outlined.ChildCare,null,Modifier.size(18.dp))})
        } }
        item { Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Forest).padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) {
                Eyebrow(if(isPreg) "SCHWANGERSCHAFT" else "VORSORGE ${on.year}",Lime)
                Text(p.name,fontSize=28.sp,lineHeight=34.sp,fontFamily=FontFamily.Serif,color=Color.White)
            };Sun(Modifier.size(46.dp)) }
            Text(if(isPreg) Pregnancy.stage(p,on) else "${if(p.approximate) "ca. " else ""}${p.age(on)} Jahre  ·  ${insurers[p.insurer]}",color=Color.White.copy(alpha=.8f),fontSize=13.sp)
        } }
        if(!isPreg) item { Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            (0..3).forEach { i -> FilterChip(selected=i==offset,onClick={offset=i},label={Text(if(i==0) "Dieses Jahr" else "+$i ${if(i==1) "Jahr" else "Jahre"}")}) }
        } }
        item { Column(verticalArrangement=Arrangement.spacedBy(7.dp)) {
            Heading(if(isPreg || offset==0) "Termine im Blick" else "Ausblick auf ${on.year}")
            if(isPreg) Body(p.dueDate?.let { "Geburtstermin: ${fmt(it)} · Zeitfenster mit dem Mutterpass abgleichen." } ?: "Geburtstermin für konkrete Wochenfenster ergänzen.")
            else if(offset>0) Body("Vorschau nach aktuellem Stand und bisherigen Einträgen.")
            if(p.approximate) Body("Geburtsdatum für genaue Altersgrenzen ergänzen.",Amber)
            if(ChronoUnit.DAYS.between(a.catalog.reviewed,a.currentDay)>180) Tag("Datenstand älter als 6 Monate · Quellen prüfen","UNKNOWN")
        } }
        if(p.family || p.chronic || p.smoking in listOf("CURRENT","FORMER")) item {
            Body("Besprich mit deiner Praxis, ob deine Risiken frühere oder zusätzliche Untersuchungen erfordern.",Amber)
        }
        items(needs,key={it.rule.id}) { RuleCard(it,p,on,open,log.takeIf { isPreg || offset==0 }) }
        if(needs.isEmpty()) item { Body("Keine offenen Standardtermine in dieser Ansicht.") }
        if(others.isNotEmpty()) item { OutlinedButton(onClick={showAll=!showAll},Modifier.fillMaxWidth()) { Text(if(showAll) "Weitere Angebote einklappen" else "Weitere Angebote & Erledigtes (${others.size})");Icon(if(showAll) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,null) } }
        if(showAll) items(others,key={it.rule.id}) { RuleCard(it,p,on,open,log.takeIf { isPreg || offset==0 }) }
        item { Footer(a.catalog) }
    }
}
@Composable fun RuleCard(item:PlanItem,p:Person,on:LocalDate,open:(Rule)->Unit,log:((Rule)->Unit)?) {
    val b=Coverage.forRule(item.rule,p,on)
    val canLog=log!=null && item.status !in listOf(Status.FUTURE,Status.PLANNED,Status.COMPLETE,Status.CURRENT)
    Panel(compact=true) {
        CompactRuleHeading(item.rule.title,{ open(item.rule) })
        val dateHint=when(item.status) {
            Status.CURRENT,Status.FUTURE -> item.date?.let { " · ab ${fmt(it)}" }.orEmpty()
            Status.DUE -> item.date?.let { " · seit ${fmt(it)}" }.orEmpty()
            else -> ""
        }
        Body(item.status.label+dateHint,Ink)
        Tag(b.label,b.type)
        if(canLog) Button(onClick={log?.invoke(item.rule)},modifier=Modifier.testTag("complete-${item.rule.id}")) {
            Icon(Icons.Outlined.Check,null,Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Als erledigt markieren")
        }
    }
}
@Composable private fun CompactRuleHeading(title:String,open:()->Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(onClickLabel="Details öffnen",onClick=open)
        .heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(title,Modifier.weight(1f),fontSize=18.sp,fontWeight=FontWeight.SemiBold,lineHeight=23.sp)
        Icon(Icons.Outlined.ChevronRight,null,Modifier.size(22.dp),tint=Muted)
    }
}
@Composable fun Atlas(c:Catalog,base:Person,now:LocalDate,open:(Rule,Person)->Unit) {
    var value by rememberSaveable { mutableStateOf(base.age(now).toString()) }
    var sex by rememberSaveable { mutableStateOf(base.sex) }
    val age=value.toIntOrNull()?.takeIf { it in 0..110 }
    val p=base.copy(birth=now.minusYears((age?:38).toLong()),approximate=false,sex=sex,prostate=sex=="MALE",cervix=sex=="FEMALE",breast=sex=="FEMALE",entries=emptyList(),followup=false)
    val rs=if(age!=null) Engine.atlas(c,p,age) else emptyList()
    LazyColumn(Modifier.fillMaxSize().testTag("atlasList"),contentPadding=PaddingValues(22.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item { Heading("Vorsorge nach Alter");Body("Angebote von 0 bis 110 Jahren.") }
        item { Panel {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                IconButton(onClick={value=((age?:38)-1).coerceAtLeast(0).toString()}) { Icon(Icons.Outlined.Remove,"Ein Jahr jünger") }
                OutlinedTextField(value,{value=it.filter(Char::isDigit).take(3)},Modifier.weight(1f),label={Text("Alter in Jahren")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,isError=age==null)
                IconButton(onClick={value=((age?:38)+1).coerceAtMost(110).toString()}) { Icon(Icons.Outlined.Add,"Ein Jahr älter") }
            }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { FilterChip(sex=="MALE",{sex="MALE"},label={Text("Männer")});FilterChip(sex=="FEMALE",{sex="FEMALE"},label={Text("Frauen")}) }
        } }
        if(age!=null) item { Eyebrow("${rs.size} ANGEBOTE MIT $age") }
        items(rs,key={it.id}) { r -> Panel(compact=true) {
            val b=Coverage.forRule(r,p,now)
            CompactRuleHeading(r.title,{ open(r,p) })
            Tag(b.label,b.type)
        } }
        item { Panel { Text("Und wann noch nicht?",fontWeight=FontWeight.SemiBold);Body("Außerhalb der genannten Altersfenster besteht kein pauschaler Screeninganspruch. Neue Beschwerden, familiäre Risiken oder Vorbefunde können trotzdem eine Untersuchung begründen.") } }
        item { Footer(c) }
    }
}
@Composable private fun InsurerPicker(selected:String,includeNone:Boolean=false,select:(String)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth().testTag("insurerPicker")) {
            Text(insurers[selected] ?: insurers.getValue("NONE"),Modifier.weight(1f))
            Icon(Icons.Outlined.ExpandMore,"Krankenkasse auswählen")
        }
        DropdownMenu(expanded=expanded,onDismissRequest={expanded=false},modifier=Modifier.testTag("insurerOptions")) {
            insurers.filterKeys { includeNone || it!="NONE" }.forEach { (key,label) ->
                DropdownMenuItem(text={Text(label)},onClick={select(key);expanded=false})
            }
        }
    }
}

@Composable fun Insurers(c:Catalog,p:Person,now:LocalDate,open:(Rule,Person)->Unit) {
    var selected by rememberSaveable(p.id,p.insurer) { mutableStateOf(p.insurer) }
    var showExample by rememberSaveable(p.id,selected) { mutableStateOf(false) }
    var invoice by rememberSaveable(p.id) { mutableStateOf("100") }
    val extra=c.rules.filter { it.id in listOf("skinextra","checkextra","fitextra","pzr","sport","psaextra","u10") }
    LazyColumn(Modifier.fillMaxSize().testTag("insurersList"),contentPadding=PaddingValues(22.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item { Heading("Krankenkasse");InsurerPicker(selected,includeNone=true) { selected=it } }
        if(selected=="NONE") item { Body("Wähle eine Kasse, um ihre Zusatzleistungen anzusehen.") }
        else {
            item { Body("Zusatzleistungen mit beispielhaften Altersangaben.") }
            items(extra,key={it.id}) { r ->
                val demoAge=when(r.id){"skinextra","checkextra"->30;"fitextra"->40;"u10"->7;"psaextra"->50;else->maxOf(18,p.age(now))}
                val preview=p.copy(insurer=selected,birth=now.minusYears(demoAge.toLong()),approximate=false)
                val b=Coverage.forRule(r,preview,now)
                Panel(compact=true) {
                    CompactRuleHeading(r.title,{ open(r,preview) })
                    Tag(b.label,b.type)
                }
            }
            item { Panel {
                SectionToggle("Eigenanteil Zahnreinigung berechnen",showExample) { showExample=!showExample }
                if(showExample) {
                    OutlinedTextField(invoice,{invoice=it.filter { ch->ch.isDigit() || ch=='.' || ch==',' }.take(8)},label={Text("Rechnungsbetrag in €")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth())
                    val amount=invoice.replace(',','.').toDoubleOrNull()
                    if(amount!=null && amount>=0) {
                        if(selected=="BARMER") Body("Eigenanteil hängt von verfügbaren Bonuspunkten und dem bereits genutzten gemeinsamen Jahresbudget ab. Ohne diese Angaben nicht berechenbar.")
                        else { val paid=minOf(amount,if(selected=="TK")40.0 else 60.0);Text("Zuschuss bis ${money(paid)} € · Eigenanteil ab ${money(amount-paid)} €",fontWeight=FontWeight.SemiBold);Body("Wenn der Jahreszuschuss noch ungenutzt ist und alle Voraussetzungen erfüllt sind. Ohne verfügbaren Zuschuss zahlst du die Rechnung selbst.") }
                    }
                    Sources(c,when(selected){"TK"->listOf("tkpzr","tkstatute");"DAK"->listOf("dpzr","dstatute");else->listOf("bbonus","bstatute")})
                }
            } }
        }
        item { Footer(c) }
    }
}
fun money(v:Double)=String.format(java.util.Locale.GERMANY,"%.2f",v)
@Composable fun Knowledge(c:Catalog) {
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(22.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item { Eyebrow("WISSEN, DAS EINORDNET");Spacer(Modifier.height(9.dp));Heading("Länger gut leben.");Spacer(Modifier.height(12.dp));Body("Was belegt ist, was individuell sinnvoll sein kann und was noch Forschung ist.") }
        items(c.articles) { article -> Panel { Eyebrow(article.label);Text(article.title,fontSize=21.sp,fontFamily=FontFamily.Serif,lineHeight=27.sp);Body(article.body);Sources(c,article.sources) } }
        item { Footer(c) }
    }
}
@Composable fun People(a:MainActivity,edit:(Person)->Unit,add:()->Unit,remove:(String)->Unit,wipe:()->Unit) {
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(22.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item { Eyebrow("FÜR DIE MENSCHEN, DIE DIR WICHTIG SIND");Spacer(Modifier.height(9.dp));Heading("Gemeinsam im Blick.") }
        items(a.data.people,key={it.id}) { p -> Panel {
            Row(verticalAlignment=Alignment.CenterVertically) { Box(Modifier.size(48.dp).clip(CircleShape).background(Lime),contentAlignment=Alignment.Center) { Text(p.name.take(1).uppercase(),fontSize=21.sp,fontFamily=FontFamily.Serif) };Spacer(Modifier.width(12.dp));Column { Text(p.name,fontWeight=FontWeight.SemiBold,fontSize=19.sp);Body("${p.age(a.currentDay)} Jahre · ${insurers[p.insurer]}") } }
            Body("${p.entries.size} dokumentierte Untersuchungen${if(p.approximate) " · Geburtsdatum noch ergänzen" else ""}")
            Row { TextButton(onClick={edit(p)}){Text("Profil bearbeiten")};Spacer(Modifier.weight(1f));IconButton(onClick={remove(p.id)}) { Icon(Icons.Outlined.DeleteOutline,"${p.name} löschen",tint=Muted) } }
        } }
        item { Button(onClick=add,Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp)) { Icon(Icons.Outlined.Add,null);Spacer(Modifier.width(8.dp));Text("Weitere Person anlegen") } }
        item { Panel { Eyebrow("DEINE DATEN BLEIBEN HIER");Body("Profile und Termine werden verschlüsselt im privaten App-Speicher gespeichert. Keine Anmeldung, kein Tracking, keine Internetberechtigung. Quellen öffnen sich auf Wunsch im Browser.");Body("Die App hat keine eigene Zugangssperre. Nutze die Gerätesperre und lege Daten anderer Personen nur mit deren Einverständnis an. Android-Backup und Geräteübertragung sind deaktiviert; beim Deinstallieren gehen die Daten verloren.") } }
        item { TextButton(onClick=wipe) { Text("Alle lokalen Daten löschen",color=Amber) };Footer(a.catalog) }
    }
}
@Composable fun Footer(c:Catalog) { Column(verticalArrangement=Arrangement.spacedBy(6.dp)) { HorizontalDivider(color=Line);Text("RECHERCHESTAND ${fmt(c.reviewed)}  ·  DEUTSCHLAND",fontSize=10.sp,letterSpacing=1.sp,color=Muted);Body("Beschwerden nicht bis zum nächsten Vorsorgetermin abwarten. Persönliche Empfehlungen und Kostenübernahme mit Praxis und Kasse klären.") } }
@Composable fun Sources(c:Catalog,ids:List<String>) {
    val context=LocalContext.current
    ids.distinct().mapNotNull { id->c.sources.find { it.id==id } }.forEach { s ->
        Column {
            TextButton(onClick={runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(s.url))) }},modifier=Modifier.testTag("source-${s.id}"),contentPadding=PaddingValues(0.dp)) {
                Text(s.title,fontSize=12.sp,modifier=Modifier.weight(1f));Icon(Icons.Outlined.OpenInNew,"Quelle im Browser öffnen",Modifier.size(14.dp))
            }
            Text("${Uri.parse(s.url).host} · ${s.checked?.let { "Zuletzt geprüft: ${fmt(it)}" } ?: "Bisheriger Katalogstand: ${fmt(c.reviewed)}; nicht erneut geprüft"}",fontSize=11.sp,lineHeight=16.sp,color=Muted)
            if(s.edition.isNotBlank()) Text(s.edition,fontSize=11.sp,lineHeight=16.sp,color=Muted)
        }
    }
}
@Composable fun CoverageEvidence(c:Catalog,b:Benefit) {
    Eyebrow("LEISTUNGSBEDINGUNGEN & PRÜFDATUM")
    if(b.sources.isEmpty()) Body("Keine belegte Kostenzusage. Vorab mit der Kasse klären.",Amber)
    else Sources(c,b.sources)
}

fun parseDate(s:String):LocalDate? = runCatching { LocalDate.parse(s.trim(),DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT)) }.getOrNull()
@Composable fun ProfileEditor(existing:Person?,now:LocalDate,dismiss:()->Unit,save:(Person)->Unit) {
    var name by rememberSaveable { mutableStateOf(existing?.name ?: "") }
    var exact by rememberSaveable { mutableStateOf(existing?.approximate==false) }
    var dob by rememberSaveable { mutableStateOf(existing?.takeIf { !it.approximate }?.birth?.let(::fmt) ?: "") }
    var age by rememberSaveable { mutableStateOf(existing?.age(now)?.toString() ?: "38") }
    var sex by rememberSaveable { mutableStateOf(existing?.sex ?: "MALE") }
    var insurer by rememberSaveable { mutableStateOf(existing?.insurer ?: "NONE") }
    var region by rememberSaveable { mutableStateOf(existing?.region ?: "") }
    var cervix by rememberSaveable { mutableStateOf(existing?.cervix ?: false) }
    var prostate by rememberSaveable { mutableStateOf(existing?.prostate ?: true) }
    var breast by rememberSaveable { mutableStateOf(existing?.breast ?: false) }
    var advanced by rememberSaveable { mutableStateOf(false) }
    var chronic by rememberSaveable { mutableStateOf(existing?.chronic ?: false) }
    var family by rememberSaveable { mutableStateOf(existing?.family ?: false) }
    var followup by rememberSaveable { mutableStateOf(existing?.followup ?: false) }
    var pregnant by rememberSaveable { mutableStateOf(existing?.pregnant ?: false) }
    var dueText by rememberSaveable { mutableStateOf(existing?.dueDate?.let(::fmt) ?: "") }
    var smoking by rememberSaveable { mutableStateOf(existing?.smoking ?: "UNKNOWN") }
    var years by rememberSaveable { mutableStateOf(existing?.smokeYears?.toString() ?: "") }
    var packs by rememberSaveable { mutableStateOf(existing?.packYears?.toString() ?: "") }
    var quit by rememberSaveable { mutableStateOf(existing?.quitYears?.let { (it+java.time.Period.between(existing.smokingAsOf,now).years).toString() } ?: "") }
    var colon by rememberSaveable { mutableStateOf(existing?.colonChoice ?: "UNDECIDED") }
    var error by rememberSaveable { mutableStateOf("") }
    Dialog(onDismissRequest=dismiss,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(Modifier.fillMaxSize().safeDrawingPadding(),color=Cream) {
            Column {
                Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically) { Text(if(existing==null) "Dein neues Profil" else "Profil bearbeiten",fontSize=22.sp,fontFamily=FontFamily.Serif,modifier=Modifier.weight(1f));IconButton(onClick=dismiss) { Icon(Icons.Outlined.Close,"Schließen") } }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(15.dp)) {
                    Eyebrow("01 · DIE GRUNDLAGEN")
                    OutlinedTextField(name,{name=it.take(40)},label={Text("Name oder Spitzname")},singleLine=true,modifier=Modifier.fillMaxWidth().testTag("profileName"))
                    Row(verticalAlignment=Alignment.CenterVertically) { Text("Genaues Geburtsdatum",Modifier.weight(1f));Switch(exact,{exact=it}) }
                    if(exact) OutlinedTextField(dob,{dob=it.take(10)},label={Text("Geburtsdatum · TT.MM.JJJJ")},singleLine=true,modifier=Modifier.fillMaxWidth(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Ascii))
                    else { OutlinedTextField(age,{age=it.filter(Char::isDigit).take(3)},label={Text("Aktuelles Alter")},singleLine=true,modifier=Modifier.fillMaxWidth(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number));Body("Ohne Geburtsdatum sind Alterswechsel und Vorschau ungefähr. Für Kinder und Jugendliche bitte das genaue Datum angeben.") }
                    Row(horizontalArrangement=Arrangement.spacedBy(7.dp)) { listOf("MALE" to "Männlich","FEMALE" to "Weiblich","OTHER" to "Individuell").forEach { (key,label) -> FilterChip(sex==key,{sex=key;if(key=="MALE") pregnant=false;prostate=key=="MALE";cervix=key=="FEMALE";breast=key=="FEMALE";if(key=="OTHER") advanced=true},label={Text(label,fontSize=12.sp)}) } }
                    TextButton(onClick={advanced=!advanced},contentPadding=PaddingValues(0.dp)) { Text("Relevante Anatomie anpassen");Icon(Icons.Outlined.ExpandMore,null) }
                    if(advanced) { Body("Nach Operationen oder bei abweichender Anatomie Angebote gezielt auswählen. Anspruch und persönlicher Plan können individuelle Prüfung erfordern.");Toggle("Gebärmutterhals vorhanden",cervix){cervix=it};Toggle("Prostata vorhanden",prostate){prostate=it};Toggle("Brustfrüherkennung berücksichtigen",breast){breast=it} }
                    if(sex=="FEMALE" || sex=="OTHER") {
                        Panel(color=Lime.copy(alpha=.4f)) {
                            Toggle("Ich bin schwanger",pregnant){pregnant=it}
                            if(pregnant) {
                                OutlinedTextField(dueText,{dueText=it.take(10)},label={Text("Errechneter Geburtstermin · TT.MM.JJJJ")},singleLine=true,modifier=Modifier.fillMaxWidth())
                                Body("Optional. Mit dem Termin zeigt die App Schwangerschaftswochen und passende Zeitfenster. Nutze den ärztlich bestätigten Termin aus dem Mutterpass.")
                            }
                            if(existing?.pregnant==true && !pregnant) Body("Der Schwangerschaftsplan wird ausgeblendet. Die bisherigen Einträge bleiben gespeichert; eine später neu aktivierte Schwangerschaft erhält eine eigene Historie.")
                        }
                    }
                    Eyebrow("02 · KRANKENKASSE, OPTIONAL")
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)) { insurers.forEach { (key,label) -> FilterChip(insurer==key,{insurer=key},label={Text(label)}) } }
                    Body("Ohne Angabe oder bei einer anderen Kasse wird der gesetzliche Standard angezeigt. Extras bleiben offen.")
                    OutlinedTextField(region,{region=it.take(30)},label={Text("Bundesland (optional)")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    Eyebrow("03 · GESUNDHEITSFRAGEN, FREIWILLIG")
                    Text("Rauchst du oder hast du früher geraucht?",fontWeight=FontWeight.Medium)
                    Column { listOf("UNKNOWN" to "Keine Angabe","NEVER" to "Nie geraucht","CURRENT" to "Ich rauche aktuell","FORMER" to "Früher geraucht").forEach { (key,label) -> Row(Modifier.fillMaxWidth().clickable { smoking=key },verticalAlignment=Alignment.CenterVertically) { RadioButton(smoking==key,{smoking=key});Text(label,fontSize=14.sp) } } }
                    if(smoking in listOf("CURRENT","FORMER")) {
                        OutlinedTextField(years,{years=it.filter(Char::isDigit).take(3)},label={Text("Rauchjahre insgesamt (optional)")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth())
                        OutlinedTextField(packs,{packs=it.filter { ch->ch.isDigit() || ch=='.' || ch==',' }.take(6)},label={Text("Packungsjahre (optional)")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth())
                        Body("Packungsjahre = Zigaretten pro Tag ÷ 20 × Rauchjahre. Beispiel: 10 Zigaretten täglich über 30 Jahre = 15 Packungsjahre. Längere Rauchpausen muss die Praxis gesondert bewerten.")
                        if(smoking=="FORMER") OutlinedTextField(quit,{quit=it.filter(Char::isDigit).take(3)},label={Text("Rauchstopp vor wie vielen vollen Jahren?")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth())
                        Body("Rauchangaben beschreiben den heutigen Stand. Ändert er sich, bitte das Profil aktualisieren.")
                    }
                    Toggle("Erkrankung oder Risikofaktor bekannt",chronic){chronic=it}
                    Body("Zum Beispiel Bluthochdruck, Diabetes oder eine schwere Grunderkrankung. Das bestätigt noch keine bestimmte Screening- oder Impfindikation.")
                    Toggle("Familiäre Krebsbelastung / Darmrisiko",family){family=it}
                    Body("Zum Beispiel Krebs bei Eltern, Geschwistern oder Kindern; auch chronisch-entzündliche Darmerkrankungen ansprechen. Art und Erkrankungsalter sind für die Praxis entscheidend.")
                    Toggle("Auffälliger Befund / individueller Nachsorgeplan",followup){followup=it}
                    if(followup) Body("Automatische Fälligkeiten werden durch „Individuell besprechen“ ersetzt. Ärztliche Nachsorge geht vor.",Amber)
                    Eyebrow("04 · DARMVORSORGE AB 50")
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)) { listOf("UNDECIDED" to "Noch offen","FIT" to "Stuhltest","COLO" to "Darmspiegelung").forEach { (key,label) -> FilterChip(colon==key,{colon=key},label={Text(label)}) } }
                    Body("Die Verfahren sind Alternativen. Die Auswahl ist eine Planungspräferenz und keine medizinische Entscheidung.")
                    if(error.isNotEmpty()) Text(error,color=Amber,fontSize=14.sp)
                    Spacer(Modifier.height(12.dp))
                }
                Button(onClick={
                    val nAge=age.toIntOrNull()
                    val birthday=if(exact) parseDate(dob) else nAge?.takeIf { it in 18..110 }?.let { if(existing?.approximate==true && existing.age(now)==it) existing.birth else now.minusYears(it.toLong()) }
                    val sy=years.toIntOrNull();val py=packs.replace(',','.').toDoubleOrNull();val q=quit.toIntOrNull()
                    val due=if(pregnant && dueText.isNotBlank()) parseDate(dueText) else null
                    val actualAge=birthday?.let { java.time.Period.between(it,now).years } ?: -1
                    error=when {
                        pregnant && dueText.isNotBlank() && (due==null || due<now.minusDays(14) || due>now.plusDays(280)) -> "Bitte einen plausiblen Geburtstermin (TT.MM.JJJJ) eingeben oder das Feld leer lassen."
                        name.isBlank()->"Bitte einen Namen eingeben."
                        birthday==null || birthday>now || actualAge !in 0..110 -> "Bitte ein gültiges Geburtsdatum oder ein Alter von 18–110 eingeben."
                        smoking in listOf("CURRENT","FORMER") && ((years.isNotBlank() && (sy==null || sy !in 0..actualAge)) || (packs.isNotBlank() && (py==null || !py.isFinite() || py !in 0.0..500.0)) || (smoking=="FORMER" && quit.isNotBlank() && (q==null || q !in 0..actualAge))) -> "Bitte plausible Rauchangaben eingeben oder die Felder leer lassen."
                        else->""
                    }
                    if(error.isEmpty() && birthday!=null) save((existing ?: Person(name=name.trim(),birth=birthday)).copy(name=name.trim(),birth=birthday,approximate=!exact,sex=sex,insurer=insurer,region=region.trim(),cervix=cervix,prostate=prostate,breast=breast,chronic=chronic,family=family,followup=followup,smoking=smoking,smokeYears=sy,packYears=py,quitYears=q,smokingAsOf=now,colonChoice=colon,pregnant=pregnant,dueDate=if(pregnant)due else null,pregnancyId=if(pregnant && existing?.pregnant!=true)java.util.UUID.randomUUID().toString() else existing?.pregnancyId ?: ""))
                },Modifier.fillMaxWidth().padding(20.dp).height(52.dp),shape=RoundedCornerShape(16.dp)) { Text("Profil speichern") }
            }
        }
    }
}
@Composable fun Toggle(label:String,value:Boolean,onChange:(Boolean)->Unit) { Row(Modifier.fillMaxWidth().clickable { onChange(!value) },verticalAlignment=Alignment.CenterVertically) { Text(label,Modifier.weight(1f),fontSize=14.sp,lineHeight=20.sp);Checkbox(value,onChange) } }
@Composable private fun SectionToggle(title:String,expanded:Boolean,toggle:()->Unit) {
    val state=if(expanded) "Ausgeklappt" else "Eingeklappt"
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
        .clickable(onClickLabel=if(expanded) "Einklappen" else "Ausklappen",onClick=toggle)
        .semantics { stateDescription=state }.heightIn(min=48.dp),
        verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(title,Modifier.weight(1f),fontSize=16.sp,fontWeight=FontWeight.SemiBold)
        Icon(if(expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,null,tint=Forest)
    }
}

@Composable fun RuleSheet(a:MainActivity,r:Rule,p:Person,editable:Boolean,logImmediately:Boolean,dismiss:()->Unit) {
    var logging by remember(r.id) { mutableStateOf(logImmediately) }
    var dateText by remember(r.id) { mutableStateOf(fmt(a.currentDay)) }
    var acknowledged by remember(r.id) { mutableStateOf(false) }
    var error by remember(r.id) { mutableStateOf("") }
    var deleting by remember { mutableStateOf<Entry?>(null) }
    val actual=if(editable) a.data.people.find { it.id==p.id } ?: p else p
    val history=actual.entries.filter { it.rule==r.id && (!r.pregnancy || it.episode==actual.pregnancyId) }.sortedByDescending { it.date }
    var showHistory by remember(r.id) { mutableStateOf(false) }
    var showCosts by remember(r.id) { mutableStateOf(false) }
    var showRecommendation by remember(r.id) { mutableStateOf(false) }
    var showAbout by remember(r.id) { mutableStateOf(false) }
    var selectedInsurer by remember(r.id,actual.insurer) { mutableStateOf(actual.insurer) }
    val benefit=Coverage.forRule(r,actual.copy(insurer=selectedInsurer),a.currentDay)
    ModalBottomSheet(onDismissRequest=dismiss,containerColor=Cream,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)) {
        Column(Modifier.fillMaxHeight(.93f).testTag("ruleSheet").verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(bottom=32.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) { Eyebrow(r.category);Spacer(Modifier.weight(1f));IconButton(onClick=dismiss) { Icon(Icons.Outlined.Close,"Details schließen") } }
            Heading(r.title)
            if(editable && r.kind!="PLANNED") {
                if(logging) Panel(color=Lime.copy(alpha=.35f)) {
                    OutlinedTextField(dateText,{dateText=it.take(10)},label={Text("Tatsächliches Datum · TT.MM.JJJJ")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    Toggle("Reguläre Vorsorge abgeschlossen / Impfserie vollständig",acknowledged){acknowledged=it}
                    if(r.id in listOf("colo","fit")) Body("Hier nur reguläre Vorsorge bei unauffälligem Befund eintragen.")
                    if(error.isNotEmpty()) Text(error,color=Amber)
                    Button(onClick={
                        val date=parseDate(dateText)
                        error=when {
                            date==null || !Engine.validEntry(r,actual,date,a.currentDay) -> "Bitte ein passendes Datum bis heute wählen. Der Termin darf noch nicht eingetragen sein."
                            a.addEntry(actual.id,r,date) -> { dismiss();"" }
                            else -> a.notice
                        }
                    },enabled=acknowledged,modifier=Modifier.fillMaxWidth()) { Text("Untersuchung speichern") }
                    TextButton(onClick={logging=false;acknowledged=false;error=""},modifier=Modifier.align(Alignment.End)) { Text("Abbrechen") }
                } else Button(onClick={logging=true},modifier=Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Check,null);Spacer(Modifier.width(8.dp));Text("Als erledigt markieren")
                }
                Body(Engine.evaluate(r,actual,a.currentDay).note)
                if(history.isNotEmpty()) Panel {
                    SectionToggle("Historie (${history.size})",showHistory) { showHistory=!showHistory }
                    if(showHistory) history.forEach { e ->
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                            Text(fmt(e.date),Modifier.weight(1f))
                            IconButton(onClick={deleting=e}) { Icon(Icons.Outlined.DeleteOutline,"Eintrag ${fmt(e.date)} löschen") }
                        }
                    }
                }
            }
            Panel {
                SectionToggle("Kosten & Kasse",showCosts) { showCosts=!showCosts }
                if(showCosts) {
                    InsurerPicker(selectedInsurer,includeNone=true) { selectedInsurer=it }
                    Column(Modifier.testTag("coverage-$selectedInsurer"),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                        Tag(benefit.label,benefit.type)
                        Body(benefit.detail)
                        CoverageEvidence(a.catalog,benefit)
                    }
                } else Tag(benefit.label,benefit.type)
            }
            Panel {
                SectionToggle("Wer empfiehlt das?",showRecommendation) { showRecommendation=!showRecommendation }
                if(showRecommendation) {
                    Text(r.recommendationBy,fontWeight=FontWeight.SemiBold)
                    Body(r.recommendation)
                    Sources(a.catalog,r.recommendationSources)
                }
            }
            Panel {
                SectionToggle("Ablauf, Nutzen & Grenzen",showAbout) { showAbout=!showAbout }
                if(showAbout) {
                    Body(r.summary)
                    if(r.detail!=r.summary) Body(r.detail)
                    Body(r.harm)
                    Sources(a.catalog,r.sources)
                }
            }
        }
    }
    deleting?.let { e -> AlertDialog(onDismissRequest={deleting=null},title={Text("Eintrag entfernen?")},text={Body("Der Termin vom ${fmt(e.date)} wird aus dieser Historie gelöscht. Die Fälligkeit wird neu berechnet.")},confirmButton={TextButton(onClick={a.savePerson(actual.copy(entries=actual.entries.filterNot { it.id==e.id }));deleting=null}) { Text("Entfernen") }},dismissButton={TextButton(onClick={deleting=null}) { Text("Behalten") }}) }
}
