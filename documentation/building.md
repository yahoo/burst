![Burst](burst_h.png "")

# Building Burst

Let's make sure you can successfully build the Burst source tree. It should be easy after taking care of a few basics,
setting up [Maven](https://maven.apache.org/) and your [Java JDK](https://www.java.com/) and letting her rip.

## Background

It may be quite basic and obvious at this point for most of you, but for the sake of completeness it is worth stating
that Burst is a [Github](https://github.com/) repository. You will need the Burst Git repo cloned somewhere in a
suitable file system and an associated bash like shell environment where you can execute a previously installed Maven in
the previously cloned Burst repository root folder. We will assume that the reader has taken care of this prelude before
continuing.

### Scala

While the main Burst implementation source language is [Scala](https://www.scala-lang.org/), in terms of building the
tree, you don't need any special infrastructure installed. The Scala language along with all dependencies is installed
automatically by Maven during the maven build. If you want to browse, customize, troubleshoot, or extend Burst you may
need to think about Scala more deeply but generally speaking Burst Scala is relatively tame and Java programmers should
have little problem following and working with the Burst source code. For now lets not worry about it.

### Java JDKs

Setting up your Java environment is a bit more nuanced. Burst currently supports `OpenJDK 8`, but plans to support LTS
JDKs (_**TODO**_ let's get this locked down). Please install the JDK you want to use and make sure your **JAVA_HOME**
variable is set appropriately.

## Local Builds

You ___must___ have `git` and `java` installed locally. Burst is currently built against OpenJDK8. You must have your __
JAVA_HOME__ environment variable set to the path of a version 8 JDK. Maven installs all other libraries from there (
including Scala). Go to the root of the tree and execute:

```shell
./mvnw clean install
```

This will build the tree and create all auto-generated files in the build such as Antlr parsers.

## CI Builds and Versioning

Burst's CI build will automatically publish artifacts when:

- A PR is opened or updated, with a version like `1.2.3-alpha-456-SNAPSHOT` for PR #456
- A PR is merged to the development branch, with a version like `1.2.3-SNAPSHOT`
- Branches intended for ad-hoc testing, with a version like `1.2.3-beta-branchwithoutpuncutaiont-SNAPSHOT`
    - Branches intended for ad-hoc testing are branches whose names end in `-ad-hoc`
- A change occurs to a release branch, with a version like `1.2.3`. A change on a relase branch can be:
    1. the branch is pushed for the first time (it is released)
    1. a PR is merged into a relase branch (it is patched)
    1. a commit is pushed directly to the release branch (discouraged)

This process is facilitated by including the [maven ci-friendly](https://maven.apache.org/maven-ci-friendly.html)
variable `${revision}` in pom versions (except for released artifacts).

### Shading

Most of the configuation for the `maven-shade-plugin` is handled in the root `pom.xml`, child modules should only
specify the configuation for what to shade and how to shade. Child poms should have shading configuration similar to:

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>${shade.maven-plugin.version}</version>
    <configuration>
        <artifactSet/>
        <filters/>
        <relocations/>
        <transformers/>
    </configuration>
</plugin>
```

### Publishing a new module

In order to publish a new module you need to make two small changes to its `pom.xml`:

```xml

<properties>
    <artifact.skip-deploy>false</artifact.skip-deploy>
</properties>
```

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-deploy-plugin</artifactId>
            <version>${deploy.maven-plugin.version}</version>
        </plugin>
    </plugins>
</build>
```

Everything else required to publish a new module is configured in the root `pom.xml`. There are no additional changes
required for shaded modules, the `maven-shade-plugin` understands the `maven-deploy-plugin` and configures the project
as required.

## Cutting a release branch

Creating a new release branch is handled by the script in `scripts/branch-for-release.sh`. The script should be invoked
from the main development branch, and requires a single argument of the next version to use for the project. The version
should be of the form MAJOR.MINOR, e.g. 3.11 or 4.0.

If the current version is 3.10, and the script is invoked `./scripts/branch-for-release.sh 3.11` then the script will:

- create a new branch called `burst-3.10`
- return to master and create a new branch called `rev-to-3.11`
- update the poms in `rev-to-3.11` to the version `3.11.0${revision}-SNAPSHOT` and commit the pom changes

After the script has finished a developer should:

- push the branch `burst-3.10` to github in order to trigger a CI build that will deploy artifacts with
  version `3.10.0` (and also rev the `burst-3.10` branch to version `3.10.1${revision}-SNAPSHOT`)
- push and create a PR with the branch `rev-to-3.11` to change the pom version in the master branch.

The full expected command sequence for releasing 3.10 follows:

```shell
# run the script
./scripts/branch-for-release.sh 3.11
# checkout the branch for the new release and push it to github
git checkout burst-3.10
git push -u origin burst-3.10
# checkout the branch to update development to 3.11 and push it to github
git checkout rev-to-3.11
git push -u origin rev-to-3.11
# create a PR on github for rev-to-3.11 -> development branch
```

---
------ [HOME](../readme.md) --------------------------------------------
