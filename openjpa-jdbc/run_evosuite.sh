# -Dlog.level=DEBUG
# -Dsandbox=false
# -Dfilter_sandbox_tests=true
# -Dsandbox_mode=OFF
# -Dtest_dir=/home/fmasci/openjpa-testing/openjpa-kernel/evosuite-tests \
# -Dcores=16
# -Dfunctional_mocking_percent=1
# -Duse_separate_classloader=false
# -projectCP ../openjpa/target/classes:../openjpa-all/target/classes:../openjpa-lib/target/classes:target/classes:$(cat cp.txt) \

#../openjpa-lib/target/classes
#../openjpa-kernel/target/classes
#../openjpa-jdbc/target/classes
#../openjpa-persistence/target/classes
#../openjpa-persistence-jdbc/target/classes
#../openjpa-examples/target/classes
#../openjpa-project/target/classes
#../openjpa-tools/target/classes

CP_REPO="/home/fmasci/.m2/repository/jakarta/persistence/jakarta.persistence-api/3.1.0/jakarta.persistence-api-3.1.0.jar:\
/home/fmasci/.m2/repository/org/apache/derby/derby/10.15.2.0/derby-10.15.2.0.jar:\
/home/fmasci/.m2/repository/org/apache/derby/derbyclient/10.15.2.0/derbyclient-10.15.2.0.jar:\
/home/fmasci/.m2/repository/org/apache/commons/commons-dbcp2/2.13.0/commons-dbcp2-2.13.0.jar:\
/home/fmasci/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar:\
/home/fmasci/.m2/repository/org/apache/bval/bval-core/1.1.2/bval-core-1.1.2.jar:\
/home/fmasci/.m2/repository/org/apache/bval/org.apache.bval.bundle/3.0.2/org.apache.bval.bundle-3.0.2.jar"

CP_OPENJPA_JARS=$(find ~/.m2/repository/org/apache/openjpa/ -name "*.jar" | tr '\n' ':')

CP_CLASSES="/home/fmasci/openjpa-testing/openjpa-lib/target/classes:\
/home/fmasci/openjpa-testing/openjpa-kernel/target/classes:\
/home/fmasci/openjpa-testing/openjpa-jdbc/target/classes:\
/home/fmasci/openjpa-testing/openjpa-persistence/target/classes:\
/home/fmasci/openjpa-testing/openjpa-persistence-jdbc/target/classes"

# Pulisci cp.txt da linee vuote e spazi finali
CPTXT=$(grep -v '^[[:space:]]*$' cp.txt | tr '\n' ':')

FULL_CP="target/classes:target/generated-sources:$CP_REPO:$CP_OPENJPA_JARS:$CPTXT"

FULL_CP=$(echo "$FULL_CP" | sed 's/::/:/g' | sed 's/:$//')
FULL_CP=${FULL_CP%:}

java -Xmx4G -jar /home/fmasci/Programmi/evosuite-1.2.0.jar \
      -class org.apache.openjpa.jdbc.sql.DBDictionary \
      -projectCP "$FULL_CP" \
      -Dsandbox=false \
      -Dsearch_budget=5