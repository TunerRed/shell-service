BACKUP_root_path=$1
BACKUP_app=$2
BACKUP_dir=$BACKUP_root_path/$BACKUP_app
mkdir -p $BACKUP_dir
cd $BACKUP_dir
DIR_list=(`ls -F -r| grep "/$"`)
for AVAIL_backup in "${DIR_list[@]}"
do
    echo "${AVAIL_backup%/}"
done
exit 0
