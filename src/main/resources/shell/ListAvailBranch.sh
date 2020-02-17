GIT_path=$1
GIT_pull=$2

function echo_error() {
    echo -e "$1" 1>&2
}

if [[ ! -d ${GIT_path} ]]; then
    echo_error "no repository found"
    exit 1
fi
cd $GIT_path

if [ "$GIT_pull" -eq "1" ] ; then
  git pull > /dev/null
fi

GIT_branch_list=(`git branch -a |grep remotes|grep -v HEAD`)

for GIT_branch in ${GIT_branch_list[@]} ; do
    echo "${GIT_branch##*/}"
done

exit 0
