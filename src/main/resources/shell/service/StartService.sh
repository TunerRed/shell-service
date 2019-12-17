function echo_error() {
    echo -e "$1" 1>&2
}

if [ $# -ne 4 ] ; then
  echo_error "args error"
  exit 1
fi

source ./.bash_profile

APP_path="`pwd`/$1"
APP_file=$2
APP_args=$3
Log_path="`pwd`/$4"

mkdir -p $APP_path
mkdir -p $Log_path
cd $APP_path
APP_file=`ls -r | egrep "^$APP_file-[0-9]{4}.jar$" | awk 'NR==1'`

if [ ! -e $APP_path/$APP_file ] ; then
  echo_error "no jar file found:$APP_path/$APP_file"
  exit 1
fi 

APP_log="$Log_path/${APP_file%.jar}.log"
CMD="nohup java -jar $APP_args $APP_path/$APP_file >$APP_log 2>&1 &"
#echo "$CMD"
eval "$CMD"

tail_count=0
tail_max=30
tail_gap=1

sleep 3
if [ ! -e $APP_log ] ; then
  echo_error "no log file found"
  exit 1
fi 
while true ; do
  if [ `tail -n 10 $APP_log | grep "INFO" | grep "Started" | wc -l` -eq 0 ] ; then
    sleep $tail_gap
    let tail_count++
  else
    START_info=`cat $APP_log | grep "INFO" | grep "Started"`
    echo "${START_info#*INFO}"
    #rm -r $APP_log
    exit 0
  fi 
  if [ $tail_count -ge $tail_max ] ; then
    echo_error "$APP_file start failed"
    echo_error "`tail -n 10 $APP_log`"
    exit 1
  fi
done 