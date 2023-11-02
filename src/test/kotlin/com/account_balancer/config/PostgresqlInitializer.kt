package com.account_balancer.config

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class PostgresqlInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        TestPropertyValues.of(
            "spring.datasource.url=${POSTGRESQL_CONTAINER.getJdbcUrl()}",
            "spring.datasource.username=${POSTGRESQL_CONTAINER.username}",
            "spring.datasource.password=${POSTGRESQL_CONTAINER.password}",
        )
            .applyTo(applicationContext.environment)
    }

    companion object {
        private var POSTGRESQL_CONTAINER: PostgreSQLContainer<*> =
            PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine").asCompatibleSubstituteFor("postgres"))
                .apply {
                    withDatabaseName("test")
                    withUsername("test")
                    withPassword("test")
                    start()
                }
    }
}