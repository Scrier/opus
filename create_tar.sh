#!/bin/bash

system=${system-opus}
if [ -z "$1" ]; then
  version=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -Ev '(^\[|Download\w+:)')
else
  version="$1"
fi

if [ -z "$version" ]; then
  echo "Set version for release: "
  read version
else
  echo "version: $version"
fi

folder="$system-$version"
_javadir=/usr/share/java
_javadocdir=/usr/share/javadoc
_initdir=/etc/rc.d/init.d
_configdir=/etc/$system
_logdir=/var/log/$system
_lognukedir=$_logdir/nuke
_logdukedir=$_logdir/duke
#_mavenpomdir=/usr/share/maven-poms

echo "Version: $version"
echo "Folder: $folder"
echo "_javadir: $_javadir"
echo "_javadocdir: $_javadocdir"

# Create folders
mkdir $folder
mkdir -p $folder/{$_javadir/$system,$_javadocdir/$system,$_initdir,$_configdir,$_logdir,$_lognukedir,$_logdukedir}

# Copy jar files to correct directory.
find . -regex ".*\/target\/.*\.jar" -not -regex ".*\/original.*" -not -regex ".*javadoc\.jar" -exec cp {} $folder/$_javadir/$system \;
find . -regex ".*\/target\/.*javadoc\.jar" -exec cp {} $folder/$_javadocdir/$system \;

# Copy shell and config files
cp files/service/*.sh $folder/$_initdir
cp files/log4j2/*.xml $folder/$_configdir
cp files/config/* $folder/$_configdir
cp files/setup/*.sh $folder

# Edit rights
chmod +x $folder/$_initdir/*
chmod +x $folder/*.sh

# Create tarball.
tar cvzf $system-$version.tar.gz $folder
rm -rf $folder

