version := "0.1"

name := "sbt-plugin"

organization := "sbt-plugin"

sbtPlugin := true

publishTo  := Some("foo" at "dav://localhost:8008")

seq(aetherPublishSettings: _*)

wagons := Seq(aether.WagonWrapper("dav", new org.apache.maven.wagon.providers.webdav.WebDavWagon()))