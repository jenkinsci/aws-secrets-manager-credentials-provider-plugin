def recentLTS = "2.504.3"
def configurations = [
    [ platform: "linux", jdk: "21", jenkins: null ],
    [ platform: "linux", jdk: "21", jenkins: recentLTS ],
]
buildPlugin(
    useContainerAgent: false, // ITs start a moto container via Testcontainers, which needs a Docker daemon
    configurations: configurations
)

