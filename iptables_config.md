### iptables config for Waytous server

_**Note: still not working**_

    #!/bin/bash
    
    # setup basic chains and allow all or we might get locked out while the rules are running...
    sudo iptables -P INPUT ACCEPT
    sudo iptables -P FORWARD ACCEPT
    sudo iptables -P OUTPUT ACCEPT
    
    # clear rules
    sudo iptables -F
    
    # redirect 80 to 8080
    sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080
    
    # redirect 443 to 8443
    sudo iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDIRECT --to-port 8443
    
    # allow HTTP inbound and replies
    sudo iptables -A INPUT -p tcp --dport 8080 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8080 -m state --state ESTABLISHED -j ACCEPT
    
    # allow HTTPS inbound and replies
    sudo iptables -A INPUT -p tcp --dport 8443 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8443 -m state --state ESTABLISHED -j ACCEPT
    
    # allow all ports rules - for production version
    sudo iptables -A INPUT -p tcp --dport 8100 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8100 -m state --state ESTABLISHED -j ACCEPT
    sudo iptables -A INPUT -p tcp --dport 8101 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8101 -m state --state ESTABLISHED -j ACCEPT
    sudo iptables -A INPUT -p tcp --dport 8200 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8200 -m state --state ESTABLISHED -j ACCEPT
    sudo iptables -A INPUT -p tcp --dport 8201 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8201 -m state --state ESTABLISHED -j ACCEPT
    sudo iptables -A INPUT -p tcp --dport 8989 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8989 -m state --state ESTABLISHED -j ACCEPT
    
    # allow all ports rules - for debug version
    sudo iptables -A INPUT -p tcp --dport 8090 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8090 -m state --state ESTABLISHED -j ACCEPT
    sudo iptables -A INPUT -p tcp --dport 8453 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8453 -m state --state ESTABLISHED -j ACCEPT
    sudo iptables -A INPUT -p tcp --dport 8110 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8110 -m state --state ESTABLISHED -j ACCEPT
    sudo iptables -A INPUT -p tcp --dport 8111 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8111 -m state --state ESTABLISHED -j ACCEPT
    sudo iptables -A INPUT -p tcp --dport 8210 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8210 -m state --state ESTABLISHED -j ACCEPT
    sudo iptables -A INPUT -p tcp --dport 8211 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8211 -m state --state ESTABLISHED -j ACCEPT
    sudo iptables -A INPUT -p tcp --dport 8999 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 8999 -m state --state ESTABLISHED -j ACCEPT
    
    # limit ssh connects to 10 every 10 seconds
    # change the port 22 if ssh is listening on a different port (which it should be)
    # in the instance's AWS Security Group, you should limit SSH access to just your IP
    # however, this will severely impede a password crack attempt should the SG rule be misconfigured
    #sudo iptables -A INPUT -p tcp --dport 22 -m state --state NEW -m recent --set
    #sudo iptables -A INPUT -p tcp --dport 22 -m state --state NEW -m recent --update --seconds 10 --hitcount 10 -j DROP
    
    # allow SSH inbound and replies
    # change the port 22 if ssh is listening on a different port (which it should be)
    sudo iptables -A INPUT -p tcp --dport 22 -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A OUTPUT -p tcp --sport 22 -m state --state ESTABLISHED -j ACCEPT
    
    # root can initiate HTTP outbound (for yum)
    # anyone can receive replies (ok since connections can't be initiated)
    sudo iptables -A OUTPUT -p tcp --dport 80 -m owner --uid-owner root -m state --state NEW,ESTABLISHED -j ACCEPT
    sudo iptables -A INPUT -p tcp --sport 80 -m state --state ESTABLISHED -j ACCEPT
    
    # root can do DNS searches (if your Subnet is 10.0.0.0/24 AWS DNS seems to be on 10.0.0.2)
    # if your subnet is different, change 10.0.0.2 to your value (eg a 172.31.1.0/24 Subnet would be 172.31.1.2)
    # see http://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/vpc-dns.html
    # DNS = start subnet range "plus two"
    #sudo iptables -A OUTPUT -p udp --dport 53 -m owner --uid-owner root -d 10.0.0.0/24 -j ACCEPT
    #sudo iptables -A INPUT -p udp --sport 53 -s 10.0.0.0/24 -j ACCEPT
    #sudo iptables -A OUTPUT -p udp --dport 53 -j ACCEPT
    #sudo iptables -A INPUT -p udp --sport 53 -m state --state ESTABLISHED  -j ACCEPT
    
    # apache user can talk to rds server on 10.0.0.200:3306
    #sudo iptables -A OUTPUT -p tcp --dport 3306 -m owner --uid-owner apache -d 10.0.0.200 -j ACCEPT
    #sudo iptables -A INPUT -p tcp --sport 3306 -s 10.0.0.200 -j ACCEPT
    
    # now drop everything else
    #sudo iptables -P INPUT DROP
    #sudo iptables -P FORWARD DROP
    #sudo iptables -P OUTPUT DROP
    
    # save config
    sudo /sbin/service iptables save
    
    echo "Filter table:"
    sudo iptables -t filter -L
    
    echo "Nat table:"
    sudo iptables -t nat -L
    
    echo "Mangle table:"
    sudo iptables -t mangle -S
    
    echo "Raw table:"
    sudo iptables -t raw -S
    
    echo "Security table:"
    sudo iptables -t security
    
    
    
    
