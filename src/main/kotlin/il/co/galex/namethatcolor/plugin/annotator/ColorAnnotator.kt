package il.co.galex.namethatcolor.plugin.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.xml.XmlTextImpl
import com.intellij.psi.util.elementType
import com.intellij.psi.util.findParentOfType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlFile
import il.co.galex.namethatcolor.core.manager.ColorNameFinder
import il.co.galex.namethatcolor.core.model.HexColor
import il.co.galex.namethatcolor.plugin.intention.NameColorIntention
import il.co.galex.namethatcolor.plugin.util.*
import org.jetbrains.kotlin.lexer.KtTokens.*
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.psiUtil.getPrevSiblingIgnoringWhitespaceAndComments

class ColorAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {

        try {
            element.findParentOfType<KtFile>()?.let {
                val color = HexColor(element.text)
                val prev = element.getPrevSiblingIgnoringWhitespaceAndComments(false)
                if (prev?.text in setOf("fun", "val")) return
                if (element.elementType == REGULAR_STRING_PART || element is KtLiteralStringTemplateEntry) return
                if (element.elementType == INTEGER_LITERAL) return
                if (prev?.elementType == EQ && element is KtConstantExpression) return
//                Log.info("prev ${prev.toString()} ${prev?.text} ${prev?.javaClass?.name}")
//                Log.info("elem ${element.toString()} ${element.javaClass.name} ${color.input}")

                // color list from name that color
                holder.newAnnotation(HighlightSeverity.ERROR, COLOR_ANNOTATION_MESSAGE_KT)
                    .newFix(NameColorIntention(NAME_THAT_COLOR, color, ColorNameFinder::findColor, EnumColorOutput.KT))
                    .registerFix()
                    .create()

                // material color palette
                holder.newAnnotation(HighlightSeverity.ERROR, MATERIAL_COLOR_ANNOTATION_MESSAGE_KT)
                    .newFix(NameColorIntention(NAME_THAT_MATERIAL_COLOR, color, ColorNameFinder::findMaterialColor, EnumColorOutput.KT))
                    .registerFix()
                    .create()
            }
        } catch (_: Exception) {}


        try {
            element.parentOfType<XmlFile>()?.let {
                // we found a value that is a valid color, checking that it is not already in a <color> tag
                if ((element.parent as XmlTextImpl).parentTag?.name == RESOURCES_TAG_NAME) {
                    val color = HexColor(element.text)

                    // color list from name that color
                    holder.newAnnotation(HighlightSeverity.ERROR, COLOR_ANNOTATION_MESSAGE)
                            .newFix(NameColorIntention(NAME_THAT_COLOR, color, ColorNameFinder::findColor, EnumColorOutput.XML))
                            .registerFix()
                            .create()

                    // material color palette
                    holder.newAnnotation(HighlightSeverity.ERROR, MATERIAL_COLOR_ANNOTATION_MESSAGE)
                            .newFix(NameColorIntention(NAME_THAT_MATERIAL_COLOR, color, ColorNameFinder::findMaterialColor, EnumColorOutput.XML))
                            .registerFix()
                            .create()
                }
            }
        } catch (e: Exception) {
            // found that a color is not valid for us, ignoring it
        }
    }
}