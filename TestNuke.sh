#/bin/sh
usage() {
  echo " Usage: $0"
  echo "    or: $0 <hazelcastconfig>"
  exit
}

error() {
  echo $1
  usage
}

[ $# -gt 1 ] && usage

pwd=`pwd`
nuke_path=$pwd/nuke
nuke_target=$nuke_path/target
nuke=$nuke_target/nuke*.jar
config=$nuke_path/hazel*.xml

nuke_name=$pwd/nuke.jar
config_name=$pwd/nukeHazelcastConfig.xml
log4jconfig=$pwd/nukelog4j2config.xml

[[ -z $nuke ]] && error "No jar file gound in $nuke_target, have you compiled?"
[[ -z $config ]] && error "No hazelcast config file found in $nuke_path"

if [[ -L $nuke_name ]]; then
  rm -f $nuke_name
fi

if [[ -L $config_name ]]; then
  rm -f $config_name
fi

ln -s $nuke $nuke_name
ln -s $config $config_name

if [ $# == 1 ]; then
  echo "java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=$log4jconfig -Dhazelcast.config=$1 -jar $nuke_name"
  java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=$log4jconfig -Dhazelcast.config=$1 -jar $nuke_name
else
  echo "java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=$log4jconfig -Dhazelcast.config=$config_name -jar $nuke_name"
  java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=$log4jconfig -Dhazelcast.config=$config_name -jar $nuke_name
fi

