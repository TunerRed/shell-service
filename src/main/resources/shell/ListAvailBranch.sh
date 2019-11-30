GIT_path=$1
GIT_user=$2
GIT_pass=$3

cd $GIT_path

git pull > /dev/null

GIT_branch_list=(`git branch -a |grep remotes|grep -v HEAD`)

for GIT_branch in ${GIT_branch_list[@]} ; do
    echo "${GIT_branch##*/}"
done

exit 0