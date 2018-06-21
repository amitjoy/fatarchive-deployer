## Why?

This maven plugin is responsible to create a Fat JAR bundle containing all the dependencies 
	specified in a POM file. Sometimes users need to unpack specific dependency (eq. zip, tar etc) 
	before packing the dependency as this dependency could contain other jar files. The primary 
	motive is to collect all the dependencies including the plain jar dependencies and the other 
	jar dependencies that reside in other dependency (eq. zip, tar etc). Apart from this, it could 
	also update the versions of the specified dependencies before wrapping in a big fat JAR bundle. 
	This internally uses bnd to wrap all the dependencies in a single fat JAR.
	
<i>The idea behind was to primarily update all the mentioned dependency versions in the POM and pack them (Plain JARs + JARs contained in non-JARs) in a big fat JAR.</i>

-----------------------------------------------------------------

## Primary Functions

1. Update all POM dependencies automatically based on available updates from the POM specified repositories
2. Unpack the dependencies containing JARs (e.g zip, tar, tar.gz, any other archive etc)
3. Pack all POM specified JAR dependencies together with the JARs contained in other non-JAR archives (e.g zip, tar, tar.gz etc)
4. Pack all these JARs to a Fat JAR OSGi Bundle by copying binaries and exporting them
5. Copy the Fat JAR Bundle to user-specific location

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
<plugin>
  <groupId>com.amitinside</groupId>
  <artifactId>fatjar-maven-plugin</artifactId>
  <version>0.0.1</version>
  <configuration>
    <bundleSymbolicName>${bundle.symbolic.name}</bundleSymbolicName> <!-- mandatory -->
    <bundleVersion>${bundle.version}</bundleVersion>                 <!-- mandatory -->
    <extensionsToUnarchive>                                          <!-- mandatory -->
        <param>zip</param>
        <param>tar</param>
    </extensionsToUnarchive>
    <targetDirectory>${file.store.location}</targetDirectory>        <!-- mandatory -->
    <targetFilename>com.mybundle.mybsn.fat.jar</targetFilename>      <!-- optional default - bsn-version.jar -->
    <updateDependencyVersions>true</updateDependencyVersions>        <!-- optional default - true -->
    <mavenLocation>/a/b/maven</mavenLocation>                        <!-- optional default environment variable -->
  </configuration>
</plugin>
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
		<!-- Add Non-JAR Dependencies Here -->
		<dependency>
			<groupId>com.a</groupId>
			<artifactId>b.c</artifactId>
			<version>${my.non-jar.version}</version>
			<type>zip</type>
		</dependency>

		<!-- Add JAR Dependencies Here -->
		<dependency>
			<groupId>com.x</groupId>
			<artifactId>y.z</artifactId>
			<version>${my.jar1.version}</version>
		</dependency>
		<dependency>
			<groupId>com.foo/groupId>
			<artifactId>bar</artifactId>
			<version>${my.jar2.version}</version>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>com.amitinside</groupId>
				<artifactId>fatjar-maven-plugin</artifactId>
				<version>0.0.1</version>
				<configuration>
					<bundleSymbolicName>${bundle.symbolic.name}</bundleSymbolicName>
					<bundleVersion>${bundle.version}</bundleVersion>
					<extensionsToUnarchive>
						<param>zip</param>
	                                        <param>tar</param>
					</extensionsToUnarchive>
					<targetDirectory>${file.store.location}</targetDirectory>
					<updateDependencyVersions>true</updateDependencyVersions>
				</configuration>
			</plugin>
		</plugins>
	</build>
</project>
```
-----------------------------------------------------------------
