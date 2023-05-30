import java.net.URI

rootProject.name = "com.example.cinenexa-extension"

sourceControl {
    gitRepository(URI.create("https://github.com/Stormunblessed/cloudstream-3.git")) {
        producesModule("com.lagradost.cloudstream3:master")
    }
}