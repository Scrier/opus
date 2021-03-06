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
duke=$(find $duke_path -regex ".*\/target\/.*\.jar" -not -regex ".*\/original.*" -not -regex ".*javadoc\.jar")
config=$duke_path/hazel*.xml

duke_name=$pwd/duke.jar
local_folder=$pwd/files/local
config_name=$local_folder/dukeHazelcastConfig.xml
log4jconfig=$local_folder/dukelog4j2config.xml

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
  sed -i "s|\(<setting name=\"execute-folder\">\)[^<>]*\(</setting>\)|\1${local_folder}\2|" ./files/local/TestSettings.xml
  echo "java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=$log4jconfig -Dhazelcast.client.config=$config_name -jar $duke_name $1"
  java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=$log4jconfig -Dhazelcast.client.config=$config_name -jar $duke_name $1
else
  echo "java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=$log4jconfig -Dhazelcast.client.config=$config_name -jar $duke_name"
  java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=$log4jconfig -Dhazelcast.client.config=$config_name -jar $duke_name
fi

