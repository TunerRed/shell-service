BACKUP_root_path=$1
BACKUP_app=$2
BACKUP_date=$3
RUNNING_app=$4

function echo_error() {
    echo -e "$1" 1>&2
}
function tomcat_path() {
  TOMCAT_PROCESS=`ps -ef|grep -v grep|grep java|grep tomcat|awk 'NR==1'`
  CATALINA_BASE=${TOMCAT_PROCESS##*Dcatalina.base=}
  CATALINA_BASE=${CATALINA_BASE%%" "*}
  WEBAPPS_PATH=$CATALINA_BASE/webapps
  if [ ! -d $WEBAPPS_PATH ];
  then
	echo_error "NO TOMCAT PROCESS FOUND"
	exit 1
  fi
}

source ./.bash_profile

tomcat_path

BACKUP_PATH=$BACKUP_root_path/$BACKUP_app/$BACKUP_date

if [ ! -d $BACKUP_PATH ]||[ `ls $BACKUP_PATH|grep -E "^index.*"| wc -l` -lt 1 ] ;
then
  echo_error "Cannot find backup dir or index.* $BACKUP_PATH"
  exit 1
fi

if [ -d $WEBAPPS_PATH/$RUNNING_app ] ;
then
  mkdir -p $BACKUP_root_path/$RUNNING_app
  mv $WEBAPPS_PATH/$RUNNING_app $BACKUP_root_path/$RUNNING_app/$(date +"%Y%m%d_%H%M")
  rm -rf $WEBAPPS_PATH/$RUNNING_app
fi

mkdir $WEBAPPS_PATH/$RUNNING_app
cp -r $BACKUP_PATH/* $WEBAPPS_PATH/$RUNNING_app/

exit 0
