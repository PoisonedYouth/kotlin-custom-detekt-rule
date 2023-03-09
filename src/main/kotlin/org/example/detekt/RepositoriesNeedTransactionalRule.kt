package org.example.detekt

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.resolve.ImportPath

class RepositoriesNeedTransactionalRule(config: Config) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Warning,
        "@Repositories need to use @Transactional",
        Debt.FIVE_MINS,
    )

    private val imports = mutableListOf<KtImportDirective>()

    override fun visitImportList(importList: KtImportList) {
        super.visitImportList(importList)
        imports.addAll(importList.imports)
    }

    override fun visitClass(klass: KtClass) {
        val annotationEntries = klass.annotationEntries
        if (containsRepositoryAnnotation(annotationEntries) &&
            !containsTransactionalAnnotation(annotationEntries)
        ) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(
                        klass.getClassOrInterfaceKeyword()
                            ?: error("Missing class or interface keyword for '$klass'.")
                    ),
                    message = "All @Repository classes should also be annotated @Transactional to satisfy Exposed.",
                    metrics = emptyList(),
                    references = emptyList()
                )
            )
        }
    }

    private fun containsRepositoryAnnotation(annotationEntries: List<KtAnnotationEntry>): Boolean {
        return annotationEntries.map { it.shortName?.asString() }.contains("Repository")
                && importsContains("org.springframework.stereotype.Repository")
    }

    private fun containsTransactionalAnnotation(annotationEntries: List<KtAnnotationEntry>): Boolean {
        return annotationEntries.map { it.shortName?.asString() }.contains("Transactional")
                && importsContains("org.springframework.transaction.annotation.Transactional")
    }

    private fun importsContains(import: String): Boolean {
        return imports.map { it.importPath }.contains(ImportPath.fromString(import))
    }
}
