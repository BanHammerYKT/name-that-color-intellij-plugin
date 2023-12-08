package il.co.galex.namethatcolor.plugin.intention

import com.esotericsoftware.kryo.kryo5.minlog.Log
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.lang.ASTFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlText
import il.co.galex.namethatcolor.core.model.Color
import il.co.galex.namethatcolor.core.model.HexColor
import il.co.galex.namethatcolor.core.util.toKtName
import il.co.galex.namethatcolor.core.util.toXmlName
import il.co.galex.namethatcolor.plugin.util.EnumColorOutput
import il.co.galex.namethatcolor.plugin.util.colorOutput
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.elementsInRange

@Suppress("DialogTitleCapitalization")
class NameColorIntention(
        private val text: String,
        private val hexColor: HexColor,
        private val find: (color: HexColor) -> Pair<HexColor, Color>,
        private val colorOutput: EnumColorOutput) : IntentionAction {

    override fun getFamilyName(): String = "Name That Color"
    override fun getText(): String = text
    override fun startInWriteAction(): Boolean = true
    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (file is KtFile) {
            editor?.caretModel?.offset?.let { offset ->
                file.elementsInRange(TextRange(offset - 10, offset + 10)).forEach { oldElement ->
                    if (oldElement.text?.contains(hexColor.input) == true) {
                        Log.info("${oldElement.toString()} ${oldElement.javaClass}")
                        val (hexColor, color) = find(HexColor(hexColor.input))
                        val name = color.name.toKtName(hexColor.percentAlpha)
                        val insert = colorOutput(name, hexColor.toString(), colorOutput)
                        val newElement: PsiElement = KtPsiFactory.contextual(oldElement).createProperty(insert)
                        oldElement.replace(newElement)
                        return@forEach
                    }
                }
            }
        }

        if (file is XmlFile) {
            file.rootTag?.let { rootTag ->
                val elements = rootTag.children.filterIsInstance<XmlText>()
                elements.forEach { oldElement ->
                    if (oldElement.text.contains(hexColor.input)) {

                        val (hexColor, color) = find(HexColor(hexColor.input))
                        val name = color.name.toXmlName(hexColor.percentAlpha)
                        val insert = colorOutput(name, hexColor.toString(), colorOutput)

                        var newElement: PsiElement = XmlElementFactory.getInstance(project).createTagFromText(insert)
                        val split = oldElement.text.split(hexColor.input)
                        newElement = oldElement.replace(newElement)
                        // keep what was before and after the entered color
                        if (split.isNotEmpty()) rootTag.addBefore(insert(project, split[0]), newElement)
                        if (split.size > 1) rootTag.addAfter(insert(project, split[1]), newElement)

                        // get out of our loop if we found one to replace
                        return@forEach
                    }
                }
            }
        }
    }

    private fun insert(project: Project, text: String): XmlText {
        val tagFromText = XmlElementFactory.getInstance(project).createTagFromText("<a>$text</a>")
        val textElements = tagFromText.value.textElements
        return if (textElements.isEmpty()) ASTFactory.composite(XmlElementType.XML_TEXT) as XmlText else textElements[0]
    }
}