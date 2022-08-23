![Burst](../../../../../../../../../documentation/burst_h_small.png "")
![](../../../../../../../../doc/felt_small.png "")


# Felt Tree
The Felt tree  and its individual subtrees such as expressions,
go through a sequence of somtimes repeating phases.
Some of these are top level `all-of-tree`
operations and happen infrequently,
some are confined to a smaller subtree level and happen more often.
1. Construct -- `build the tree via the Hydra parser`
1. Validate -- `find errors`
1. Reduce -- `reduce tree or subtrees to static equivalents`
1. Infer -- `type resolve tree or subtrees via inference`
1. Bind -- `link names to references in tree or subtrees`
1. Normalize -- `output equivalent normalized Hydra form`
1. Reparse -- `take normalized form and build tree again`
1. Finalize -- `prepare tree for code generation`
1. Generate -- `generate directly executable scala code`
1. Compile -- `turn generated scala code into Java bytecode`

`code-generation` phase producing scala code that can then be
scala compiled at runtime to create a Java class closure which can directly scan
data.
