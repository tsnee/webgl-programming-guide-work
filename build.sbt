import org.scalajs.linker.interface.ModuleSplitStyle

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val webgl = project.in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion      := "3.4.0",
    semanticdbEnabled := true,
    scalacOptions ++= List("-explain"),

    // Tell Scala.js that this is an application with a main method
    scalaJSUseMainModuleInitializer := true,

    /* Configure Scala.js to emit modules in the optimal way to connect to Vite's incremental reload.
     * - emit ECMAScript modules
     * - emit as many small modules as possible for classes in the "io.github.tsnee" package
     * - emit as few (large) modules as possible for all other classes (in particular, for the standard library) */
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("io.github.tsnee.webgl"))
        )
    },
    libraryDependencies ++= List(
      "org.typelevel"      %%% "cats-core"   % "2.10.0",
      "io.github.iltotore" %%% "iron"        % "2.5.0",
      "io.github.iltotore" %%% "iron-cats"   % "2.5.0",
      "com.raquo"          %%% "laminar"     % "16.0.0",
      "org.scala-js"       %%% "scalajs-dom" % "2.8.0",
      "ai.dragonfly"       %%% "slash"       % "0.3.1",
      "org.scalameta"      %%% "munit"       % "0.7.29" % Test
    )
  )
