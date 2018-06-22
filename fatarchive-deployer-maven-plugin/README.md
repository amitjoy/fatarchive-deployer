## Why?

This maven plugin is responsible to deploy the JARs contained inside the specified composite maven dependencies.

-----------------------------------------------------------------

### Contribution [![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/amitjoy/fatjar-maven-plugin/issues)

Want to contribute? Great! Check out [Contribution Guide](https://github.com/amitjoy/fatjar-maven-plugin/blob/master/CONTRIBUTING.md)

----------------------------------------------------------------

#### Project Import

**Import as Maven Project**

Import the project as Existing Maven Projects (`File -> Import -> Maven -> Existing Maven Projects`)

----------------------------------------------------------------

#### Building from Source

Run `mvn clean install -Dgpg.skip` in the project root directory

----------------------------------------------------------------

### License

This project is licensed under EPL-1.0 [![License](http://img.shields.io/badge/license-EPL-blue.svg)](http://www.eclipse.org/legal/epl-v10.html)

-----------------------------------------------------------------

### Usage

```xml
<build>
		<plugins>
			<plugin>
				<groupId>com.amitinside</groupId>
				<artifactId>fatarchive-deployer-maven-plugin</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<configuration>
					<extensionsToUnarchive>
						<param>par</param>
						<param>zip</param>
					</extensionsToUnarchive>
				</configuration>
			</plugin>
		</plugins>
	</build>
```

```
mvn fatjar:makefat
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://maven.apache.org/POM/4.0.0"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.amitinside</groupId>
	<artifactId>com.amitinside.repo</artifactId>
	<version>1.0.0</version>
	<name>My Repo</name>
	<packaging>pom</packaging>

	<repositories>
		<repository>
			<id>public</id>
			<url>http://a/b/public</url>
		</repository>
		<repository>
			<id>snapshots</id>
			<url>http://a/b/snapshots</url>
		</repository>
	</repositories>

	<properties>
		<my.non-jar.version>2.1.2</my.non-jar.version>
		<my.jar1.version>1.0.3</my.jar1.version>
		<my.jar2.version>1.2.12</my.jar2.version>
		<bundle.symbolic.name>com.my.first.fat.jar.bundle</bundle.symbolic.name>
		<bundle.version>1.0.0</bundle.version>
		<file.store.location>../repo/</file.store.location>
	</properties>

	<dependencies>
		<dependency>
			<groupId>com.a</groupId>
			<artifactId>b.c</artifactId>
			<version>${my.non-jar.version}</version>
			<type>zip</type>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>com.amitinside</groupId>
				<artifactId>fatarchive-deployer-maven-plugin</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<configuration>
					<extensionsToUnarchive>
						<param>par</param>
						<param>zip</param>
					</extensionsToUnarchive>
				</configuration>
			</plugin>
		</plugins>
	</build>
</project>
```
-----------------------------------------------------------------
