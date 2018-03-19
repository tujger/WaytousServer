## Local machine developer certificate

###1. Variables

Let `DOMAINNAME` is the target (maybe a network computer name), `PASSWORD` is a password for the jks-storage.

###2. Do it automatically 

_Note: go to point 3 if it's not working._

Create batch file `gencert.sh`:

    #!/bin/bash
    EMPTY=true
    SKIP_CONFIG=false
    for i in "$@"
    do
    case $i in
        -n=*|--domain-name=*)
        EMPTY=false
        DOMAIN="${i#*=}"
        ;;
        -p=*|--password=*)
        EMPTY=false
        PASS="${i#*=}"
        ;;
        -k|--skip-generating)
        EMPTY=false
        SKIP_CONFIG=true
        ;;
        *)
        # unknown option
        ;;
    esac
    done
    if [ $EMPTY == true ]; then
        echo gencert.sh -n=[name] -p=[password]
        echo "    " -n, --domain-name
        echo "    " -p, --password
        echo "    " -k, --skip-generating - do not generate configs, use server.cnf and server.ext
        echo $DOMAIN
        exit 1
    fi
    if [ -z $DOMAIN ]; then
        echo Domain name is not defined
        exit 1
    fi
    if [ -z $PASS ]; then
        echo Password is not defined
        exit 1
    fi
    if [ $SKIP_CONFIG == true ]
    then
        echo --- Uses server.cnf and server.ext provided
    else
        echo --- Generate configs
    cat > server.cnf <<CNFEND
    [req]
    default_bits=2048
    prompt=no
    default_md=sha256
    distinguished_name=dn
    
    [dn]
    C=US
    ST=VA
    L=Reston
    O=Edeqa
    OU=IT
    emailAddress=info@edeqa.com
    CN=$DOMAIN
    CNFEND
    
    cat > server.ext <<EXTEND
    authorityKeyIdentifier=keyid,issuer
    basicConstraints=CA:TRUE
    keyUsage=digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
    subjectAltName=@alt_names
    
    [alt_names]
    DNS.1=$DOMAIN
    EXTEND
    
    fi
    
    if [ -f "./server.cnf" ]
    then
    	echo --- server.cnf found
    else
    	echo server.cnf file is not found
    	exit 1
    fi
    if [ -f "./server.ext" ]
    then
    	echo --- server.ext found
    else
    	echo server.ext file is not found
    	exit 1
    fi
    
    echo --- Creating root SSL certificate
    openssl genrsa -des3 -passout pass:$PASS -out root_temp.key 2048
    openssl req -x509 -new -nodes -key root_temp.key -sha256 -days 1024 -out root_temp.pem -passin pass:$PASS -config server.cnf
    
    echo --- Creating domain SSL certificate
    openssl req -new -sha256 -nodes -out server.csr -newkey rsa:2048 -keyout server.key -config server.cnf
    openssl x509 -req -in server.csr -CA root_temp.pem -CAkey root_temp.key -CAcreateserial -out server.crt -days 500 -sha256 -extfile server.ext -passin pass:$PASS
    
    rm debug-keystore.jks
    
    echo --- Exporting certificates into debug-keystore.jks
    openssl pkcs12 -export -in server.crt -inkey server.key -name $DOMAIN -out server.p12 -passout pass:$PASS
    keytool -importkeystore -deststorepass $PASS -destkeystore debug-keystore.jks -srckeystore server.p12 -srcstoretype PKCS12 -srcstorepass $PASS
    
    echo --- Cleaning
    rm root_temp.key
    rm root_temp.pem
    rm root_temp.srl
    rm server.key
    rm server.csr
    rm server.p12
    
    if [ $SKIP_CONFIG == false ]
    then
    rm server.cnf
    rm server.ext
    fi
    
    echo --- Done. Use debug-keystore.jks and server.crt

Execute `./gencert.sh -n=DOMAINNAME -p=PASSWORDNAME`
        
###3. Do it manually

_Note: skip if point 2 works._

####a. Create configuration files

Configuration file: `server.cnf`
    
    [req]
    default_bits=2048
    prompt=no
    default_md=sha256
    distinguished_name=dn
    
    [dn]
    C=US
    ST=VA
    L=Reston
    O=Edeqa
    OU=IT
    emailAddress=info@edeqa.com
    CN=DOMAINNAME
        
Configuration file: `server.ext`
    
    authorityKeyIdentifier=keyid,issuer
    basicConstraints=CA:TRUE
    keyUsage=digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
    subjectAltName=@alt_names
    
    [alt_names]
    DNS.1=DOMAINNAME


####b. Create root SSL certificate

    openssl genrsa -des3 -passout pass:PASSWORD -out root_temp.key 2048
    openssl req -x509 -new -nodes -key root_temp.key -sha256 -days 1024 -out root_temp.pem -psssin pass:PASSWORD -config server.cnf

####c. Create domain SSL certificate

Then create a certificate key for localhost using the configuration settings stored in server.csr.cnf. This key is stored in server.key.
    
    openssl req -new -sha256 -nodes -out server.csr -newkey rsa:2048 -keyout server.key -config server.cnf
        
A certificate signing request is issued via the root SSL certificate we created earlier to create a domain certificate for localhost. The output is a certificate file called server.crt.
    
    openssl x509 -req -in server.csr -CA root_temp.pem -CAkey root_temp.key -CAcreateserial -out server.crt -days 500 -sha256 -extfile server.ext -passin pass:PASSWORD
        
####d. Export certificate into `debug-keystore.jks`

    openssl pkcs12 -export -in server.crt -inkey server.key -name DOMAINNAME -out server.p12 -passout pass:PASSWORD
    keytool -importkeystore -deststorepass PASSWORD -destkeystore debug-keystore.jks -srckeystore server.p12 -srcstoretype PKCS12 -srcstorepass PASSWORD

####e. Clean unnecessary files

    rm root_temp.key
    rm root_temp.pem
    rm root_temp.srl
    rm server.key
    rm server.csr
    rm server.p12

###4. Trust the root SSL certificate
 
####a. Mac

Keychain Access, File -> Import Items. Select `server.crt`. Double click the imported certificate and change the "When using this certificate:" dropdown to "Always Trust" in the "Trust section".
    
####b. Android

Send `server.crt` to Android-device and tap on it. Enter name, tap OK.

### Investigating certificates

Show info:

    openssl x509 -in server.crt -text -noout 

Show certificate on host:

    openssl s_client -connect DOMAINNAME:port -tls1


### Links

https://medium.freecodecamp.org/how-to-get-https-working-on-your-local-development-environment-in-5-minutes-7af615770eec

https://stackoverflow.com/questions/906402/how-to-import-an-existing-x509-certificate-and-private-key-in-java-keystore-to-u

https://www.ibm.com/support/knowledgecenter/en/SSHS8R_6.3.0/com.ibm.worklight.installconfig.doc/admin/t_installing_root_CA_android.html