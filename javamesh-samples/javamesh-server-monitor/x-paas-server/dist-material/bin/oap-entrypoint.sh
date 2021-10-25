set -e

EXT_LIB_DIR=/home/apps/oap/ext-libs

umask 0027

CLASSPATH="config:"
for i in oap-libs/*.jar
do
    CLASSPATH="$i:$CLASSPATH"
done
for i in "${EXT_LIB_DIR}"/*.jar
do
    CLASSPATH="$i:$CLASSPATH"
done

if java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -version; then
  JAVA_OPTS="${JAVA_OPTS} -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
fi

exec java ${JAVA_OPTS} ${SELF_MONITOR} -classpath ${CLASSPATH} org.apache.skywalking.oap.server.starter.OAPServerStartUp "$@"