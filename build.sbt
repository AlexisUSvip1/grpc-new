import sbtprotoc.ProtocPlugin.autoImport._

name := "grpc-new"
organization := "com.example"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.17"

// === Dependencias principales ===
libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test,

  // === Functional Programming ===
  "org.typelevel" %% "cats-core"   % "2.12.0",
  "org.typelevel" %% "cats-effect" % "3.5.4",

  "org.sangria-graphql" %% "sangria" % "4.0.0",
  "org.sangria-graphql" %% "sangria-circe" % "1.3.2",
  "org.sangria-graphql" %% "sangria-play-json" % "2.0.2",

  // === gRPC runtime ===
  "io.grpc" % "grpc-netty-shaded" % "1.66.0",
  "io.grpc" % "grpc-stub"         % "1.66.0",
  "io.grpc" % "grpc-protobuf"     % "1.66.0",

  // === ScalaPB runtime (Protocol Buffers + gRPC bindings) ===
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % "0.11.17"
)
Compile / PB.protoSources := Seq(
  baseDirectory.value / "src" / "main" / "protobuf"
)

Compile / PB.targets := Seq(
  scalapb.gen(grpc = true) -> (Compile / sourceManaged).value
)

// Fuerza que PB.generate corra antes de compile
Compile / sourceGenerators += Def.task {
  val files = (Compile / PB.generate).value
  println(s"✅ Generados: ${files.mkString(", ")}")
  files
}.taskValue
// --- Fix para el error "IO error while decoding ... (Is a directory)" ---
// --- Fix para el error "IO error while decoding ... (Is a directory)" ---
// --- FIX: evita intentar compilar directorios dentro de src_managed ---
Compile / unmanagedResourceDirectories := (Compile / unmanagedResourceDirectories).value.filterNot(_.getName == "src_managed")
Compile / unmanagedSourceDirectories := (Compile / unmanagedSourceDirectories).value.filterNot(_.getName == "src_managed")

// Filtra solo archivos reales .scala en managed sources
Compile / managedSources := (Compile / managedSources).value.filter(f => f.exists && f.isFile)

// Asegura encoding UTF-8
scalacOptions ++= Seq("-encoding", "UTF-8")