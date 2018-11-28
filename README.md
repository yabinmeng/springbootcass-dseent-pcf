## Overview

This repository demonstrates a few things:
1. How to develop a java application using Spring Boot framework and DSE enterprise java driver together for data interaction with a DSE cluster.
2. How to deploy the developed java application on PCF (Pivotal Cloud Foundry).
3. How to store sensitive information (e.g. Cassandra credentials, SSL keystore password, etc.) in PCF CUPS (create user provided sevice) and let the java application fetch the information dynamically from PCF CUPS.

## Testing Envrionment

* DSE 5.1.11, with the following features enabled (by OpsCenter LCM)
  * User authentication (internal)
  * Server-to-server SSL/TLS encryption
  * Client-to-server SSL/TLS encryption

* Spring boot 2.1.0

* PCF environment 
  * Pivotal Web Service (free trial)
  * PCF DEV version
```
$ cf dev version
CLI: 0.0.12
cf: v5.3.0
cf-mysql: 36.15.0
```

## Set up PCF DEV environment

1) Register for a PCF trail account

2) Install CF CLI
https://docs.pivotal.io/pivotalcf/2-3/cf-cli/install-go-cli.html
    
3) Download and Install PCF DEV
https://docs.pivotal.io/pcf-dev/index.html

4) Start PCF DEV
```
$ cf dev start

Downloading Network Helper...
Installing cfdevd network helper...00.0%
Installing networking components (requires root privileges)
Password:
Setting up IP aliases for the BOSH Director & CF Router (requires administrator privileges)
Downloading Resources...
WARNING: CF Dev requires 8192 MB of RAM to run. This machine may not have enough free RAM.
Creating the VM...
Starting VPNKit...
Starting the VM...
Waiting for Garden...
Deploying the BOSH Director...
Deploying CF...
  Done (29m25s)
Deploying Mysql...
  Done (4m51s)

 	  ██████╗███████╗██████╗ ███████╗██╗   ██╗
 	 ██╔════╝██╔════╝██╔══██╗██╔════╝██║   ██║
 	 ██║     █████╗  ██║  ██║█████╗  ██║   ██║
 	 ██║     ██╔══╝  ██║  ██║██╔══╝  ╚██╗ ██╔╝
 	 ╚██████╗██║     ██████╔╝███████╗ ╚████╔╝
 	  ╚═════╝╚═╝     ╚═════╝ ╚══════╝  ╚═══╝
 	             is now running!

 	To begin using CF Dev, please run:
 	    cf login -a https://api.dev.cfdev.sh --skip-ssl-validation

 	Admin user => Email: admin / Password: admin
 	Regular user => Email: user / Password: pass
```

5) Log in PCF Web Service (using the registered trial account) Console WebUI; Set up a testing Org (**datastax-test**) and a testing Space (**mytestspace**) under the Organization

6) Log in PCF from CF CLI, using the registered trial account. Once logged in, the targeted Org. and Space will be default to the ones created in step 5) 
```
$ cf login -a api.run.pivotal.io
API endpoint: api.run.pivotal.io

Email> <registered_free_trial_account_email>

Password>
Authenticating...
OK

Targeted org datastax-test

Targeted space mytestspace

API endpoint:   https://api.run.pivotal.io (API version: 2.125.0)
User:           <registered_free_trial_account_email>
Org:            datastax-test
Space:          mytestspace
```

7) (Optional) If needed, you can change the targeted Org. or Space from CF CLI
```
cf target [-o ORG] [-s SPACE]
```

## PCF CUPS

In this example, since DSE requires user authentication and clien-to-server SSL/TLS encryption, the developed java application needs to provide the following credentials in order to be able to connect to DSE successfully:
* Cassandra username
* Cassandra password
* Client truststore password

Instead of embedding these credentials in the application, it is recommended to store these credentials in PCF CUPS and let the application to fetch them dynamically. The benefits of doing so are: 1) it is more secure because the credentials are stored only in PCF; 2) it is more flexible because changing the required DSE credentials doesn't need the applcation to be redeployed.

In this example, a PCF user provided service (**mycassauth-service**) is created for this purpose.
```
$ cf cups mycassauth-service -p "cass_username,cass_password,truststore_pass"

cass_username> <cassandra_username>

cass_password> <cassandra_user_password>

truststore_pass> <client_truststore_password>
Creating user provided service mycassauth-service in org datastax-test / space mytestspace as <registered_trial_account_email>...
OK
```
