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
    #echo _filename'.jar'
}

JAR_CATEGORY=$1

if [ ! -d $JAR_CATEGORY ] || [ `ls $JAR_CATEGORY | grep -E "jar$" | wc -l` -eq 0 ] ; then
  echo_error "no files found"
  exit 1
fi

cd $JAR_CATEGORY
JAR_FILES=(`ls | grep -E "jar$"`)
for JAR_FILE in "${JAR_FILES[@]}" ; do
    rename $JAR_FILE
done

exit 0