GIT_path=$1
GIT_pull=$2

cd $GIT_path

if [ "$GIT_pull" -eq "1" ] ; then
  git pull > /dev/null
fi

GIT_branch_list=(`git branch -a |grep remotes|grep -v HEAD`)

for GIT_branch in ${GIT_branch_list[@]} ; do
    echo "${GIT_branch##*/}"
done

exit 0
