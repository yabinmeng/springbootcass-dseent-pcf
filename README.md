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

## Set up PCF Environment

1) Register a PCF trial account

2) Install CF CLI
https://docs.pivotal.io/pivotalcf/2-3/cf-cli/install-go-cli.html
    
3) Install PCF DEV
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

5) Log in PCF Web Service Console WebUI (using the registered trial account); Set up a testing Org. (**datastax-test**) and a testing Space (**mytestspace**) under the Org.

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

After this, we can go to the PCF web service console Web UI to view the service details.

## Deploy the Application to PCF

The application is developed in a way that it reads the required credentials from the PCF user provided service (**mycassauth-service**). Once the service is created, we need to push the application to PCF and bind it with the service.

1) Push the application
```
$ cf push mybookrating -p ./build/libs/dseent-pcf-0.0.1-SNAPSHOT.jar 
Pushing app mybookrating to org datastax-test / space mytestspace as <registered_trial_account_email>...
Getting app info...
Updating app with these attributes...
  name:                mybookrating
  path:                <some_root_folder>/build/libs/dseent-pcf-0.0.1-SNAPSHOT.jar 
  command:             JAVA_OPTS="-agentpath:$PWD/.java-buildpack/open_jdk_jre/bin/jvmkill-1.16.0_RELEASE=printHeapHistogram=1 -Djava.io.tmpdir=$TMPDIR -Djava.ext.dirs=$PWD/.java-buildpack/container_security_provider:$PWD/.java-buildpack/open_jdk_jre/lib/ext -Djava.security.properties=$PWD/.java-buildpack/java_security/java.security $JAVA_OPTS" && CALCULATED_MEMORY=$($PWD/.java-buildpack/open_jdk_jre/bin/java-buildpack-memory-calculator-3.13.0_RELEASE -totMemory=$MEMORY_LIMIT -loadedClasses=16498 -poolType=metaspace -stackThreads=250 -vmOptions="$JAVA_OPTS") && echo JVM Memory Configuration: $CALCULATED_MEMORY && JAVA_OPTS="$JAVA_OPTS $CALCULATED_MEMORY" && MALLOC_ARENA_MAX=2 SERVER_PORT=$PORT eval exec $PWD/.java-buildpack/open_jdk_jre/bin/java $JAVA_OPTS -cp $PWD/. org.springframework.boot.loader.JarLauncher
  disk quota:          1G
  health check type:   port
  instances:           1
  memory:              1G
  stack:               cflinuxfs2
  routes:
    mybookrating.cfapps.io

Updating app mybookrating...
Mapping routes...
Comparing local files to remote cache...
Packaging files to upload...
Uploading files...
   .... ....
   ... <<a lot more output>>
   .... ....
```

Once the application is pushed, PCF will try to start it automatically (and connect to DSE). But at this point, the application is not bound with the service yet, it will fail and PCF output will show the application failed/crashed, which is expected.

2) Bind the application with the service

```
$ cf bind-service mybookrating mycassauth-service
Binding service mycassauth-service to app mybookrating in org datastax-test / space mytestspace as <registered_trial_account_email>...
OK
TIP: Use 'cf restage mybookrating' to ensure your env variable changes take effect
```

3) Restage the application

```
$ cf restage mybookrating
Restaging app mybookrating in org datastax-test / space mytestspace as <registered_trial_account_email>...
timeout connecting to log server, no log will be shown

Staging app and tracing logs...
   Downloaded build artifacts cache (132B)
   Downloaded app package (23.2M)
   -----> Java Buildpack v4.16.1 (offline) | https://github.com/cloudfoundry/java-buildpack.git#41b8ff8
   -----> Downloading Jvmkill Agent 1.16.0_RELEASE from https://java-buildpack.cloudfoundry.org/jvmkill/trusty/x86_64/jvmkill-1.16.0_RELEASE.so (found in cache)
   -----> Downloading Open Jdk JRE 1.8.0_192 from https://java-buildpack.cloudfoundry.org/openjdk/trusty/x86_64/openjdk-1.8.0_192.tar.gz (found in cache)
          Expanding Open Jdk JRE to .java-buildpack/open_jdk_jre (1.1s)
          JVM DNS caching disabled in lieu of BOSH DNS caching
   -----> Downloading Open JDK Like Memory Calculator 3.13.0_RELEASE from https://java-buildpack.cloudfoundry.org/memory-calculator/trusty/x86_64/memory-calculator-3.13.0_RELEASE.tar.gz (found in cache)
          Loaded Classes: 15719, Threads: 250
   -----> Downloading Client Certificate Mapper 1.8.0_RELEASE from https://java-buildpack.cloudfoundry.org/client-certificate-mapper/client-certificate-mapper-1.8.0_RELEASE.jar (found in cache)
   -----> Downloading Container Security Provider 1.16.0_RELEASE from https://java-buildpack.cloudfoundry.org/container-security-provider/container-security-provider-1.16.0_RELEASE.jar (found in cache)
   -----> Downloading Spring Auto Reconfiguration 2.5.0_RELEASE from https://java-buildpack.cloudfoundry.org/auto-reconfiguration/auto-reconfiguration-2.5.0_RELEASE.jar (found in cache)
   Exit status 0
   Uploading droplet, build artifacts cache...
   Uploading droplet...
   Uploading build artifacts cache...
   Uploaded build artifacts cache (132B)
   Uploaded droplet (69.9M)
   Uploading complete
   Cell 2c755f28-7f03-4a12-871c-7b1e60306330 stopping instance f07ed4f4-176d-4f1c-9d59-5175674c5692
   Cell 2c755f28-7f03-4a12-871c-7b1e60306330 destroying container for instance f07ed4f4-176d-4f1c-9d59-5175674c5692

Waiting for app to start...

name:              mybookrating
requested state:   started
routes:            mybookrating.cfapps.io
last uploaded:     Mon 26 Nov 21:47:54 PST 2018
stack:             cflinuxfs2
buildpacks:        client-certificate-mapper=1.8.0_RELEASE container-security-provider=1.16.0_RELEASE
                   java-buildpack=v4.16.1-offline-https://github.com/cloudfoundry/java-buildpack.git#41b8ff8 java-main
                   java-opts java-security jvmkill-agent=1.16.0_RELEASE open-jd...

type:            web
instances:       1/1
memory usage:    1024M
start command:   JAVA_OPTS="-agentpath:$PWD/.java-buildpack/open_jdk_jre/bin/jvmkill-1.16.0_RELEASE=printHeapHistogram=1
                 -Djava.io.tmpdir=$TMPDIR
                 -Djava.ext.dirs=$PWD/.java-buildpack/container_security_provider:$PWD/.java-buildpack/open_jdk_jre/lib/ext
                 -Djava.security.properties=$PWD/.java-buildpack/java_security/java.security $JAVA_OPTS" &&
                 CALCULATED_MEMORY=$($PWD/.java-buildpack/open_jdk_jre/bin/java-buildpack-memory-calculator-3.13.0_RELEASE
                 -totMemory=$MEMORY_LIMIT -loadedClasses=16498 -poolType=metaspace -stackThreads=250 -vmOptions="$JAVA_OPTS") &&
                 echo JVM Memory Configuration: $CALCULATED_MEMORY && JAVA_OPTS="$JAVA_OPTS $CALCULATED_MEMORY" &&
                 MALLOC_ARENA_MAX=2 SERVER_PORT=$PORT eval exec $PWD/.java-buildpack/open_jdk_jre/bin/java $JAVA_OPTS -cp $PWD/.
                 org.springframework.boot.loader.JarLauncher
     state     since                  cpu    memory        disk           details
#0   running   2018-11-27T05:48:14Z   0.0%   34.7M of 1G   151.5M of 1G
```

At this point, the application is successfully started and connects to DSE using the provided credentials from PCF CUPS.


## Test the Application from PCF endpoint

When the application is pushed to PCF, it is given a PCF route (endpoint). For my testing application, the route is:
https://mybookrating.cfapps.io/

At this point, we can test the Rest APIs as provided by this application for data writing and reading from DSE. The screenshot below is the result returned in the web browser.

<img src="https://github.com/yabinmeng/springbootcass-dseent-pcf/blob/master/src/main/resources/pcf_springboot.png" width="800" height="300">
