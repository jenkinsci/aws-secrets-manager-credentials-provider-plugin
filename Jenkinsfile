def recentLTS = "2.222.4"
def configurations = [
    [ platform: "linux", jdk: "8", jenkins: null ],
    [ platform: "linux", jdk: "8", jenkins: recentLTS, javaLevel: "8" ],
    [ platform: "linux", jdk: "11", jenkins: recentLTS, javaLevel: "8" ],
]
// useAci is a workaround for JDK 8u292 bug that breaks some tests
buildPlugin(configurations: configurations, useAci: true)
