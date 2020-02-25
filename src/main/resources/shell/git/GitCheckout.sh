Git_path=$1
Git_branch=$2

function echo_error () {
  echo -e "$1" 1>&2
}

if [ ! -d $Git_path ];
then
  echo_error "no git path found:"+$Git_path
  exit 1
fi

cd $Git_path
#newest=`git reflog|awk 'NR==1'|awk '{print $1}'`
#git reset --hard $newest
if [ `git branch | grep "\*"| grep -E "$Git_branch$" | wc -l` -eq 0 ];
then
  git checkout $Git_branch
fi

exit 0
