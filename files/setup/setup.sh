#!/bin/sh

usage() {
  local message="$1"
  if [[ -n $message ]]; then
    echo "$message"
  fi
  exit
}

[[ $EUID -ne 0 ]] && usage "Need to run as root."

echo "Removing items from chkconfig"
chkconfig --del duke
chkconfig --del nuke

echo "Removing old files"
rm -rf /etc/opus
rm -rf /usr/share/java/opus
rm -rf /usr/share/javadoc/opus
rm -rf /usr/log/opus

echo "Creating user"
getent group opus || groupadd -r opus
getent passwd opus || useradd -r -d /home/opus -g opus -s /bin/bash opus

echo "Creating directories"
mkdir -p {/etc/opus,/usr/share/java/opus,/usr/share/javadoc/opus,/var/log/opus/nuke,/var/log/opus/duke}

echo "Copying files"
cp -R ./etc/opus/* /etc/opus
mv ./etc/rc.d/init.d/duke.sh /etc/rc.d/init.d/duke
mv ./etc/rc.d/init.d/nuke.sh /etc/rc.d/init.d/nuke
cp -R ./usr/share/java/opus/* /usr/share/java/opus
cp -R ./usr/share/javadoc/opus/* /usr/share/javadoc/opus

echo "Creating symlinks"
ln -sf /usr/share/java/opus/common*.jar /usr/share/java/opus/common.jar
ln -sf /usr/share/java/opus/duke*.jar /usr/share/java/opus/duke.jar
ln -sf /usr/share/java/opus/nuke*.jar /usr/share/java/opus/nuke.jar

echo "Adding items to chkconfig"
chkconfig --add nuke
chkconfig --add duke

echo "Setting up folder permissions"
chown opus:opus -R /etc/opus
chown opus:opus -R /usr/share/java/opus
chown opus:opus -R /usr/share/javadoc/opus
chown opus:opus -R /var/log/opus

