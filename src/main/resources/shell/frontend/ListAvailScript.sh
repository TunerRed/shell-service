cd $1
cat package.json | while read line
do
        if [[ $line == *scripts* ]];
        then
                while read script
                do
                        if [ `echo $script|grep "}"|wc -l` -gt 0 ];
                        then
                                break
                        fi
                        if [[ $script == \"build* ]];
                        then
                                script=${script#\"}
                                script=${script%%\"*}
                                echo $script
                        fi
                done
                break
        fi
done