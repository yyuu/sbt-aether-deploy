# SBT aether deploy plugin
Deploys sbt-artifacts using Sonatype aether. 
Aether is the same library as maven itself uses, meaning that the same behaviour should be expected.

## Caveat 
This plugin should not yet be used for publishing sbt plugins. There are an experimental branch for making this work.


# Configuration

## Basic configuration

Set up the repository and the credentials in `~/.sbt/global.sbt` or the project configuration.

```scala
publishTo <<= (version: String) {
  if (version.endsWith("SNAPSHOT") {
    Some("Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
  }
  else {
    Some("Sonatype Nexus Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  }
}

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
```

## Project configuration

### Plugins

Set up sbt-aether-plugin as sbt plugin of the project in `project/plugins.sbt`.

```scala
addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.9")
```

### Build file

```scala
seq(aether.Aether.aetherSettings: _*)
```

### Override default publish tasks

To override default `publish` task.

```scala
seq(aether.Aether.aetherPublishSettings: _*)
```

To override default `publish-local` task.

```scala
seq(aether.Aether.aetherPublishLocalSettings: _*)
```

They can be used together.


# Usage

## Standard usage

To deploy to remote Maven repository.

    sbt aether-deploy

To deploy to local Maven repository.

    sbt aether-install

## Usage if the publish tasks are overridden

To deploy to remote Maven repository.

    sbt publish

To deploy to local Maven repository.

    sbt publish-local


# Tips

## Proxies

Documentation for proxies can be found [here](http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html)

## Using the plugin with sbt-pgp-plugin 0.8

Previously the [sbt-pgp-plugin](https://github.com/sbt/sbt-pgp) hooked into the published-artifacts task, 
and this plugin does the same. This is no longer the case.

## Workaround until code is updated

```scala
seq(aether.Aether.aetherSettings: _*)

aetherArtifact <<= (coordinates, Keys.`package` in Compile, makePom in Compile, signedArtifacts in Compile) map {
  (coords: MavenCoordinates, mainArtifact: File, pom: File, artifacts: Map[Artifact, File]) =>
    aether.Aether.createArtifact(artifacts, pom, coords, mainArtifact) 
}
```

This should now allow aether-deploy task to work with the sbt-pgp-plugin

## Overriding the publish-signed task

```scala
publishSigned <<= deploy
```
   
## Using .scala file

To use the plugin in a .scala file you have to import it like this:

```scala
import aether.Aether._
```
