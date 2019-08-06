#!/usr/bin/env bash
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

# 1. Tags the HEAD on the 'master' branch with 
#    'myriad-<version>-incubating-rc<RC#>'.
# 2. Creates release artifacts and signs them with GPG key.
# 3. Uploads the release artifacts to "dev" SVN.

test ${#} -eq 2 || \
  { echo "Usage: `basename ${0}` [myriad version] [rc #]"; exit 1; }

VERSION=${1}
RC=${2}

TAG="myriad-${VERSION}-incubating-rc${RC}"
GIT_URL="https://gitbox.apache.org/repos/asf/incubator-myriad.git"
WORK_DIR="."

echo "Preparing a release candidate ${TAG}.."

TEMP="temp"
# clone the repo to "temp"
git clone $GIT_URL ${TEMP}|| \
  { echo "Failed to clone Myriad from ${GIT_URL} into ${TEMP}"; exit 1; }

# perform a build
pushd ${TEMP}
./gradlew build || \
  { echo "Failed to build Myriad."; exit 1; }

echo "Creating git tag ${TAG} and pushing to ${GIT_URL} ...${NORMAL}"
git tag ${TAG}
git push ${GIT_URL} refs/tags/${TAG}

popd # temp

# make a fresh clone that syncs to ${TAG}
git clone $GIT_URL ${TAG} --branch ${TAG} || \
  { echo "Failed to clone Myriad from ${GIT_URL}"; exit 1; }

# Remove files/folders that shouldn't be included in the release artifacts
rm -rf ${TAG}/website
rm -rf ${TAG}/support
rm -rf ${TAG}/gradlew.bat
rm -rf ${TAG}/gradlew
rm -rf ${TAG}/gradle/wrapper
rm -rf ${TAG}/.travis.yml
rm -rf ${TAG}/myriad-scheduler/src/main/resources/banner.txt

# Create a tar ball that excludes VCS files
TARBALL=${TAG}.tar.gz
tar czf ${TARBALL} --exclude-vcs ${TAG} || \
  { echo "Failed to create a tarball of ${TAG}"; exit 1; }

# Sign the tarball.
echo "Signing the distribution ..."
gpg --armor --output ${TARBALL}.asc --detach-sig ${TARBALL} || \
  { echo "Failed to sign the tarball ${TARBALL}"; exit 1; }

# Create SHA512 checksum.
echo "Creating a SHA512 checksum..."
gpg --print-md SHA512 ${TARBALL} > ${TARBALL}.sha512 || \
  { echo "Failed to create SHA512 checksum for tarball ${TARBALL}"; exit 1; }

SVN_DEV_REPO="https://dist.apache.org/repos/dist/dev/incubator/myriad"
SVN_DEV_LOCAL="${WORK_DIR}/dev"

echo "Checking out svn dev repo from ${SVN_DEV_REPO}..."

# Note '--depth=empty' ensures none of the existing files
# in the repo are checked out, saving time and space.
svn co --depth=empty ${SVN_DEV_REPO} ${SVN_DEV_LOCAL} || \
  { echo "Failed to checkout SVN repo from ${SVN_DEV_REPO} to ${SVN_DEV_LOCAL}"; exit 1; }

RELEASE_DIRECTORY="${SVN_DEV_LOCAL}/${TAG}"
mkdir ${RELEASE_DIRECTORY}
mv ${TARBALL} ${TARBALL}.asc ${TARBALL}.sha512 ${RELEASE_DIRECTORY}
echo "Release artifacts moved into ${RELEASE_DIRECTORY}"

pushd ${SVN_DEV_LOCAL}

while true; do
    read -p "PUSH THE ARTIFACTS TO ${SVN_DEV_REPO}/${TAG} ? " yn
    case $yn in
        [Yy]* ) break;;
        [Nn]* ) echo "OK. Goodbye..!"; exit;;
        * ) echo "Please answer yes or no.";;
    esac
done

echo "Uploading the artifacts (the distribution," \
  "signature and SHA) to ${SVN_DEV_REPO}/${TAG} ...."

svn add ${TAG}
svn commit -m "Adding ${TAG}."
popd # ${SVN_DEV_LOCAL}

echo "All good!"
