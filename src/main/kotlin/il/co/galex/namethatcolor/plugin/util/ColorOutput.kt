package il.co.galex.namethatcolor.plugin.util

fun colorOutput(name: String, hexColor: String, colorOutput: EnumColorOutput) = when (colorOutput) {
    EnumColorOutput.XML -> "<color name=\"$name\">$hexColor</color>"
    EnumColorOutput.KT -> "val $name = Color.parseColor(\"$hexColor\")"
    EnumColorOutput.KT_COMPOSE -> "val $name = Color(0x${hexColor.drop(1)})"
}

enum class EnumColorOutput { XML, KT, KT_COMPOSE }