
function echo_error() {
    echo -e "$1" 1>&2
}
function expect_git_cmd () {
  #expect $SHELL_home_path/LoginAuth.sh $GIT_user $GIT_pass "git $1"
  git $1 > /dev/null 2>&1
}

if [ $# -ne 8 ];
then
  echo_error "shell args error"
  exit 1
fi

GIT_url=$1
GIT_user=$2
GIT_pass=$3
GIT_path=$4
GIT_repo=$5
GIT_branch=$6

BUILD_script=$7
BUILD_app_name=$8

SHELL_home_path=`pwd`

mkdir -p $GIT_path

source ./.bash_profile

if [ "$SASS_BINARY_PATH" == "" ] ;
then
  echo_error "no node-sass found"
  exit 1
fi

cd $GIT_path
#if [ ! -d $GIT_repo ];
#then
# expect_git_cmd "clone $GIT_url/$GIT_repo.git"
#fi
cd $GIT_repo
git pull > /dev/null 2>&1
if [ `git branch | grep '*'| grep -E "$Git_branch$" | wc -l` -eq 0 ];
then
  git checkout $Git_branch > /dev/null
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

mkdir -p $BUILD_app_name
rm -rf $BUILD_app_name

mv dist $BUILD_app_name
tar -czf $BUILD_app_name.tar.gz $BUILD_app_name/*

rm -rf $BUILD_app_name
mv $BUILD_app_name.tar.gz ../
