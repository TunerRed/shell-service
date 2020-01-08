
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

if [ $# -lt 2 ]; then
  echo_error "shell args error"
  exit 1
fi
if [ $# -eq 3 ]; then
  WEBAPPS_PATH=`pwd`/$3
fi
# deploy category
DEPLOY_CATEGORY=$1
BACKUP_CATEGORY=$2
HOME_PATH=`pwd`
DATE_TIME=$(date +"%Y%m%d_%H%M")
mkdir -p $DEPLOY_CATEGORY
cd $DEPLOY_CATEGORY
DEPLOY_LIST=(`ls *.tar.gz`)
for DEPLOY_FILE in "${DEPLOY_LIST[@]}"
do
  FILE_NAME=${DEPLOY_FILE%%.*}
  mkdir -p "$FILE_NAME"
  rm -r "$FILE_NAME"
  mkdir "$FILE_NAME"
  tar -xzf $DEPLOY_FILE $FILE_NAME
  if [ -d "$WEBAPPS_PATH/$FILE_NAME" ];
  then
    mkdir -p $HOME_PATH/$BACKUP_CATEGORY/$FILE_NAME
    mv $WEBAPPS_PATH/$FILE_NAME $HOME_PATH/$BACKUP_CATEGORY/$FILE_NAME/$DATE_TIME
    if [ `ls $HOME_PATH/$BACKUP_CATEGORY/$FILE_NAME/$DATE_TIME/index*|wc -l` -eq 0 ] ;
    then
      echo_error "$FILE_NAME backup failed\n"
    fi
    rm -rf $WEBAPPS_PATH/$FILE_NAME
  fi
  mv $FILE_NAME $WEBAPPS_PATH/$FILE_NAME
  if [ `ls $WEBAPPS_PATH/$FILE_NAME/index*|wc -l` -eq 0 ] ;
  then
    echo_error "$FILE_NAME deploy failed\n"
    exit 1
  fi 
done
