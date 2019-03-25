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

# 1. Tags the successfully voted RC with
#    'myriad-<version>-incubating'.
# 2. Pushes the release tag to git.
# 3. Deletes the previously tagged RC tags.
# 4. Uploads the release artifacts to "release" SVN.
# 5. Deletes the previously available RC artifacts from "dev" SVN.

function askAndGo() {
while true; do
    read -p "$1 ?" yn
    case $yn in
        [Yy]* ) break;;
        [Nn]* ) echo "OK. Goodbye..!"; exit;;
        * ) echo "Please answer yes or no.";;
    esac
done
}

#main
test ${#} -eq 2 || \
  { echo "Usage: `basename ${0}` [myriad version] [voted rc#]"; exit 1; }

VERSION=${1}
RC=${2}

RC_TAG="myriad-${VERSION}-incubating-rc${RC}"
RELEASE_TAG="myriad-${VERSION}-incubating"
GIT_URL="https://gitbox.apache.org/repos/asf/incubator-myriad.git"
WORK_DIR="."

echo "Preparing a release ${RELEASE_TAG}.."

TEMP="temp"
# clone the repo to "temp"
git clone $GIT_URL ${TEMP}|| \
  { echo "Failed to clone Myriad from ${GIT_URL} into ${TEMP}"; exit 1; }

pushd ${TEMP}
echo "Creating new release tag '${RELEASE_TAG}' and pushing it to ${GIT_URL} ..."
git tag ${RELEASE_TAG} ${RC_TAG}
echo "Git tags for version ${VERSION}: "
git log --pretty=oneline --abbrev-commit --decorate | grep "tag:" | grep ${VERSION}
askAndGo "Push tag '${RELEASE_TAG}' to ${GIT_URL}"
git push origin refs/tags/${RELEASE_TAG}

echo "Deleting (now) old RC git tags..."
for i in `git tag -l | grep ${VERSION} | grep "rc"`;
do
  git tag -d ${i}
  askAndGo "Delete tag '${i}' from ${GIT_URL}"
  git push origin :refs/tags/${i}
done
popd # temp

SVN_DEV_REPO="https://dist.apache.org/repos/dist/dev/incubator/myriad"
SVN_RELEASE_REPO="https://dist.apache.org/repos/dist/release/incubator/myriad"
SVN_DEV_LOCAL="${WORK_DIR}/dev"
SVN_RELEASE_LOCAL="${WORK_DIR}/release"

echo "Checking out svn dev repo from ${SVN_DEV_REPO} to ${SVN_DEV_LOCAL}..."
svn co ${SVN_DEV_REPO} ${SVN_DEV_LOCAL} || \
  { echo "Failed to checkout SVN repo from ${SVN_DEV_REPO} to ${SVN_DEV_LOCAL}"; exit 1; }

echo "Checking out svn release repo from ${SVN_RELEASE_REPO} to ${SVN_RELEASE_LOCAL}..."
svn co ${SVN_RELEASE_REPO} ${SVN_RELEASE_LOCAL} || \
  { echo "Failed to checkout SVN repo from ${SVN_RELEASE_REPO} to ${SVN_RELEASE_LOCAL}"; exit 1; }

mkdir ${SVN_RELEASE_LOCAL}/${RELEASE_TAG}
echo "Copying release artifacts under ${SVN_DEV_LOCAL}/${RC_TAG} to ${SVN_RELEASE_LOCAL}/${RELEASE_TAG}..."
cp ${SVN_DEV_LOCAL}/${RC_TAG}/${RC_TAG}.tar.gz ${SVN_RELEASE_LOCAL}/${RELEASE_TAG}/${RELEASE_TAG}.tar.gz
cp ${SVN_DEV_LOCAL}/${RC_TAG}/${RC_TAG}.tar.gz.asc ${SVN_RELEASE_LOCAL}/${RELEASE_TAG}/${RELEASE_TAG}.tar.gz.asc
cp ${SVN_DEV_LOCAL}/${RC_TAG}/${RC_TAG}.tar.gz.sha512 ${SVN_RELEASE_LOCAL}/${RELEASE_TAG}/${RELEASE_TAG}.tar.gz.sha512

pushd ${SVN_RELEASE_LOCAL}
echo "Release artifacts under ${SVN_RELEASE_LOCAL}/${RELEASE_TAG}:"
ls ${RELEASE_TAG}
askAndGo "PUSH THE ${RELEASE_TAG} ARTIFACTS TO ${SVN_RELEASE_REPO}/${RELEASE_TAG}"
svn add ${RELEASE_TAG}
svn commit -m "Adding ${RELEASE_TAG}."
popd # ${SVN_RELEASE_LOCAL}

askAndGo "PROCEED TO DELETE OLD RCs FROM ${SVN_DEV_REPO}/"
echo "Old RCs in ${SVN_DEV_REPO}:"
ls ${SVN_DEV_LOCAL} | grep ${VERSION}

### delete old RCs
pushd ${SVN_DEV_LOCAL}
for i in `ls | grep ${VERSION}`
do
  askAndGo "DELETE $i FROM ${SVN_DEV_REPO}/"
  svn delete $i
  svn commit -m "Deleting $i as ${RELEASE_TAG} is committed to ${SVN_RELEASE_REPO}."
done
popd # ${SVN_DEV_LOCAL}

echo "All good!"
askAndGo "Cleanup local repos"
rm -rf ${TEMP}
rm -rf ${SVN_DEV_LOCAL}
rm -rf ${SVN_RELEASE_LOCAL}
echo "All clean :)"

