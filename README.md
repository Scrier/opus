                 _  (`-')            (`-').-> 
          .->    \-.(OO )     .->    ( OO)_   
     (`-')----.  _.'    \,--.(,--.  (_)--\_)  
     ( OO).-.  '(_...--''|  | |(`-')/    _ /  
     ( _) | |  ||  |_.' ||  | |(OO )\_..`--.  
      \|  |)|  ||  .___.'|  | | |  \.-._)   \ 
       '  '-'  '|  |     \  '-'(_ .'\       / 
        `-----' `--'      `-----'    `-----'  

# General

    Hazelcast distributed execution.

# Contents
 * [Installation](#Installation)
 * [Dependencies](#Dependencies)
  * [Application](##Application)
  * [Testing](##Testing)
 * [Configuration](#Configuration)
  * [Hazelcast](##Hazelcast)
  * [Log4j2](##Log4j2)
  * [Opus](##Opus)
  * [Run configuration](##Run-configuration)

# Installation

```
sudo rpm -i "opus rpm"
```

# Dependencies
The following libraries external libraries is used by opus.

## Application

 * Hazelcast 3.3.1 - http://hazelcast.org/
 * Log4j2 2.1 - http://logging.apache.org/log4j/2.x/

## Testing

 * JUnit 4.11 - http://junit.org/
 * Mockito 1.9.5 - https://code.google.com/p/mockito/

# Configuration
Configuration is divided in 4 parts. Hazelcast, Log4j2, Opus and the run configurations.

## Hazelcast 

Installed under /etc/opus with filename hazelcastDukeConfig.xml or hazelcastNukeConfig.xml

Most information should be available on their homepage for setting this up. Here are some links that is well worth the reading.

 * http://docs.hazelcast.org/docs/3.3/manual/html-single/hazelcast-documentation.html
 * https://github.com/hazelcast/hazelcast/blob/master/hazelcast/src/main/resources/hazelcast-default.xml - example config explained

### Group

If you want to run local instance and avoid collissions with existing clusters you need to rename the group name for your local cluster.


## Log4j2

Installed under /etc/opus with filename log4j2duke.xml and log4j2nuke.xml

## Opus

Installed under /etc/opus/opus.conf, is commented well in the file and should be self explanatory, but here is a quick extra explanation.

### OPUS_SERVICE_USER

This is set default to opus, but can be changed to other users depending on specific installed files that needs to be run in the application. It is important that that user is a member of opus (yes group and name is the same). This is to have write rights for logging and similar when running the application.

### OPUS_SERVICE_USER_HOME

This sets the user home, defaults to /home/$OPUS_SERVICE_USER.

### OPUS_EDITOR

Defaults to vim atm. Is used for accessing the config files through the services as extra parameters to debug part.

```bash
sudo service <nuke/duke> debug -o   # starts opus config
sudo service <nuke/duke> debug -hc  # starts hazelcast config
sudo service <nuke/duke> debug -l   # starts log4j config
sudo service <nuke/duke> debug -dh  # shows a help with the debug options
```

### NUKE_HAZELCAST_CONFIG

links to the hazelcast config to use. 

### NUKE_LOG4J2_CONFIG

Links to the log4j configuration.

### DUKE_HAZELCAST_CLIENT_CONFIG

Links to the hazelcast config to use for the duke application.

### DUKE_LOG4J2_CONFIG

Links to the log4j configuration to use.

### DUKE_CONFIG_DIR

Links to the directory where duke can find xxml files for running tests from.

## Run configuration

Here is an example configuration.

```xml
<settings>
  <setting name="execute-min-nodes">1</setting>
  <setting name="execute-max-users">5</setting>
  <setting name="execute-repeat">true</setting>
  <setting name="execute-interval">10</setting>
  <setting name="execute-user-inc">2</setting>
  <setting name="execute-peak-delay">10</setting>
  <setting name="execute-terminate">120</setting>
  <setting name="execute-command">sleep 2</setting>
  <setting name="execute-folder"></setting>
</settings>
```

 * *execute-min-nodes*  - The minimum number of nodes available before we start execution.
 * *execute-max-users*  - Maximum number of threads we should run in paralell.
 * *execute-repeat*     - Should the issued commmand be repeated when done or onlye run once.
 * *execure-interval*   - The interval to rampup the threads in seconds.
 * *execute-user-inc*   - Number of threads to rampup each interval.
 * *execute-peak-delay* - Number of seconds to stay at the maximum threads after reaching it before ramping down.
 * *execute-terminate*  - Number of seconds since start before we terminate applicate, used as a guard.
 * *execute-command*    - The command to execute.
 * *execute-folder*     - The folder to execute the command from, this can for example be a ccumber test suite.

The following will result in the following "graph" during execution.

```
threads
        a    b    c    d    e                                      f
      5 |              #####                                       | 
      4 |         ##########                                       | 
      3 |         ##########                                       |
      2 |    ###############                                       |
      1 |    ###############                                       |
      0 ------------------------------------------------------------------
        0   10   20   30   40   50   60   70   80   90  100  110  120  time
```

Events that will happen:
 * a - We have found 1 node that can start execcution and we start the task.
 * b - After 10 seconds we tell the node to start 2 threads to execute *sleep 2* over and over again.
 * c - After 20 seconds we tell the node to start another 2 threads 
 * d - After 30 seconds we tell the node to start another thread (only 1 due to reaching max). We also start the peak delay phase for 10 seconds in this case. 
 * e - After 40 seconds the peak delay is finished and we issue a stop command to do a nice shutdown of all threads. In this case the sleeps will run for a maximum of 2 seconds and the stop.
 * f - In case of the threads not stopping nice we will issue a terminate command to the nodes to terminate all threads started by force. 

