#!/bin/sh

usage() {
  local message="$1"
  if [[ -n $message ]]; then
    echo "$message"
  fi
  exit
}

[[ $EUID -ne 0 ]] && usage "Need to run as root."

echo "Load useradd settings"
. /etc/default/useradd

echo "Removing items from chkconfig"
/sbin/chkconfig --del duke 2>/dev/null || echo "duke not installed"
/sbin/chkconfig --del nuke 2>/dev/null || echo "nuke not installed"

echo "Removing old files"
[[ -d /etc/opus ]] && rm -rf /etc/opus
[[ -d /usr/share/java/opus ]] && rm -rf /usr/share/java/opus
[[ -d /usr/share/javadoc/opus ]] && rm -rf /usr/share/javadoc/opus
[[ -d /var/log/opus ]] && rm -rf /var/log/opus

echo "Creating user"
/usr/bin/getent group opus || /usr/sbin/groupadd -r opus
/usr/bin/getent passwd opus || /usr/sbin/useradd -r -d /home/opus -g opus -s /bin/bash opus

if [ ! -d /home/opus ]; then
  echo "useradd did not create home directory for opus user"
  mkdir -p /home/opus
  /bin/chown opus:opus -R /home/opus
fi

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
/sbin/chkconfig --add nuke
/sbin/chkconfig --add duke

echo "Setting up folder permissions"
/bin/chown opus:opus -R /etc/opus
/bin/chown opus:opus -R /usr/share/java/opus
/bin/chown opus:opus -R /usr/share/javadoc/opus
/bin/chown opus:opus -R /var/log/opus

echo "Setting rwx rights on folders"
/bin/chmod 775 -R /etc/opus


