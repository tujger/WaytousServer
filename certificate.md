## Local machine developer certificate

####0. Variables

Let `DOMAINNAME` is the target, `PASSWORD` is the password for keys and storages.

####1. Root SSL certificate

    openssl genrsa -des3 -out rootCA.key 2048
    openssl req -x509 -new -nodes -key rootCA.key -sha256 -days 1024 -out rootCA.pem

####2. Trust the root SSL certificate

Keychain Access, File -> Import Items. Select `rootCA.pem`. Double click the imported certificate and change the "When using this certificate:" dropdown to "Always Trust" in the "Trust section".
    
####3. Domain SSL certificate

Configuration file: `server.csr.cnf`
    
    [req]
    default_bits = 2048
    prompt = no
    default_md = sha256
    distinguished_name = dn
    
    [dn]
    C=US
    ST=VA
    L=Reston
    O=Edeqa
    OU=IT
    emailAddress=info@edeqa.com
    CN = DOMAINNAME
        
Configuration file: `v3.ext`
    
    authorityKeyIdentifier=keyid,issuer
    basicConstraints=CA:TRUE
    keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
    subjectAltName = @alt_names
    
    [alt_names]
    DNS.1 = DOMAINNAME
        
Then create a certificate key for localhost using the configuration settings stored in server.csr.cnf. This key is stored in server.key.
    
    openssl req -new -sha256 -nodes -out server.csr -newkey rsa:2048 -keyout server.key -config <( cat server.csr.cnf )
        
A certificate signing request is issued via the root SSL certificate we created earlier to create a domain certificate for localhost. The output is a certificate file called server.crt.
    
    openssl x509 -req -in server.csr -CA rootCA.pem -CAkey rootCA.key -CAcreateserial -out server.crt -days 500 -sha256 -extfile v3.ext
        
####4. Export certificate into `debug-keystore.jks`

    openssl pkcs12 -export -in server.crt -inkey server.key -name DOMAINNAME -out 1.p12
    keytool -importkeystore -deststorepass PASSWORD -destkeystore debug-keystore.jks -srckeystore 1.p12 -srcstoretype PKCS12

####5. Trust certificate onto Android device

Send `server.pem` to Android-device and tap on it. Enter name, tap OK.

# Investigating certificates

Show info:

    openssl x509 -in server.crt -text -noout 

Show certificate on host:

    openssl s_client -connect DOMAINNAME:port -tls1


# Links

https://medium.freecodecamp.org/how-to-get-https-working-on-your-local-development-environment-in-5-minutes-7af615770eec

https://stackoverflow.com/questions/906402/how-to-import-an-existing-x509-certificate-and-private-key-in-java-keystore-to-u

https://www.ibm.com/support/knowledgecenter/en/SSHS8R_6.3.0/com.ibm.worklight.installconfig.doc/admin/t_installing_root_CA_android.html