import org.scalajs.linker.interface.ModuleSplitStyle

lazy val webgl = project.in(file("."))
  .enablePlugins(ScalaJSPlugin) // Enable the Scala.js plugin in this project
  .settings(
    scalaVersion := "3.4.0",
    scalacOptions ++= List("-deprecation", "-explain"),

    // Tell Scala.js that this is an application with a main method
    scalaJSUseMainModuleInitializer := true,
    //Compile/mainClass := Some("io.github.tsnee.webgl.App"),

    /* Configure Scala.js to emit modules in the optimal way to
     * connect to Vite's incremental reload.
     * - emit ECMAScript modules
     * - emit as many small modules as possible for classes in the "io.github.tsnee" package
     * - emit as few (large) modules as possible for all other classes
     *   (in particular, for the standard library)
     */
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("io.github.tsnee.webgl")))
    },

    /* Depend on the scalajs-dom library.
     * It provides static types for the browser DOM APIs.
     */
    libraryDependencies ++= List(
    	"org.scala-js" %%% "scalajs-dom" % "2.8.0",
	"com.raquo"    %%% "laminar"     % "16.0.0"
    )
  )