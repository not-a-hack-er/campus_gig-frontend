package com.abpvt.campusgig_frontend.core.utils

/** Stable product configuration for the Greater Noida college selector. */
object CollegeCatalog {
    val greaterNoida: List<String> = listOf(
        "Accurate Institute of Management and Technology",
        "Bennett University",
        "Birla Institute of Management Technology (BIMTECH)",
        "G.L. Bajaj Institute of Technology and Management",
        "Galgotias College of Engineering and Technology",
        "Galgotias University",
        "Gautam Buddha University",
        "Greater Noida Institute of Technology (GNIOT)",
        "IIMT Group of Colleges",
        "ITS Engineering College",
        "Lloyd Institute of Engineering and Technology",
        "Noida Institute of Engineering and Technology (NIET)",
        "Sharda University",
        "United College of Engineering and Research, Greater Noida"
    ).distinct().sortedBy(String::lowercase)
}
