#!/bin/bash
for i in "$@"
do
case ${i} in
    -u|--update)
    UPDATE=true

    ;;
    -n=*|--name=*)
    USERNAME="${i#*=}"
    ;;
    -f=*|--folder=*)
    FOLDER="${i#*=}"
    ;;
    -l=*|--lib=*)
    DIR="${i#*=}"
    ;;
    --default)
    DEFAULT=YES
    ;;
    *)
            # unknown option
    ;;
esac
done
if [ ${USERNAME} ]; then
    echo --- Username: ${USERNAME}
else
    echo --- Define -n=username
    exit 1
fi
if [ ${FOLDER} ]; then
    FOLDER=FOLDER
else
    FOLDER="beta"
fi
echo --- Deployment folder: ${FOLDER}
if [ ${UPDATE} ]; then
    echo --- Update mode
else
    echo --- Restart mode. Call with --update for update mode
fi

ssh -i conf/aws/aws_credentials.pem ${USERNAME}@wayto.us "mkdir conf"
scp -i conf/aws/aws_credentials.pem ~/Documents/Devel/Android/Waytous/conf/aws_beta/options_aws_beta.json ${USERNAME}@wayto.us:conf/options_aws_beta.json
scp -i conf/aws/aws_credentials.pem ~/Documents/Devel/Android/Waytous/conf/letsencrypt/letsencrypt.jks ${USERNAME}@wayto.us:conf/aws.jks
scp -i conf/aws/aws_credentials.pem ~/Documents/Devel/Android/Waytous/conf/waytous-beta-firebase-adminsdk-twi5v-a17e79f8afe.json ${USERNAME}@wayto.us:conf/waytous-beta-firebase-adminsdk-twi5v-a17e79f8afe.json

if [ ${UPDATE} ]; then
    scp -i conf/aws/aws_credentials.pem ~/Documents/Devel/Android/Waytous/WaytousServer/build/libs/WaytousServer.war ${USERNAME}@wayto.us:WaytousServer.war
    ssh -i conf/aws/aws_credentials.pem ${USERNAME}@wayto.us "
pkill -f $FOLDER
rm -r $FOLDER
mkdir $FOLDER
mv WaytousServer.war $FOLDER
cd $FOLDER
mkdir .well-known
mkdir .well-known/acme-challenge
unzip -o WaytousServer.war
rm WaytousServer.war
"
else
    echo war skipping in restart mode...
    ssh -i conf/aws/aws_credentials.pem ${USERNAME}@wayto.us "
pkill -f java
"
fi

#scp -i conf/aws/aws_credentials.pem ~/Documents/Devel/Android/Waytous/conf/4AEUFu_OWe84WGfbnGhxK4_WZIMZemNUkRi5EGUzoE8 $USERNAME@wayto.us:prod/.well-known/acme-challenge/4AEUFu_OWe84WGfbnGhxK4_WZIMZemNUkRi5EGUzoE8
#scp -i conf/aws/aws_credentials.pem ~/Documents/Devel/Android/Waytous/conf/p1hZDcHlEen9Krerq_owcGjYtk31zDVn1NpujM2IiQ0 $USERNAME@wayto.us:prod/.well-known/acme-challenge/p1hZDcHlEen9Krerq_owcGjYtk31zDVn1NpujM2IiQ0
#scp -i conf/aws/aws_credentials.pem ~/Documents/Devel/Android/Waytous/conf/QwwRg2fh0rnaMkDKUaoQ7-1dPQJnLKZheRMXACxuiNE $USERNAME@wayto.us:prod/.well-known/acme-challenge/QwwRg2fh0rnaMkDKUaoQ7-1dPQJnLKZheRMXACxuiNE
#scp -i conf/aws/aws_credentials.pem ~/Documents/Devel/Android/Waytous/conf/taI0Ho_zob-nGe-Ha-jRfnAvlM66wK0kkiB3hCrqdAU $USERNAME@wayto.us:prod/.well-known/acme-challenge/taI0Ho_zob-nGe-Ha-jRfnAvlM66wK0kkiB3hCrqdAU

ssh -i conf/aws/aws_credentials.pem ${USERNAME}@wayto.us "
cd $FOLDER/WEB-INF/classes
/usr/bin/java -cp .:../lib/firebase-admin-5.2.0.jar:../lib/google-api-client-gson-1.22.0.jar:../lib/google-http-client-gson-1.22.0.jar:../lib/google-oauth-client-1.22.0.jar:../lib/guava-20.0.jar:../lib/jackson-core-2.1.3.jar:../lib/javax.servlet-api-3.1.0.jar:../lib/jsr305-1.3.9.jar:../lib/google-api-client-1.22.0.jar:../lib/google-http-client-1.22.0.jar:../lib/google-http-client-jackson2-1.22.0.jar:../lib/gson-2.1.jar:../lib/guava-jdk5-17.0.jar:../lib/Java-WebSocket-1.3.4.jar:../lib/json-20160810.jar com.edeqa.waytousserver.WaytousServer ../../../conf/options_aws_beta.json > ../../../waytous-beta.log
"


#/usr/bin/java -cp .:../lib/appengine-api-1.0-sdk-1.9.54.jar.:../lib/firebase-admin-5.2.0.jar.:../lib/google-api-client-gson-1.22.0.jar.:../lib/google-http-client-gson-1.22.0.jar.:../lib/google-oauth-client-1.22.0.jar.:../lib/guava-20.0.jar.:../lib/jackson-core-2.1.3.jar.:../lib/javax.servlet-api-3.1.0.jar.:../lib/jsr305-1.3.9.jar.:../lib/eventbus-0.3.jar.:../lib/google-api-client-1.22.0.jar.:../lib/google-http-client-1.22.0.jar.:../lib/google-http-client-jackson2-1.22.0.jar.:../lib/gson-2.1.jar.:../lib/guava-jdk5-17.0.jar.:../lib/Java-WebSocket-1.3.4.jar.:../lib/json-20160810.jar com.edeqa.waytousserver.WaytousServer ../../../conf/options_aws.json > ../../../waytous.log

#/usr/bin/java -cp :../lib/appengine-api-1.0-sdk-1.9.54.jar:../lib/firebase-server-sdk-3.0.3.jar:../lib/google-api-client-gson-1.21.0.jar:../lib/google-http-client-gson-1.21.0.jar:../lib/google-oauth-client-1.21.0.jar:../lib/guava-20.0.jar:../lib/jackson-core-2.1.3.jar:../lib/javax.servlet-api-3.1.0.jar:../lib/jsr305-1.3.9.jar:../lib/eventbus-0.3.jar:../lib/google-api-client-1.21.0.jar:../lib/google-http-client-1.21.0.jar:../lib/google-http-client-jackson2-1.21.0.jar:../lib/gson-2.6.2.jar:../lib/guava-jdk5-17.0.jar:../lib/Java-WebSocket-1.3.4.jar:../lib/json-20160212.jar com.edeqa.waytousserver.WaytousServer ../../../conf/options_aws.json > ../../../waytous.log

