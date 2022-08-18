#!/bin/sh

new_version=$1
dev_branch=main
project_name=burst

if [ "$new_version" == "" ]; then
  echo "You need to provide a new version for the project to be used in the development branch"
  echo "The version should be like x.y (which will set a new version of x.y.0-SNAPSHOT)"
  exit 1
fi

if [ "$(git branch --show-current)" != "${dev_branch}" ]; then
  echo "You should only use the release script from the branch '${dev_branch}'"
  exit 1
fi

branch_version=$(./mvnw help:evaluate -Dexpression=project.version -DforceStdout -q -ntp | sed 's/.0-SNAPSHOT//')
branch_name=${project_name}-${branch_version}
echo "----------------------------------------"
echo "Creating release branch $branch_name"
echo "----------------------------------------"
echo

git checkout -b $branch_name

git checkout $dev_branch
git checkout -b rev-to-$new_version

echo "----------------------------------------"
echo "Creating branch to bump version: rev-to-$new_version"
echo "----------------------------------------"
echo

./mvnw -ntp versions:set versions:commit -DnewVersion=${new_version}.0\${d}\${rev}-SNAPSHOT

next_version=$(./mvnw help:evaluate -Dexpression=project.version -DforceStdout -q -ntp | sed 's/(${revision})?-SNAPSHOT//')

echo "# ${new_version}" > new-changelog.md
echo "## Breaking changes" >> new-changelog.md
echo >> new-changelog.md
echo "## Features">> new-changelog.md
echo >> new-changelog.md
echo "## Fixes">> new-changelog.md
echo >> new-changelog.md
cat CHANGELOG.md >> new-changelog.md
mv new-changelog.md CHANGELOG.md

echo
echo "----------------------------------------"
echo "New project version: ${next_version}"
echo "----------------------------------------"

git commit -a -m "Rev to version $new_version.0"
