function echo_error() {
    echo -e "$1" 1>&2
}

if [ $# -ne 3 ] ; then
  echo_error "args error"
  exit 1
fi

source ./.bash_profile

APP_path=$1
APP_file=$2
APP_args=$3

if [ ! -e $APP_path/$APP_file ] ; then
  echo_error "no jar file found:$APP_file"
  exit 1
fi 

nohup java -jar $APP_args $APP_path/$APP_file >$APP_file.log 2>&1 &

tail_count=0
tail_max=30
tail_gap=1

sleep $tail_gap
if [ ! -e $APP_file.log ] ; then
  echo_error "no log file found"
  exit 1
fi 
while true ; do
  if [ `tail -n $APP_file.log | grep "INFO" | grep "Started" | wc -l` -eq 0 ] ; then
    sleep $tail_gap
    let $tail_count++
  else
    cat $APP_file.log | grep "INFO" | grep "Started"
    rm -r $APP_file.log
    exit 0
  fi 
  if [ $tail_count -ge $tail_max ] ; then
    echo_error "$APP_file start failed"
    exit 1
  fi
done 