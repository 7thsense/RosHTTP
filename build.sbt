name := "RösHTTP root project"

crossScalaVersions := Seq("2.10.6", "2.11.7")
//isSnapshot := true

lazy val root = project.in(file(".")).
aggregate(scalaHttpJS, scalaHttpJVM)

lazy val scalaHttp = crossProject.in(file("."))
  .configure(InBrowserTesting.cross)
  .settings(
    name := "roshttp",
    version := "1.0.1.ss.0",
    bintrayOrganization := Some("7thsense"),
    scalaVersion := "2.11.7",
    organization := "fr.hmil",
    bintrayOrganization := Some("7thsense"),
    homepage := Some(url("http://github.com/hmil/RosHTTP")),
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    publishMavenStyle := true,

    libraryDependencies += "com.lihaoyi" %%% "utest" % "0.4.3",

    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .jvmSettings(
    // jvm-specific settings
  )
  .jsSettings(
    // js-specific settings
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.0",

    jsEnv := NodeJSEnv().value
  )

lazy val scalaHttpJVM = scalaHttp.jvm
lazy val scalaHttpJS = scalaHttp.js
