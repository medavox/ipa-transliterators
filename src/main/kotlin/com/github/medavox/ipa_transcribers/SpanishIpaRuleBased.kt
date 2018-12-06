package com.github.medavox.ipa_transcribers

import java.lang.StringBuilder

/**Spanish spelling is largely considered phonetic.
 * I'm not sure how true that is practice
 * (with all the varying dialects), but it does mean that there aren't many IPA transcription
 * engines for spanish out there.
 *
 * So I'll try to write my own.
 *
 * Pronunciation Information sourced from:-
 * * [Oxford Dictionary Spanish Pronunciation Guide](https://es.oxforddictionaries.com/grammar/spanish-pronunciation)]
 * * [Wikipedia's IPA for Spanish](https://en.wikipedia.org/wiki/Help:IPA/Spanish)
 * */
class SpanishIpaRuleBased: IpaTranscriber {
    //the 'transcripcon' problem - does the voicedness of n bleed over onto s AND c?
    //todo: account for voicing assimilation
    /**
     METHODOLOGY
    ============

    all "before" and "after" rules cn be encoded here as digraphs,
    even if this produces a lot of rules (eg, before a voiced consonant).

    It's still simpler than creating an entire IPA-structure in code,
    to look back at the voicedness of the last (or next) letter.

    Digraphs require less code, but more rules.

    We do however need to be able to distinguish Spanish syllable boundaries,
    for two reasons:

    1. Some rules are encoded as syllable-final or syllable-initial.
     We need to know what part of the current syllable we're at.

    2. Barring words that have specific accent-markers, Spanish word stress has rules along the lines of falling
    on the final or penultimate syllable.
    We need to know which syllable of the word we're on, so we know which one to mark for stress in the IPA.

    (having looked at the rules for syllabification in Spanish, we don't need the Spanish sonority hierarchy.)

    -------

    10.  R is pronounced /r/ when it occurs between vowels or in syllable-final position
    (aro /′aro/, horma /′orma/, barco /′barko/, cantar /kan′tar/).
    It is pronounced /rr/ when in initial position (rama /′rrama/, romper /rrom′per/).


    11.  S is pronounced /s/ but it is aspirated in many dialects of Spanish
    when it occurs in syllable-final position (hasta /′ahta/, los cuatro /loh′kwatro/).
    In other dialects it is voiced when followed by a voiced consonant (mismo/′mIzmo/, los dos /loz′ðos/).

    14.  Y (a) When followed by a vowel within the same syllable y is pronounced rather like the y in English yes
    (slightly more emphatically when at the beginning of an utterance).
    In the River Plate area it is pronounced /Ʒ/ (as in English measure),
    with some speakers using a sound which tends toward /ʃ/ (as in shop).

    When phonetic transcriptions of Spanish headwords containing y are given in the dictionary,
    the symbol /J/ is used to represent both pronunciations described above.
    (b) As the conjunction y and in syllable-final position, y is pronounced /i/.

     ----------

     Syllabification Rules
     ====================

     from [https://www.spanishdict.com/guide/spanish-syllables-and-syllabification-rules]

     Consonant Plus Vowel
     -------------------

     Whenever possible, you should break up words so that each syllable contains a consonant followed by a vowel.
     A consonant between two vowels belongs to the syllable with the second vowel.
     The goal is to end a syllable with a vowel whenever possible.

     Check out the syllabification of these common Spanish words.

     Word    | Syllabification
     --------|----------------
     sábana  | sá-ba-na
     gato    | ga-to
     casa    | ca-sa
     mano    | ma-no
     oro     | o-ro
     mesa    | me-sa

     Two Consecutive Consonants
     -------------------------

     Two consecutive consonants will generally belong to separate syllables.
     However, if the second consonant in a consonant pair is r or l,
     the consonant pair is not separated into different syllables.

     Words that begin with prefixes often violate the above rules.
     For example the syllabification of enloquecer is en-lo-que-cer.

     Check out the syllabification of these common Spanish words containing consecutive consonants.

     Word       | Syllabification
     -----------|----------------
     cuando     | cuan-do
     alcanzar   | al-can-zar
     costa      | cos-ta
     sombrilla  | som-bri-lla
     clave      | cla-ve
     trabajo    | tra-ba-jo
     aplicar    | a-pli-car
     frecuente  | fre-cuen-te
     hecho      | he-cho
     amarillo   | a-ma-ri-llo
     carro      | ca-rro
     merengue   | me-ren-gue

     In Puerto Rico and most of Spain, the consonant cluster tl is divided into separate syllables.
     For example, the syllabification of atlántico is at-lán-ti-co.

     In other regions, such as Mexico and the Canary Islands of Spain,
     the consonant cluster tl is not divided into separate syllables.
     For example, the syllabification of atlántico is a-tlán-ti-co
     and the syllabification of tlacuache (possum) is tla-cua-che.

     Three Consecutive Consonants
     ---------------------------

     When three consonants appear together, the first one will generally belong to a separate syllable.

     Check out the syllabification of these words with three consecutive consonants.

     Word       | Syllabification
     -----------|----------------
     inglés     | in-glés
     compresar  | com-pre-sar
     panfleto   | pan-fle-to
     ombligo    | om-bli-go
     constante  | cons-tan-te

     Strong and Weak Vowels
     ----------


     A tilde placed over a letter changes the above pronunciation rules,
     and the accented letter must be separated from any surrounding vowels. Example: mío

     Check out the syllabification of these words containing groups of vowels.

     Word    | Syllabification
     --------|---------------
     toalla  | to-a-lla
     feo     | fe-o
     iguana  | i-gua-na
     reina   | rei-na
     tío     | tí-o
     ciudad  | ciu-dad
     creer   | cre-er

     */

    /**Maps the first character of digraphs to all the possible rules it could represent,
     * if it forms a digraph with the next character.
     *  Digraphs are orthographical combos, usually two letters, that together represent one sound.
     *  Eg in English: th sh ch.
     *  */
    override fun transcribeToIpa(nativeText: String): Set<Variant> {
        val american = StringBuilder().append('/')
        val european = StringBuilder().append('/')

        //NOTE:rule order matters!
        data class Rule(val matcher:Regex, val outputString:String, val lettersConsumed:Int=1 )
        val rules:Array<Rule> = arrayOf(
            //8. Ñ is always pronounced /ŋ/
            Rule(Regex("ñ"), "ɲ"),
            //5.  H is mute in Spanish, (huevo /′ weβo/, almohada /almo′aða/)
            Rule(Regex("h"), ""),
            Rule(Regex("(mb|mv|nb|nv)"), "mb", 2),
            Rule(Regex("^[bv]"), "b"),
            //1.  B,V The letters b and v are pronounced in exactly the same way:
            //    /b/ when at the beginning of an utterance or after m or n (barco/′barko/, vaca/′baka/, ambos /′ambos/, en vano /em′bano/)
            // and /β/ in all other contexts (rabo /′rraβo/, ave /′aβe/, árbol /′arβol/, Elvira /el′βira/).

            Rule(Regex("[bv]"), "β"),
            Rule(Regex("ng"), "ŋg", 2),
            Rule(Regex("(nk|nc)"), "ŋ"),

            //the combination 'ch' is pronounced /tʃ/ (chico /′tʃiko/, leche /′letʃe/).
            Rule(Regex("ch"), "tʃ", 2),
            // When c is followed by e or i,
            // it is pronounced /s/ in Latin America and parts of southern Spain and /θ/ in the rest of Spain
            // (cero /′sero/, /′θero/; cinco /′siŋko/, /′θiŋko/).
            Rule(Regex("c[ie]"), "θ|s"),
            //2.  C is pronounced /k/ when followed by a consonant other than h or by a, o or u
            Rule(Regex("c"), "k"),

            //15.  Z is pronounced /s/ in Latin America and parts of southern Spain and /θ/ in the rest of Spain.
            Rule(Regex("z"), "θ|s"),

            //9.  Q is always followed by ue or ui.
            // It is pronounced /K/, and the u is silent (quema /′kema/, quiso /′kiso/).
            Rule(Regex("que"), "ke", 3),
            Rule(Regex("qui"), "ki", 3),

            //3.  D is pronounced /d/ when it occurs at the beginning of an utterance
            //or after n or l (digo /′diƔo/, anda /′anda/, el dueño /el′dweɲo/)
            //and /ð/ in all other contexts (hada /′aða/, arde /′arðe/, los dados /loz′ðaðos/).
            //It is often not pronounced at all at the end of a word (libertad /liβer′ta(ð)/,
            //Madrid /ma′ðri(ð)/).
            Rule(Regex("^d"), "d"),
            //todo: match this rule across word boundaries
            Rule(Regex("ld"), "ld", 2),
            Rule(Regex("nd"), "nd", 2),
            Rule(Regex("d"), "ð"),

            //6.  J is always pronounced /x/ (jamón /xa′mon/, jefe /′xefe/).
            Rule(Regex("j"), "x"),

            //4.  G is pronounced /x/ when followed by e or i (gitano /xi′tano/, auge /′awxe/).
            Rule(Regex("g[ie]"), "x"),
            // Note that the u is not pronounced in the combinations gue and gui,
            // When followed by a, o, u, ue or ui it is pronounced /g/ if at the beginning of an utterance or after n
            // (gato /′gato/, gula/′gula/, tango /′taŋgo/, guiso /′giso/)
            Rule(Regex("^gui"), "gi", 3),
            Rule(Regex("^gue"), "ge", 3),
            Rule(Regex("^g[aou]"), "g"),
            // and /Ɣ/ in all other contexts (hago /′aƔo/, trague /′traƔe/, alga /′alƔa/, águila /′aƔila/).
            Rule(Regex("g"), "Ɣ"),
            // unless it is written with a diaeresis (paragüero /para′Ɣwero/, agüita /a′Ɣwita/).
            Rule(Regex("güi"), "Ɣwi", 3),
            Rule(Regex("güe"), "Ɣwe", 3),

            //The double consonant rr is always pronounced /rr/.
            //the letter /r/ represents the trilled r in IPA
            Rule(Regex("rr"), "r", 2),

            //13.  X is pronounced /ks/, although there is a marked tendency to render it as /s/ before consonants,
            //especially in less careful speech (extra /′ekstra/, /′estra/, or in some dialects /′ehtra/, see 11 above).
            //In some words derived from Nahuatl and other Indian languages it is pronounced /x/ (México /′mexiko/)
            //and in others it is pronounced /s/ (Xochlmllco /sotʃi′milko/).
            Rule(Regex("x"), "ks"),

            //7. The pronunciation of ll varies greatly throughout the Spanish-speaking world.

            //(a) LL is pronounced rather like the y in English yes by the majority of speakers,
            //who do not distinguish between the pronunciation of ll and that of y
            //(e.g. between haya and halla).
            //The sound is pronounced slightly more emphatically when at the beginning of an utterance.

            //(b) In some areas, particularly Bolivia, parts of Peru and Castile in Spain,
            //the distinction between ll and y has been preserved.
            //In these areas ll is pronounced with the tongue against the palate,
            //the air escaping through narrow channels on either side.
            //(The nearest sound in English would be that of lli in million).

            //(c) In the River Plate area ll is pronounced /Ʒ/ (as in English measure),
            //with some speakers using a sound which tends toward /ʃ / (as in shop).

            //When phonetic transcriptions of Spanish headwords containing ll are given in the dictionary,
            //the symbol /J/ is used to represent the range of pronunciations described above.
            Rule(Regex("ll"), "ʎ|ʝ", 2),


            //Spanish has both strong vowels (a, e, o) and weak vowels (i, u).
            //Here are some rules on how the combinations of these vowels are divided into syllables.

            //Two weak vowels together form a diphthong and are not separated into different syllables.
            ///wa/ 	cuadro 	picture
            ///we/ 	fuego 	fire
            ///wi/[62] 	buitre 	vulture
            ///wo/ 	cuota 	quota
            Rule(Regex("u[aeio]"), "w"),
            ///ja/ 	hacia 	towards
            ///je/ 	tierra 	earth
            ///ju/ 	viuda 	widow
            ///jo/ 	radio 	radio
            Rule(Regex("i[aeou]"), "j"),

            //A weak vowel and a strong vowel together form a diphthong and are not separated into different syllables.
            //Two strong vowels together form a hiatus and are separated into different syllables. Example: Leo

            //Falling
            ///ai/ 	aire 	air
            ///au/ 	pausa 	pause
            ///ei/ 	rey 	king
            Rule(Regex("ey"), "ei", 2),
            ///eu/ 	neutro 	neutral
            ///oi/ 	hoy 	today
            Rule(Regex("oy"), "oi", 2)
            ///ou/[61] 	bou 	seine fishing


//syllables:
        //we either need to be able to produce
        //1. the number of syllablesin a word, the number of the current syllable we're in,
        //and the position in the current syllable (onset, nucleus, coda). Or
        //2. A list of ranges, that correspond to the start and end of each syllable,
        //and something to tell whether a letter is eg syllable-final or not
        )

        var processingWord = nativeText.toLowerCase()
        loop@ while(processingWord.isNotEmpty()) {
            for (i in 0 until rules.size) {
                //if the rule matches the start of the remaining string
                if(rules[i].matcher.find(processingWord)?.range?.start == 0) {
                    //System.out.println("rule '${rules[i]}' matches '$processingWord'")
                    if(rules[i].outputString.contains("|")) {
                        //there's a difference in pronunciation between european and american spanish
                        //european on the left, american on the right of the pipe
                        val variants = rules[i].outputString.split("|")
                        european.append(variants[0])
                        american.append(variants[1])
                    }
                    else{//otherwise, they're the same
                        american.append(rules[i].outputString)
                        european.append(rules[i].outputString)
                    }

                    processingWord = processingWord.substring(rules[i].lettersConsumed)
                    continue@loop
                }
            }
            //no rule matched; the spanish orthography matches the IPA.
            //just copy it to the output
            american.append(processingWord[0])
            european.append(processingWord[0])
            processingWord = processingWord.substring(1)
        }

        american.append('/')
        european.append('/')
    return setOf(
        Variant("American", american.toString()),
        Variant("Peninsular", european.toString())    )
    }
}