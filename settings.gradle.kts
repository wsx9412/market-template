pluginManagement {
    repositories {
        google()
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

rootProject.name = "MarketDomain"

// 공개 저장소에는 도메인 계층만 담았습니다.
// 실제 앱은 여기에 :app, :data-local, :data-remote, :data-firebase 가 더해집니다.
include(":domain")
