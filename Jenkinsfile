def recentLTS = "2.164.3"
def configurations = [
    [ platform: "linux", jdk: "8", jenkins: null ],
    [ platform: "linux", jdk: "8", jenkins: recentLTS, javaLevel: "8" ],
    [ platform: "linux", jdk: "11", jenkins: recentLTS, javaLevel: "8" ],
]
buildPlugin(
  findbugs: [run: true, archive: true],
  checkstyle: [run: true, archive: true],
  configurations: configurations
)
