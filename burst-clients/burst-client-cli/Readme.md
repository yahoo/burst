# Burst Client CLI

The client CLI module vends two products. First, it defines a fully functional CLI to create domains and views, and to
execute queries. Second, it provides a core CLI service that can be repackaged by cluster owners to include connection
details for a Burst cluster, or collection of Burst clusters, so that cluster users need not be aware of cluster
implementation details.

## Using the provided CLI

Check the usage of the CLI:
```shell
# download burst-cli-VERSION.jar for your version of burst
java -jar burst-cli.jar --help
```

Ensure a domain exists in the cell:
```shell
java -jar burst-cli-3.13.0.jar [--connection connection.json] EnsureDomain -f domain.json
```

## Repackageing the CLI for your customers

- Add `org.burstsys:burst-client-cli` as a dependency of your project
- Subclass `BurstCli` providing a `getConnection` method that resolves your burst cells
- Add a main class that parses command-line arguments (you can use `BurstCliMain.scala` as a reference)
and pass your parsed arguments to your subclassed BurstCli
- Publish an uber jar, either with the `maven-shade` or `maven-assembly` plugin
