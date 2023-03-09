package org.example.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class RepositoriesNeedTransactionalTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `reports usages for @Repository without @Transactional`() {
        val code =
            """
            import org.springframework.stereotype.Repository            

            @Repository
            interface UserRepository {
                fun findAll() : List<User>
            }
             """.trimIndent()

        val findings = RepositoriesNeedTransactionalRule(Config.empty)
            .compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `does not report for @Repository with @Transactional`() {
        val code = """
            import org.springframework.transaction.annotation.Transactional
            import org.springframework.stereotype.Repository

            @Transactional
            @Repository
            interface UserRepository{
                fun findAll() : List<User>
            }
    """

        val findings = RepositoriesNeedTransactionalRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }
}
