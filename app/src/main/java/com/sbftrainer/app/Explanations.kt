package com.sbftrainer.app

/**
 * Kurze, handgeschriebene Begründung ("warum ist das richtig, warum die anderen nicht"),
 * plus optional eine Merkhilfe.
 */
private data class RuleCard(val why: String, val mnemonic: String? = null)

/** Erklärung zu einer einzelnen Frage, wie sie im Quiz nach dem Antworten angezeigt wird. */
data class Explanation(
    val correctAnswerText: String,
    val why: String,
    val mnemonic: String?,
    val isDetailed: Boolean
)

/**
 * Liefert zu ausgewählten Fragen eine fachlich abgesicherte Begründung.
 *
 * Nur Fragen mit einer klaren, eindeutigen seemännischen Regel sind hier hinterlegt - lieber
 * weniger Fragen mit einer Erklärung, auf die man sich verlassen kann, als viele mit geratenen
 * Begründungen zu Detailzahlen oder Ermessensfällen. Für alle anderen Fragen zeigt [explain]
 * ehrlich nur die im Fragenkatalog hinterlegte richtige Antwort, ohne etwas zu erfinden.
 */
object Explanations {

    private val luvLee = RuleCard(
        "Luv ist die dem Wind zugewandte Seite, Lee die windabgewandte Seite – beide sind " +
            "exakte Gegenteile. Die Einteilung hängt ausschließlich vom Wind ab, nicht von " +
            "Backbord/Steuerbord oder der Fahrtrichtung; Antworten mit „links“/„rechts“ in " +
            "Fahrtrichtung sind deshalb hier falsch."
    )

    private val farbenSeiten = RuleCard(
        "Bei der Betonnung gilt: von See bzw. von unten kommend ist Steuerbord (rechte Seite) " +
            "grün, Backbord (linke Seite) rot betonnt oder befeuert. Auf Binnenschifffahrtsstraßen " +
            "gilt für die Bergfahrt entsprechend: Steuerbordseite der Fahrrinne = grüne " +
            "Spitztonnen, Backbordseite = rote Stumpftonnen. Antworten, die Farbe oder Seite " +
            "vertauschen, sind falsch.",
        "Wortlänge als Merkhilfe: „Steuerbord“ ist länger als „Backbord“, „grün“ ist länger als " +
            "„rot“ – die beiden längeren Wörter gehören zusammen: Steuerbord = grün."
    )

    private val tonLang = RuleCard(
        "Ein langer Ton dauert etwa 4 bis 6 Sekunden – deutlich länger als ein kurzer Ton " +
            "(unter 1 Sekunde). Andere Zeitangaben in den Antworten treffen diese Definition " +
            "nicht.",
        "Ein langer Ton dauert etwa so lange wie ein tiefer Atemzug (4–6 s), ein kurzer Ton ist " +
            "so kurz wie ein Wimpernschlag (unter 1 s)."
    )

    private val tonKurz = RuleCard(
        "Ein kurzer Ton dauert weniger als eine Sekunde – deutlich kürzer als ein langer Ton " +
            "(etwa 4 bis 6 Sekunden). Die anderen Antworten nennen Zeiten, die eher zum langen " +
            "Ton passen.",
        "Ein kurzer Ton ist so kurz wie ein Wimpernschlag (unter 1 s), ein langer Ton dauert " +
            "etwa so lange wie ein tiefer Atemzug (4–6 s)."
    )

    private val tonLangBedeutung = RuleCard(
        "Ein einzelner langer Ton ist das allgemeine Achtungssignal – er warnt andere Fahrzeuge, " +
            "etwa vor unübersichtlichen Streckenabschnitten. Rückwärtsfahrt, Manövrierunfähigkeit " +
            "und „Überholen nicht möglich“ werden mit jeweils eigenen, anderen Ton- bzw. " +
            "Zeichenkombinationen angezeigt."
    )

    private val nebelMaschine = RuleCard(
        "Ein Maschinenfahrzeug, das Fahrt durchs Wasser macht, gibt bei verminderter Sicht " +
            "mindestens alle zwei Minuten einen langen Ton. Fahrzeuge vor Anker, geschleppte " +
            "Fahrzeuge oder das letzte Fahrzeug eines Schleppverbandes verwenden davon " +
            "abweichende Signale."
    )

    private val kopfAnKopf = RuleCard(
        "Bei einer Kopf-an-Kopf-Begegnung ändert jedes Maschinenfahrzeug seinen Kurs nach " +
            "Steuerbord, sodass beide sich an Backbord passieren. Antworten mit „Backbord“ oder " +
            "mit „luv-/leewärtig“ passen hier nicht – diese Begriffe gelten für Segelfahrzeuge " +
            "unter Wind, nicht für die Grundregel bei Maschinenfahrzeugen auf Gegenkurs.",
        "Wie im Straßenverkehr: bei Gegenverkehr hält man sich rechts, also nach Steuerbord."
    )

    private val segelGleicheSeite = RuleCard(
        "Haben zwei Segelfahrzeuge den Wind von derselben Seite, muss das luvwärtige (weiter im " +
            "Wind stehende) Fahrzeug dem leewärtigen ausweichen. Antworten, die stattdessen auf " +
            "„Wind von Backbord/Steuerbord“ abstellen, beschreiben den anderen Fall – " +
            "unterschiedliche Windseiten."
    )

    private val segelVerschiedeneSeite = RuleCard(
        "Haben zwei Segelfahrzeuge den Wind von verschiedenen Seiten, muss das Fahrzeug mit " +
            "Wind von Backbord dem Fahrzeug mit Wind von Steuerbord ausweichen. Die luv-/" +
            "leewärtige Regel gilt dagegen nur, wenn beide Fahrzeuge den Wind von derselben " +
            "Seite haben.",
        "Steuerbord ist im Vorteil – ähnlich wie „rechts vor links“ im Straßenverkehr."
    )

    private val kleinfahrzeugDef = RuleCard(
        "Auf Binnenschifffahrtsstraßen gilt ein Sportboot als Kleinfahrzeug, solange es kürzer " +
            "als 20 Meter ist. Ab 20 Metern oder mehr gilt es nicht mehr als Kleinfahrzeug. " +
            "Andere Längenangaben in den Antworten sind falsch."
    )

    private val manoeverunfaehigDef = RuleCard(
        "„Manövrierunfähig“ bedeutet: Das Fahrzeug kann wegen eines außergewöhnlichen Umstands " +
            "– zum Beispiel Ausfall von Ruder- oder Maschinenanlage – nicht wie vorgeschrieben " +
            "manövrieren und deshalb nicht ausweichen. Fischerei mit einschränkenden Fanggeräten " +
            "oder Behinderung durch die Art des Einsatzes sind dagegen Kennzeichen von " +
            "„manövrierbehindert“, ein zu großer Tiefgang beschreibt „tiefgangbehindert“."
    )

    private val manoeverbehindertDef = RuleCard(
        "„Manövrierbehindert“ bedeutet: Das Fahrzeug ist durch die Art seines Einsatzes " +
            "eingeschränkt und kann deshalb nicht wie vorgeschrieben manövrieren (z. B. bei " +
            "Bagger- oder Kabelarbeiten). Ausfall von Ruder oder Maschine beschreibt dagegen " +
            "„manövrierunfähig“, Fischerei mit Fanggeräten ist ein eigener, separat genannter " +
            "Fall."
    )

    private val manoeverunfaehigVorrang = RuleCard(
        "Ein manövrierunfähiges Fahrzeug kann nicht ausweichen – deshalb muss das " +
            "Maschinenfahrzeug ausweichen, wenn Kollisionsgefahr besteht. „Kurs und " +
            "Geschwindigkeit beibehalten“ oder nur „Fahrt verringern“ würde die Ausweichpflicht " +
            "nicht erfüllen."
    )

    private val manoeverbehindertVorrang = RuleCard(
        "Auch ein manövrierbehindertes Fahrzeug hat gegenüber einem Maschinenfahrzeug Vorrang, " +
            "weil es in seiner Bewegungsfreiheit eingeschränkt ist. Das Maschinenfahrzeug muss " +
            "deshalb ausweichen, statt nur Kurs/Geschwindigkeit beizubehalten oder die Fahrt zu " +
            "verringern."
    )

    private val manoeverunfaehigLichterFahrt = RuleCard(
        "Ein manövrierunfähiges Fahrzeug ab 12 m Länge führt in Fahrt mit Fahrt durchs Wasser " +
            "zwei rote Rundumlichter übereinander UND zusätzlich die normalen Seitenlichter und " +
            "das Hecklicht, weil es sich ja durchs Wasser bewegt."
    )

    private val manoeverunfaehigLichterOhneFahrt = RuleCard(
        "Macht das manövrierunfähige Fahrzeug keine Fahrt durchs Wasser, zeigt es nur die zwei " +
            "roten Rundumlichter übereinander – ohne Seiten- und Hecklicht, weil diese eine " +
            "Fahrt durchs Wasser anzeigen würden, die ja nicht vorliegt."
    )

    private val manoeverunfaehigSignalBinnen = RuleCard(
        "Auf Binnenschifffahrtsstraßen wird Manövrierunfähigkeit mit vier kurzen Tönen " +
            "angekündigt; zusätzlich wird bei Tag eine rote Flagge, bei Nacht ein rotes Licht im " +
            "unteren Halbkreis geschwenkt. Fünf kurze Töne sind das allgemeine Warn-/" +
            "Zweifelsignal, andere Ton-Kombinationen beschreiben andere Situationen."
    )

    private val motorseglerKegel = RuleCard(
        "Führt ein Segelfahrzeug zusätzlich einen schwarzen Kegel mit der Spitze nach unten, " +
            "läuft es auch unter Maschine („Motorsegler“) und gilt deshalb für die " +
            "Ausweichregeln als Maschinenfahrzeug, nicht mehr als Fahrzeug unter Segel. Deshalb " +
            "muss es einem reinen Segelfahrzeug ausweichen, unabhängig davon, von welcher Seite " +
            "der Wind kommt.",
        "Der schwarze Kegel bedeutet sinngemäß „ich fahre wie ein Motorboot“ – Segelregeln " +
            "gelten dann nicht mehr."
    )

    private val seemeileDef = RuleCard(
        "Eine Seemeile ist international als die Länge einer Bogenminute auf einem Großkreis " +
            "der Erde definiert und entspricht 1.852 Metern. Die anderen Antworten verwechseln " +
            "das entweder mit einer Bogenminute auf einem Breitenparallel (kein konstanter Wert) " +
            "oder mit einer zurückgelegten Strecke."
    )

    private val knotenDef = RuleCard(
        "„Knoten“ ist die Einheit für Geschwindigkeit auf See: 1 Knoten entspricht 1 Seemeile " +
            "pro Stunde. Die anderen Antworten beziehen sich auf zurückgelegte Strecken pro Tag " +
            "oder auf Kilometer statt Seemeilen und beschreiben damit keine " +
            "Geschwindigkeitseinheit."
    )


    private val kleinfahrzeugOhneMaschine = RuleCard(
        "Ein Kleinfahrzeug ohne Maschinenantrieb (z. B. unter Segel oder Rudern), das die " +
            "eigentlich vorgeschriebenen Lichter nicht führen kann, zeigt ersatzweise " +
            "mindestens ein von allen Seiten sichtbares weißes Rundumlicht. Seitenlichter " +
            "allein, Topp-/Hecklicht oder eine Dreifarbenlaterne setzen dagegen eine feste " +
            "Lichteranlage voraus, die ein einfaches Kleinfahrzeug ohne Maschine oft nicht hat."
    )

    private val schleppregelKleinfahrzeug = RuleCard(
        "Für Kleinfahrzeuge gilt eine vereinfachte Regel: Sowohl beim Schleppen als auch beim " +
            "Geschlepptwerden führt ein Kleinfahrzeug einfach die normalen Lichter eines " +
            "Kleinfahrzeugs mit Maschinenantrieb – zusätzliche Schlepplichter, wie sie große " +
            "Verbände zeigen müssen, sind hier nicht vorgeschrieben."
    )

    private val motorseglerLichter = RuleCard(
        "Läuft ein Segelfahrzeug zusätzlich mit Maschinenkraft, gilt es – wie schon bei den " +
            "Ausweichregeln – als Maschinenfahrzeug und muss deshalb auch dessen Lichter " +
            "führen, nicht die Segellichter. Die anderen Antworten mischen Segel- und " +
            "Maschinenlichter oder nennen Lichter für eine ganz andere Situation " +
            "(Manövrierunfähigkeit).",
        "Genau wie beim schwarzen Kegel gilt: Läuft die Maschine mit, zählt das Boot als " +
            "Maschinenfahrzeug – auch bei den Lichtern."
    )

    private val schubverbandLichter = RuleCard(
        "Ein Schubverband zeigt drei weiße Topplichter in einem Dreieck angeordnet, dazu die " +
            "normalen Seitenlichter und drei weiße Hecklichter nebeneinander. Die " +
            "Dreiecksanordnung der Topplichter unterscheidet ihn von einem einzelnen Fahrzeug " +
            "oder anderen Verbandsformen, bei denen die Lichter anders angeordnet sind."
    )

    private val geschlepptesFahrzeugLichter = RuleCard(
        "Ein geschlepptes Fahrzeug führt die normalen Seitenlichter (rot/grün) und zusätzlich " +
            "ein weißes Hecklicht – keine roten Rundumlichter (die sind z. B. für " +
            "Manövrierunfähigkeit reserviert) und kein weißes Rundumlicht (das wäre z. B. ein " +
            "Ankerlicht)."
    )

    private val fischereiVorrang = RuleCard(
        "Ein Fahrzeug beim Fischfang hat sowohl gegenüber einem Maschinenfahrzeug als auch " +
            "gegenüber einem Segelfahrzeug Vorrang – beide müssen ausweichen, wenn " +
            "Kollisionsgefahr besteht. Das gilt, obwohl Segelfahrzeuge gegenüber " +
            "Maschinenfahrzeugen sonst meist im Vorteil sind."
    )

    private val ueberholendDef = RuleCard(
        "Als überholend gilt ein Fahrzeug, wenn es sich einem anderen aus einer Richtung von " +
            "mehr als 22,5° achterlicher als querab nähert – also aus dem Bereich, in dem man " +
            "bei Nacht nur das weiße Hecklicht des anderen Fahrzeugs sehen würde, nicht dessen " +
            "Seitenlichter. Die anderen Antworten nennen einen falschen Winkel oder den " +
            "falschen Lichtbereich.",
        "Siehst du nur das weiße Hecklicht und keine der farbigen Seitenlichter, bist du im " +
            "Überhol-Sektor."
    )

    private val ueberholendVerhalten = RuleCard(
        "Ein überholendes Fahrzeug muss dem zu überholenden Fahrzeug ausweichen, bis es klar " +
            "vorbeigefahren ist. Ein Schallsignal zur „Zustimmung“ ist dafür nicht " +
            "vorgeschrieben, und nur „Abstand halten“ oder „nicht behindern“ beschreibt die " +
            "Ausweichpflicht nicht vollständig."
    )

    private val schleuseKleinfahrzeugReihenfolge = RuleCard(
        "Kleinfahrzeuge fahren bei einer gemeinsamen Schleusung erst nach den größeren " +
            "Fahrzeugen und erst auf Aufforderung der Schleusenaufsicht ein – nicht von sich " +
            "aus und nicht vorher."
    )

    private val cards: Map<String, RuleCard> = buildMap {
        for (id in listOf("binnen-1", "binnen-192", "see-1", "see-190")) put(id, luvLee)
        for (id in listOf("binnen-13", "see-43", "see-92")) put(id, farbenSeiten)
        for (id in listOf("binnen-79", "see-81")) put(id, tonLang)
        for (id in listOf("binnen-103", "see-100")) put(id, tonKurz)
        put("binnen-92", tonLangBedeutung)
        put("see-159", nebelMaschine)
        for (id in listOf("binnen-182", "see-181")) put(id, kopfAnKopf)
        put("see-65", segelGleicheSeite)
        put("binnen-91", segelVerschiedeneSeite)
        for (id in listOf("binnen-18", "binnen-55")) put(id, kleinfahrzeugDef)
        put("see-157", manoeverunfaehigDef)
        put("see-194", manoeverbehindertDef)
        put("see-160", manoeverunfaehigVorrang)
        put("see-87", manoeverbehindertVorrang)
        put("see-64", manoeverunfaehigLichterFahrt)
        put("see-37", manoeverunfaehigLichterOhneFahrt)
        put("binnen-165", manoeverunfaehigSignalBinnen)
        for (id in listOf("binnen-23", "binnen-131", "binnen-151")) put(id, motorseglerKegel)
        put("see-177", seemeileDef)
        put("see-165", knotenDef)
        for (id in listOf("binnen-19", "see-18")) put(id, kleinfahrzeugOhneMaschine)
        for (id in listOf("binnen-90", "binnen-186")) put(id, schleppregelKleinfahrzeug)
        for (id in listOf("binnen-193", "see-191")) put(id, motorseglerLichter)
        put("binnen-102", schubverbandLichter)
        put("see-158", geschlepptesFahrzeugLichter)
        for (id in listOf("see-13", "see-143")) put(id, fischereiVorrang)
        put("see-170", ueberholendDef)
        put("see-207", ueberholendVerhalten)
        put("binnen-16", schleuseKleinfahrzeugReihenfolge)
    }

    /** Anzahl der Fragen mit handgeschriebener Begründung - für Statistik/Transparenz. */
    fun detailedCount(): Int = cards.size

    fun explain(question: Question): Explanation {
        val correctAnswerText = question.options[question.correctIndex]
        val card = cards[question.id]
        return if (card != null) {
            Explanation(correctAnswerText, card.why, card.mnemonic, isDetailed = true)
        } else {
            Explanation(correctAnswerText, why = "", mnemonic = null, isDetailed = false)
        }
    }
}
