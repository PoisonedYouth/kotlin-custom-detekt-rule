package org.example.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class SingletonComponentsShouldBeStatelessRuleTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `reports usages for service with variable property state`() {
        val code =
            """
            import org.springframework.stereotype.Service                

            @Service
            class UserService {
                private var state: String = "test"
                
                fun doAnything(){
                    println("Do business logic")
               }

            }
             """.trimIndent()

        val findings = SingletonComponentsShouldBeStatelessRule(Config.empty)
            .compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `does not reports usages for service with fix property state`() {
        val code =
            """
            import org.springframework.stereotype.Service                

            @Service
            class UserService {
                private val state: String = "test"
                
                fun doAnything(){
                    println("Do business logic")
               }

            }
             """.trimIndent()

        val findings = SingletonComponentsShouldBeStatelessRule(Config.empty)
            .compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `reports usages for service with top-level state`() {
        val code =
            """
            import org.springframework.stereotype.Service       

            private val state: String = "test"

            @Service
            class UserService {
                fun doAnything(){
                    println("Do business logic")
               }

            }
             """.trimIndent()

        val findings = SingletonComponentsShouldBeStatelessRule(Config.empty)
            .compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `reports usages for multiple services`() {
        val code =
            """
            import org.springframework.stereotype.Service       

            private val state: String = "test"

            @Service
            class UserService {
                fun doAnything(){
                    println("Do business logic")
               }

            }
            @Service
            class CustomerService {
                fun doAnything(){
                    println("Do business logic")
               }

            }

             """.trimIndent()

        val findings = SingletonComponentsShouldBeStatelessRule(Config.empty)
            .compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `reports usages for service with const state`() {
        val code =
            """
            import org.springframework.stereotype.Service       

            @Service
            class UserService {
                const val TEST: String = "TEST"
                fun doAnything(){
                    println("Do business logic")
               }

            }
             """.trimIndent()

        val findings = SingletonComponentsShouldBeStatelessRule(Config.empty)
            .compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `does not report for service without state`() {
        val code = """
            import org.springframework.stereotype.Service

            @Service
            class UserRepository{
               fun doAnything(){
                    println("Do business logic")
               }
            }
    """

        val findings = SingletonComponentsShouldBeStatelessRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }
}
