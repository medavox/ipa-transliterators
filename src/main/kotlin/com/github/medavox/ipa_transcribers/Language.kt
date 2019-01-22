package com.github.medavox.ipa_transcribers

import com.github.medavox.ipa_transcribers.Language.*

sealed class Language(val code: String) {
    //object Spanish : Language("es")
    sealed class Spanish : Language("es") {
        object Peninsular:Spanish()
        object PanAmerican:Spanish()
    }
    sealed class English : Language("en") {
        object American : English()
        object British : English()
    }
    object Arabic : Language("ar") //I intend this to mean classical Arabic which, thanks to the Qu'ran,
    // should be understood by most arabic dialect speakers
    object Hindi : Language("hi")
    object Bengali : Language("bn")
    object Portuguese : Language("pt")
    object Russian : Language("ru")
    object Japanese : Language("ja")
    object Punjabi : Language("pa") //listed on Ethnologue as Lahnda with related dialects,
    // but I've excluded them here.
    object Javanese : Language("jw")
    object Turkish : Language("tr")
    object Korean : Language("ko")
    object French : Language("fr")
    object German : Language("de")
    object Telugu : Language("te")
    object Marathi : Language("mr")
    object Urdu : Language("ur")
    object Vietnamese : Language("vi")
    object Tamil : Language("ta")
    object Italian : Language("it")
    object Persian : Language("fa")
    object InternationalPhoneticAlphabet : Language("ipa")
}
