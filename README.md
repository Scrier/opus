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

# Dependencies
The following libraries external libraries is used by opus.

## Application

 * Hazelcast 3.3.1 - http://hazelcast.org/
 * Log4j2 2.1 - http://logging.apache.org/log4j/2.x/

## Testing

 * JUnit 4.11 - http://junit.org/
 * Mockito 1.9.5 - https://code.google.com/p/mockito/

# Configuration
Configuration is divided in 3 parts. Hazelcast, Log4j2 and the service opus.

## Hazelcast 

Installed under /etc/opus with filename hazelcastDukeConfig.xml or hazelcastNukeConfig.xml

Most information should be available on their homepage for setting this up. Here are some links that is well worth the reading.

 * http://docs.hazelcast.org/docs/3.3/manual/html-single/hazelcast-documentation.html
 * https://github.com/hazelcast/hazelcast/blob/master/hazelcast/src/main/resources/hazelcast-default.xml - example config explained

### Group

If you want to run local instance and avoid collissions with existing clusters you need to rename the group name for your local cluster.


## Log4j2

Installed under /etc/opus with filename log4j2duke.xml and log4j2nuke.xml

## opus

Installed under /etc/opus/opus.conf


