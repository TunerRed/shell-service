BASE_dir=`pwd`

DEPLOY_dir=$BASE_dir/$1
BACKUP_dir=$BASE_dir/$2
RUN_dir=$BASE_dir/$3

DATE_DAY=$(date +"%m%d")
DATE_TIME=$(date +"%Y%m%d_%H%M")

source ./.bash_profile

function echo_error() {
    echo -e "$1" 1>&2
}
function rename() {
    _filename=$1
    _prefix_list=('my-jar-' 'prefix-')
    _suffix_list=('.jar' '-0.0.1-SNAPSHOT')
    for _prefix in ${_prefix_list[@]} ; do
        _filename=${_filename#$_prefix}
    done
    for _suffix in ${_suffix_list[@]} ; do
        _filename=${_filename%$_suffix}
    done
    mv $1 _filename'.jar'
    echo _filename'.jar'
}

mkdir -p $DEPLOY_dir
if [ `ls $DEPLOY_dir|grep -E "jar$"|wc -l` -eq 0 ] ; then
  echo_error "no jars found under $DEPLOY_dir"
  exit 1
fi

mkdir -p $BACKUP_dir/$DATE_TIME
mkdir -p $RUN_dir

DEPLOY_files=(`ls $DEPLOY_dir|grep -E "jar$"`)
cd $RUN_dir
for DEPLOY_file in ${DEPLOY_files[@]} ; do
  DEPLOY_file=`rename $DEPLOY_file`
  appname=${DEPLOY_file%.jar}
  if [ `ls| grep -E "^$appname-[0-9]{4}.jar$"|wc -l` -gt 0 ] ; then
    old_file=(`ls $RUN_dir| grep -E "^$appname-[0-9]{4}.jar$"`)
    mv ${old_file[*]} $BACKUP_dir/$DATE_TIME/
  fi
  mv $DEPLOY_dir/$DEPLOY_file ./$appname-$DATE_DAY.jar
done