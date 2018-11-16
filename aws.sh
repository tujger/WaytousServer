#!/bin/bash
EMPTY=true
for i in "$@"
do
case ${i} in
    -r|--restart)
    EMPTY=false
    RESTART=true
    ;;
    -u|--update)
    EMPTY=false
    UPDATE=true
    ;;
    -uu|--update-server)
    EMPTY=false
    UPDATE_SERVER=true
    ;;
    -v=*|--verification-file=*)
    EMPTY=false
    VERIFICATION="${i#*=}"
    ;;
    -n=*|--name=*)
    EMPTY=false
    USERNAME="${i#*=}"
    ;;
    -s|--session)
    EMPTY=false
    SESSION=true
    ;;
    -l=*|--lib=*)
    EMPTY=false
    DIR="${i#*=}"
    ;;
    -f=*|--folder=*)
    EMPTY=false
    FOLDER="${i#*=}"
    ;;
    --upload-file=*)
    EMPTY=false
    UPLOAD_FILE="${i#*=}"
    ;;
    --get-log)
    EMPTY=false
    GETLOG=true
    ;;
    --destination-path=*)
    EMPTY=false
    DESTINATION_PATH="${i#*=}"
    ;;
    *)
            # unknown option
    ;;
esac
done

if [ ${EMPTY} == true ]; then
    echo aws.sh -n=[name] -u -uu -r ${EMPTY}
    echo "    " -n, --name - login name, default is "ec2-user"
    echo "    " -u, --update - update configs
    echo "    " -uu, --update-server - update WaytousServer.war and deploy it on server
    echo "    " -r, --restart - start/restart server
    echo "    " -s, --session - ssh session
    echo "    " --upload-file=SOURCE_FILE
    echo "    " -v=SOURCE_FILE, --verification-file=SOURCE_FILE - upload verification file into "/.well-known/acme-challenge/" folder
    echo "    " --destination-path=DESTINATION_FILE
    echo "    " --get-log - get waytous.log
    exit 1
fi
if [ ${USERNAME} ]; then
    echo --- Username: ${USERNAME}
else
    echo --- Username is not defined, using default "ec2-user". Redefine it: -n=username
    USERNAME="ec2-user"
fi
if [ ${FOLDER} ]; then
    FOLDER=FOLDER
else
    FOLDER="prod"
fi
if [ ${UPDATE_SERVER} ]; then
    echo --- Updating server to ${FOLDER}...
    scp -i conf/aws/aws_credentials.pem build/libs/WaytousServer-2.64.war ${USERNAME}@wayto.us:WaytousServer.war

    ssh -i conf/aws/aws_credentials.pem ${USERNAME}@wayto.us << RECREATEFOLDER
        pkill -f java
        mv ${FOLDER}/webapp/data .
        mv ${FOLDER}/webapp/content .
        rm -r ${FOLDER}
        mkdir ${FOLDER}
        mv WaytousServer.war ${FOLDER}
        cd ${FOLDER}
        mkdir .well-known
        mkdir .well-known/acme-challenge
        unzip -o WaytousServer.war
        rm WaytousServer.war
#        mv ./data ${FOLDER}/webapp
#        mv ./content ${FOLDER}/webapp
RECREATEFOLDER
fi
if [ ${UPDATE} ] || [ ${UPDATE_SERVER} ]; then
    echo --- Updating options and credentials...
    ssh -i conf/aws/aws_credentials.pem ${USERNAME}@wayto.us "mkdir conf"
    scp -i conf/aws/aws_credentials.pem conf/aws/options_aws.json ${USERNAME}@wayto.us:conf/options_aws.json
    scp -i conf/aws/aws_credentials.pem conf/letsencrypt/letsencrypt.jks ${USERNAME}@wayto.us:conf/aws.jks
    scp -i conf/aws/aws_credentials.pem conf/waytous-gamma-firebase-adminsdk-77ij5-540bdb1a17.json ${USERNAME}@wayto.us:conf/waytous-gamma-firebase-adminsdk-77ij5-540bdb1a17.json
    scp -i conf/aws/aws_credentials.pem conf/aws/googlee7b16def95e75693.html ${USERNAME}@wayto.us:prod/googlee7b16def95e75693.html
fi
if [ ${UPLOAD_FILE} ]; then
    echo --- Uploading file ${UPLOAD_FILE}...
    if [ ${DESTINATION_PATH} ]; then
        scp -i conf/aws/aws_credentials.pem ${UPLOAD_FILE} ${USERNAME}@wayto.us:${DESTINATION_PATH}
    else
        echo --- Define --destination-path=PATH_TO_FILE
        exit 1
    fi
fi
if [ ${GETLOG} ]; then
    echo --- Getting waytous.log => aws-waytous.log...
    scp -i conf/aws/aws_credentials.pem ${USERNAME}@wayto.us:waytous.log aws-waytous.log
fi
if [ ${VERIFICATION} ]; then
#scp -i ./conf/aws/aws_credentials.pem ./2tvHGXt2rjLbSjJ7Jfi_oA9sHUS8imFmZ_vhXnqpFPQ ec2-user@wayto.us:prod/.well-known/acme-challenge/2tvHGXt2rjLbSjJ7Jfi_oA9sHUS8imFmZ_vhXnqpFPQ
    echo --- Uploading ${VERIFICATION} => .well-known/acme-challenge/
    scp -i conf/aws/aws_credentials.pem ./${VERIFICATION} ${USERNAME}@wayto.us:prod/.well-known/acme-challenge/${VERIFICATION}
fi
if [ ${RESTART} ]; then
    echo --- Restarting server...
    ssh -i conf/aws/aws_credentials.pem ${USERNAME}@wayto.us "pkill -f java"
    ssh -i conf/aws/aws_credentials.pem ${USERNAME}@wayto.us << STARTSERVER
        cd ${FOLDER}/WEB-INF/classes
        /usr/bin/java -classpath .:../lib/guava-20.0.jar:../lib/* com.edeqa.waytousserver.WaytousServer ../../../conf/options_aws.json &> ../../../waytous.log
STARTSERVER
fi
if [ ${SESSION} ]; then
    if [ ${USERNAME} ]; then
        echo --- Starting session...
        ssh -i conf/aws/aws_credentials.pem ${USERNAME}@wayto.us
    else
        echo --- Session not started. Define -n=USER_NAME
        exit 1
    fi
fi
#/usr/bin/java -cp :../lib/appengine-api-1.0-sdk-1.9.54.jar:../lib/firebase-server-sdk-3.0.3.jar:../lib/google-api-client-gson-1.21.0.jar:../lib/google-http-client-gson-1.21.0.jar:../lib/google-oauth-client-1.21.0.jar:../lib/guava-20.0.jar:../lib/jackson-core-2.1.3.jar:../lib/javax.servlet-api-3.1.0.jar:../lib/jsr305-1.3.9.jar:../lib/eventbus-0.3.jar:../lib/google-api-client-1.21.0.jar:../lib/google-http-client-1.21.0.jar:../lib/google-http-client-jackson2-1.21.0.jar:../lib/gson-2.6.2.jar:../lib/guava-jdk5-17.0.jar:../lib/Java-WebSocket-1.3.4.jar:../lib/json-20160212.jar com.edeqa.waytousserver.WaytousServer ../../../conf/options_aws.json > ../../../waytous.log

