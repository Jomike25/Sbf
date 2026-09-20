package com.sbftrainer.app

/**
 * Thematische Unterkategorien des Fragenkatalogs.
 *
 * Die Kataloge enthalten selbst keine Themenangabe, deshalb wird das Thema aus dem
 * Fragetext abgeleitet: ein Treffer gilt, wenn ein Wort der Frage mit einem der
 * Stichwoerter beginnt (Wortanfang, nicht irgendwo im Wort - sonst wuerde z. B.
 * "wind" in "Geschwindigkeit" treffen). Die Reihenfolge der Konstanten ist die
 * Pruefreihenfolge: das erste passende Thema gewinnt.
 *
 * Das ist eine Heuristik und keine amtliche Einteilung; Fragen ohne Treffer landen
 * in [SONSTIGE].
 */
enum class QuestionTopic(
    val emoji: String,
    val labelRes: Int,
    val keywords: List<String>
) {
    NOTFALL(
        "🆘",
        R.string.topic_notfall,
        listOf(
            "seenot", "notruf", "notsignal", "notfall", "mayday", "rettung", "rettungs", "ersthilfe",
            "erste", "feuerlösch", "brand", "löschmittel", "ertrink", "kenter", "gekentert", "leck",
            "quickstopp", "treibend", "gesunken", "unfall", "schwimmweste", "seekrank", "hypotherm",
            "verletz", "ersthelfer", "leuchtrakete", "rakete", "seenotsignal"
        )
    ),
    ZEICHEN(
        "🚦",
        R.string.topic_zeichen,
        listOf(
            "tonne", "tonnen", "betonnung", "fahrwasser", "fahrrinne", "bake", "leuchtfeuer",
            "leuchttonne", "blinkfeuer", "funkelfeuer", "quermarkenfeuer", "befeuerung", "kennung",
            "schifffahrtszeichen", "tafelzeichen", "tafel", "tafeln", "kardinal", "wrack", "badezone",
            "sperrwerk", "richtfeuer", "seezeichen", "feuer", "leitfeuer", "oberfeuer", "unterfeuer",
            "gleichtaktfeuer", "wiederkehr"
        )
    ),
    LICHTER(
        "💡",
        R.string.topic_lichter,
        listOf(
            "licht", "lichter", "laterne", "laternen", "signal", "signale", "schallsignal", "nebelsignal",
            "morsesignal", "lichtsignal", "seitenlicht", "hecklicht", "buglicht", "ankerlicht",
            "rundumlicht", "funkellicht", "mastlicht", "positionslicht", "blaulicht", "sichtzeichen",
            "tagbezeichnung", "nachtbezeichnung", "bezeichnung", "bezeichnet", "flagge", "flaggen",
            "flaggensignal", "wimpel", "nebelhorn", "glocke", "pfeife", "heulton", "morse", "topplicht",
            "langer", "kurzer", "langen", "kurzen", "töne", "schallzeichen", "kegel", "rhombus", "ball",
            "bälle", "zylinder"
        )
    ),
    VERHALTEN(
        "↔️",
        R.string.topic_verhalten,
        listOf(
            "ausweich", "ausweichpflicht", "vorfahrt", "wegerecht", "entgegenkommend", "kreuzend",
            "überhol", "weich", "begegn", "kollision", "kollisionskurs", "zusammenstoß", "vorbeifahr",
            "vorrang", "bergfahrt", "talfahrt", "talfahrer", "bergfahrer", "behindern", "behindert",
            "verkehrstrennungsgebiet", "abstand", "anlegen", "ablegen", "anlaufwinkel", "schleuse",
            "schleusen", "hafen", "wasserski", "stillliegen", "liegeverbot", "verhalten", "verhält",
            "vorsichtsmaßnahme", "vorbeifahrt"
        )
    ),
    WETTER(
        "🌬",
        R.string.topic_wetter,
        listOf(
            "wetter", "wetterbericht", "wetterkarte", "wind", "winde", "windstärke", "beaufort", "nebel",
            "gewitter", "sturm", "böe", "böen", "wolke", "wolken", "luftdruck", "isobaren", "seegang",
            "wellenhöhe", "sichtweite", "sichtweiten", "unsichtig", "niederschlag", "warnung"
        )
    ),
    NAVIGATION(
        "🧭",
        R.string.topic_navigation,
        listOf(
            "seekarte", "seekarten", "karte", "karten", "peilung", "kompass", "magnetkompass",
            "missweisung", "deviation", "seemeile", "gezeit", "gezeiten", "tide", "tidenkalender", "flut",
            "ebbe", "niedrigwasser", "hochwasser", "wasserstand", "position", "echolot", "wassertiefe",
            "tiefen", "standlinie", "radar", "radarreflektor", "radarfahrt", "ais", "besteck", "strömung",
            "strom", "nautisch", "navigiert", "navigation", "seemeilen", "koppelort", "koppeln",
            "veröffentlichungen"
        )
    ),
    TECHNIK(
        "⚙️",
        R.string.topic_technik,
        listOf(
            "motor", "motoren", "motorboot", "antriebsmaschine", "antrieb", "propeller", "schraube",
            "getriebe", "kraftstoff", "treibstoff", "benzin", "diesel", "öl", "ölkontroll",
            "kontrollleuchte", "batterie", "zündung", "kontrolllampe", "maschine", "maschinen",
            "maschinenanlage", "kühl", "welle", "wellen", "auspuff", "tank", "tanken", "gasanlage",
            "flüssiggas", "flüssiggase", "propan", "butan", "gasbehälter", "bilge", "lenzpumpe",
            "landstrom", "stromschlag", "kohlenmonoxid", "abgas", "elektrisch", "elektro"
        )
    ),
    SEEMANNSCHAFT(
        "⚓",
        R.string.topic_seemannschaft,
        listOf(
            "knoten", "leine", "leinen", "festmach", "anker", "trosse", "belegen", "schlepp", "geschleppt",
            "slip", "trailer", "beladen", "stabilität", "krängung", "segel", "segeln", "fender", "poller",
            "klampe", "persenning", "winterlager", "ruder", "rudergänger", "besatzung", "anstrich",
            "unterwasserschiff", "antifouling", "pflege", "gepäck", "vorkehrungen"
        )
    ),
    RECHT(
        "📜",
        R.string.topic_recht,
        listOf(
            "vorschrift", "vorschriften", "führerschein", "sportbootführerschein", "fahrerlaubnis",
            "erlaubnis", "verboten", "haft", "haftung", "versicherung", "papiere", "umwelt",
            "umweltfreundlich", "abfall", "abfälle", "abfällen", "abwasser", "gewässerschutz", "bußgeld",
            "zulassung", "verantwortlich", "geschwindigkeit", "höchstgeschwindigkeit", "kennzeichen",
            "kennzeichnung", "kennzeichnungsarten", "pflicht", "gesetz", "behörde", "wasserschutzpolizei",
            "bestimmungen", "register", "binnenschiffsregister", "bundeswasserstraße",
            "bundeswasserstraßen", "naturschutz", "schutzgebiet", "festgelegt", "auskünfte", "hinweise",
            "grenzen", "schiffsführer", "verordnung", "warngebiet", "schießübung", "tauglichkeit",
            "lebensmöglichkeiten", "seehund", "seehundbänke", "pflanzen", "tierwelt", "müll", "schilf",
            "röhricht", "uferzone", "nachrichten", "bekanntmachungen"
        )
    ),
    GRUNDLAGEN(
        "📖",
        R.string.topic_grundlagen,
        listOf(
            "luvseite", "leeseite", "luv", "lee", "kleinfahrzeug", "kleinfahrzeuge", "manövrierunfähig",
            "manövrierbehindert", "manövrier", "sportboot", "sportboote", "fahrzeugart", "tiefgang",
            "verdrängung"
        )
    ),
    SONSTIGE("🔹", R.string.topic_sonstige, emptyList());

    /** Prueft, ob ein Wort des Textes mit einem Stichwort dieses Themas beginnt. */
    fun matchesText(words: List<String>): Boolean =
        words.any { word -> keywords.any { word.startsWith(it) } }
}

object Topics {

    private val wordPattern = Regex("[a-zäöüß]+")

    /** Themen in Anzeigereihenfolge, [QuestionTopic.SONSTIGE] zuletzt. */
    val all: List<QuestionTopic> = QuestionTopic.values().toList()

    private val cache = HashMap<String, QuestionTopic>()

    @Synchronized
    fun of(question: Question): QuestionTopic = cache.getOrPut(question.id) {
        val words = wordPattern.findAll(question.question.lowercase()).map { it.value }.toList()
        all.firstOrNull { it != QuestionTopic.SONSTIGE && it.matchesText(words) }
            ?: QuestionTopic.SONSTIGE
    }
}
