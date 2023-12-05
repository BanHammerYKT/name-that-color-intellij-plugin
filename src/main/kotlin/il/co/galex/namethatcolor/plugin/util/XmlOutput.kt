package il.co.galex.namethatcolor.plugin.util

fun xmlOutput(name: String, hexColor: String) = "<color name=\"$name\">$hexColor</color>"
fun ktOutput(name: String, hexColor: String) = "val $name = \"$hexColor\""