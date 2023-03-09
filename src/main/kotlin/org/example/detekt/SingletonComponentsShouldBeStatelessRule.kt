package org.example.detekt

import io.gitlab.arturbosch.detekt.api.*
import io.gitlab.arturbosch.detekt.rules.isConstant
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.resolve.ImportPath

class SingletonComponentsShouldBeStatelessRule(config: Config) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Warning,
        "Spring services annotated with @Service should not have a state.",
        Debt.FIVE_MINS,
    )

    private val imports = mutableListOf<KtImportDirective>()

    override fun visitImportList(importList: KtImportList) {
        super.visitImportList(importList)
        imports.addAll(importList.imports)
    }

    override fun visitClassBody(classBody: KtClassBody) {
        super.visitClassBody(classBody)
        if (containsServiceAnnotation(classBody.containingClass()?.annotationEntries) && hasState(classBody)) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(
                        classBody.containingClass()?.getClassOrInterfaceKeyword()
                            ?: error("Missing class or interface keyword for '${classBody.containingClass()}'.")
                    ),
                    message = "The class '${classBody.containingClass()?.name}' is annotated with @Service and should not have an internal state.",
                    metrics = emptyList(),
                    references = emptyList()
                )
            )
        }
    }

    override fun postVisit(root: KtFile) {
        super.postVisit(root)
        imports.clear()
    }

    private fun hasState(classBody: KtClassBody): Boolean {
        return classBody.properties.any { it.isMember && it.isVar && !it.isConstant() }
    }

    private fun containsServiceAnnotation(annotationEntries: List<KtAnnotationEntry>?): Boolean {
        return annotationEntries != null && annotationEntries.map { it.shortName?.asString() }.contains("Service")
                && containsImport("org.springframework.stereotype.Service")
    }

    private fun containsImport(import: String): Boolean {
        return imports.map { it.importPath }.contains(ImportPath.fromString(import))
    }
}
