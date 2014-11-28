#/bin/sh
usage() {
  echo " Usage: $0"
  echo "    or: $0 <xmlconfig>"
  exit
}

error() {
  echo $1
  usage
}

[ $# -gt 1 ] && usage

pwd=`pwd`
duke_path=$pwd/duke
duke_target=$duke_path/target
duke=$duke_target/duke*.jar
config=$duke_path/hazel*.xml

duke_name=$pwd/duke.jar
config_name=$pwd/dukeHazelcastConfig.xml

[[ -z $duke ]] && error "No jar file gound in $duke_target, have you compiled?"
[[ -z $config ]] && error "No hazelcast config file found in $duke_path"

if [[ -L $duke_name ]]; then
  rm -f $duke_name
fi

if [[ -L $config_name ]]; then
  rm -f $config_name
fi

ln -s $duke $duke_name
ln -s $config $config_name

if [ $# == 1 ]; then
  echo "java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=$pwd/log4j2config.xml -Dhazelcast.client.config=$config_name -jar $duke_name $1"
  java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=$pwd/log4j2config.xml -Dhazelcast.client.config=$config_name -jar $duke_name $1
else
  echo "java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=$pwd/log4j2config.xml -Dhazelcast.client.config=$config_name -jar $duke_name"
  java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=$pwd/log4j2config.xml -Dhazelcast.client.config=$config_name -jar $duke_name
fi

