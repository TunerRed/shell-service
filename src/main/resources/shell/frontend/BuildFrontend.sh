
function echo_error() {
    echo -e "$1" 1>&2
}
function expect_git_cmd () {
  git $1 > /dev/null 2>&1
}

if [ $# -ne 6 ];
then
  echo_error "shell args error"
  exit 1
fi

GIT_path=$1
GIT_repo=$2
GIT_branch=$3
BUILD_script=$4

TAR_path=$5
BUILD_app_name=$6

SHELL_home_path=`pwd`

mkdir -p $GIT_path

source ./.bash_profile

if [ "$SASS_BINARY_PATH" == "" ] ;
then
  echo_error "no node-sass found"
  exit 1
fi

cd $GIT_path
if [ ! -d $GIT_repo ];
then
 #git clone $GIT_url/$GIT_repo.git
 echo_error "no repository found:$GIT_repo"
 exit 1
fi

cd $GIT_repo
git pull > /dev/null 2>&1
if [ `git branch | grep '*'| grep -E "$GIT_branch$" | wc -l` -eq 0 ];
then
  git checkout $GIT_branch > /dev/null
  git pull origin $GIT_branch > /dev/null
fi

npm install
if [ $? -ne 0 ] ;
then
  echo_error "Install Error"
  exit 1
fi

npm run $BUILD_script > build.log 2>&1
if [ $? -ne 0 ] ;
then
  echo_error "----- npm build error -----"
  exit 1
fi
rm -rf build.log

mv dist $BUILD_app_name
if [[ ! -d $BUILD_app_name ]]; then
  echo_error "No resource folder found: $BUILD_app_name"
  exit 1
fi
tar -czf $BUILD_app_name.tar.gz $BUILD_app_name/*

rm -r $BUILD_app_name
mkdir -p $TAR_path
mv $BUILD_app_name.tar.gz $TAR_path
