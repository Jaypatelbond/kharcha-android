pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Kharcha"
include(":kharcha")
include(":core:model")
include(":core:designsystem")
include(":core:common")
include(":core:database")
include(":core:datastore")
include(":core:domain")
include(":core:data")
include(":experiences:list:list-public")
include(":experiences:list:list-private")
include(":experiences:list:list-wiring")
include(":experiences:dashboard:dashboard-public")
include(":experiences:dashboard:dashboard-private")
include(":experiences:dashboard:dashboard-wiring")
include(":experiences:history:history-public")
include(":experiences:history:history-private")
include(":experiences:history:history-wiring")
include(":experiences:addtransaction:addtransaction-public")
include(":experiences:addtransaction:addtransaction-private")
include(":experiences:addtransaction:addtransaction-wiring")
