function echo_error() {
    echo -e "$1" 1>&2
}

if [[ $# -ne 5 ]];
then
  echo_error "shell args error"
  exit 1
fi

GIT_PATH=$1
GIT_REPO=$2
GIT_BRANCH=$3
TAR_JAR_PATH=$4
TAR_PATH=$5
SHELL_HOME_PATH=`pwd`

if [[ ! -d "${GIT_PATH}/${GIT_REPO}" ]]; then
    echo_error "no repository found"
    exit 1
fi

cd ${GIT_PATH}/${GIT_REPO}
newest=`git reflog|awk 'NR==1'|awk '{print $1}'`
git reset --hard ${newest}
if [[ `git branch | grep "\*"| grep -E "$GIT_BRANCH$" | wc -l` -eq 0 ]]; then
  git checkout ${GIT_BRANCH}
fi

git pull origin ${GIT_BRANCH}
BUILD_LOG=BUILD.log
mvn clean install > ${BUILD_LOG} 2>&1
if [[ $? -ne 0 ]]; then
    echo_error "mvn install error"
    BUILD_INFO=`cat ${BUILD_LOG} | grep "ERROR"`
    echo_error ${BUILD_INFO}
    exit 1
fi

mkdir -p ./${TAR_JAR_PATH}
mv ./${TAR_JAR_PATH}/*.jar ${SHELL_HOME_PATH}/${TAR_PATH}
exit 0
