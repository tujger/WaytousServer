#!/bin/bash
EMPTY=true
for i in "$@"
do
case $i in
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
    --destination-path=*)
    EMPTY=false
    DESTINATION_PATH="${i#*=}"
    ;;
    *)
            # unknown option
    ;;
esac
done

if [ $EMPTY == true ]; then
    echo aws.sh -n=[name] -u -uu -r $EMPTY
    echo    -n, --name - login name
    echo    -u, --update - update configs
    echo    -uu, --update-server - update WaytousServer.war and deploy it on server
    echo    -r, --restart - start/restart server
    echo    --upload-file=SOURCE_FILE
    echo    --destination-path=DESTINATION_FILE
    echo    -s, --session - ssh session
    exit 1
fi
if [ $USERNAME ]; then
    echo --- Username: $USERNAME
else
    echo --- Define -n=username, i.e. ec2-user
    exit 1
fi
if [ $FOLDER ]; then
    FOLDER=FOLDER
else
    FOLDER="prod"
fi
if [ $UPDATE_SERVER ]; then
    echo --- Updating server to $FOLDER...
    scp -i conf/aws/aws_credentials.pem build/libs/WaytousServer-1.41.war $USERNAME@wayto.us:WaytousServer.war

    ssh -i conf/aws/aws_credentials.pem $USERNAME@wayto.us << RECREATEFOLDER
        pkill -f java
        rm -r $FOLDER
        mkdir $FOLDER
        mv WaytousServer.war $FOLDER
        cd $FOLDER
        mkdir .well-known
        mkdir .well-known/acme-challenge
        unzip -o WaytousServer.war
        rm WaytousServer.war
RECREATEFOLDER
fi
if [ $UPDATE ] || [ $UPDATE_SERVER ]; then
    echo --- Updating options and credentials...
    ssh -i conf/aws/aws_credentials.pem $USERNAME@wayto.us "mkdir conf"
    scp -i conf/aws/aws_credentials.pem conf/aws/options_aws.json $USERNAME@wayto.us:conf/options_aws.json
    scp -i conf/aws/aws_credentials.pem conf/letsencrypt/letsencrypt.jks $USERNAME@wayto.us:conf/aws.jks
    scp -i conf/aws/aws_credentials.pem conf/waytous-gamma-firebase-adminsdk-77ij5-540bdb1a17.json $USERNAME@wayto.us:conf/waytous-gamma-firebase-adminsdk-77ij5-540bdb1a17.json
    scp -i conf/aws/aws_credentials.pem conf/aws/googlee7b16def95e75693.html $USERNAME@wayto.us:prod/googlee7b16def95e75693.html
fi
if [ $UPLOAD_FILE ]; then
    echo --- Uploading file $UPLOAD_FILE...
    if [ $DESTINATION_PATH ]; then
        scp -i conf/aws/aws_credentials.pem $UPLOAD_FILE $USERNAME@wayto.us:$DESTINATION_PATH
    else
        echo --- Define --destination-path=PATH_TO_FILE
        exit 1
    fi
fi
if [ $RESTART ]; then
    echo --- Restarting server...
    ssh -i conf/aws/aws_credentials.pem $USERNAME@wayto.us "pkill -f java"


#scp -i ./conf/aws/aws_credentials.pem ./JkUBvVjs_Z4f15vkDJZMNX8FXf0HccqMErqaGzcFN48 ec2-user@wayto.us:prod/.well-known/acme-challenge/JkUBvVjs_Z4f15vkDJZMNX8FXf0HccqMErqaGzcFN48
#scp -i ./conf/aws/aws_credentials.pem ./pX7ESUiT0ExIjKz60V2tIRNyvTdoJWYzkpDCj7K6_Eo ec2-user@wayto.us:prod/.well-known/acme-challenge/pX7ESUiT0ExIjKz60V2tIRNyvTdoJWYzkpDCj7K6_Eo
#scp -i ./conf/aws/aws_credentials.pem ./uqPn2lT3ltmBG-IP4JmunOtBdJdsH4oC8P0Eqw904Rw ec2-user@wayto.us:prod/.well-known/acme-challenge/uqPn2lT3ltmBG-IP4JmunOtBdJdsH4oC8P0Eqw904Rw
#scp -i ./conf/aws/aws_credentials.pem ./conf/assetlinks.json ec2-user@wayto.us:prod/.well-known/assetlinks.json


    ssh -i conf/aws/aws_credentials.pem $USERNAME@wayto.us << STARTSERVER
        cd $FOLDER/WEB-INF/classes
        /usr/bin/java -cp ..::../lib/Java-WebSocket-1.3.5.jar:../lib/firebase-admin-5.4.0.jar:../lib/javax.servlet-api-3.1.0.jar:../lib/json-20171018.jar:../lib/slf4j-nop-1.7.25.jar:../lib/guava-20.0.jar:../lib/junit-4.12.jar:../lib/google-api-client-1.22.0.jar:../lib/google-api-client-gson-1.22.0.jar:../lib/google-http-client-1.22.0.jar:../lib/api-common-1.1.0.jar:../lib/google-auth-library-oauth2-http-0.8.0.jar:../lib/google-cloud-storage-1.2.1.jar:../lib/slf4j-api-1.7.25.jar:../lib/hamcrest-core-1.3.jar:../lib/google-oauth-client-1.22.0.jar:../lib/google-http-client-jackson2-1.22.0.jar:../lib/google-http-client-gson-1.22.0.jar:../lib/httpclient-4.0.1.jar:../lib/google-auth-library-credentials-0.8.0.jar:../lib/google-cloud-core-1.2.1.jar:../lib/google-cloud-core-http-1.2.1.jar:../lib/google-api-services-storage-v1-rev100-1.22.0.jar:../lib/guava-jdk5-17.0.jar:../lib/joda-time-2.9.2.jar:../lib/gax-1.4.1.jar:../lib/protobuf-java-util-3.3.0.jar:../lib/proto-google-common-protos-0.1.12.jar:../lib/proto-google-iam-v1-0.1.12.jar:../lib/google-http-client-appengine-1.21.0.jar:../lib/google-http-client-jackson-1.21.0.jar:../lib/auto-value-1.2.jar:../lib/threetenbp-1.3.3.jar:../lib/protobuf-java-3.3.0.jar:../lib/jackson-core-asl-1.9.11.jar:../lib/jsr305-3.0.0.jar:../lib/httpcore-4.0.1.jar:../lib/commons-logging-1.1.1.jar:../lib/commons-codec-1.3.jar:../lib/WaytousFramework.jar:../lib/Helpers.jar:../lib/jackson-core-2.1.3.jar:../lib/gson-2.7.jar com.edeqa.waytousserver.WaytousServer ../../../conf/options_aws.json &> ../../../waytous.log
STARTSERVER
fi
if [ $SESSION ]; then
    if [ $USERNAME ]; then
        echo --- Starting session...
        ssh -i conf/aws/aws_credentials.pem $USERNAME@wayto.us
    else
        echo --- Session not started. Define -n=USER_NAME
        exit 1
    fi
fi
#/usr/bin/java -cp .:../lib/appengine-api-1.0-sdk-1.9.54.jar.:../lib/firebase-admin-5.2.0.jar.:../lib/google-api-client-gson-1.22.0.jar.:../lib/google-http-client-gson-1.22.0.jar.:../lib/google-oauth-client-1.22.0.jar.:../lib/guava-20.0.jar.:../lib/jackson-core-2.1.3.jar.:../lib/javax.servlet-api-3.1.0.jar.:../lib/jsr305-1.3.9.jar.:../lib/eventbus-0.3.jar.:../lib/google-api-client-1.22.0.jar.:../lib/google-http-client-1.22.0.jar.:../lib/google-http-client-jackson2-1.22.0.jar.:../lib/gson-2.1.jar.:../lib/guava-jdk5-17.0.jar.:../lib/Java-WebSocket-1.3.4.jar.:../lib/json-20160810.jar com.edeqa.waytousserver.WaytousServer ../../../conf/options_aws.json > ../../../waytous.log

#/usr/bin/java -cp :../lib/appengine-api-1.0-sdk-1.9.54.jar:../lib/firebase-server-sdk-3.0.3.jar:../lib/google-api-client-gson-1.21.0.jar:../lib/google-http-client-gson-1.21.0.jar:../lib/google-oauth-client-1.21.0.jar:../lib/guava-20.0.jar:../lib/jackson-core-2.1.3.jar:../lib/javax.servlet-api-3.1.0.jar:../lib/jsr305-1.3.9.jar:../lib/eventbus-0.3.jar:../lib/google-api-client-1.21.0.jar:../lib/google-http-client-1.21.0.jar:../lib/google-http-client-jackson2-1.21.0.jar:../lib/gson-2.6.2.jar:../lib/guava-jdk5-17.0.jar:../lib/Java-WebSocket-1.3.4.jar:../lib/json-20160212.jar com.edeqa.waytousserver.WaytousServer ../../../conf/options_aws.json > ../../../waytous.log

