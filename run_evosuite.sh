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

java -Xmx4G -jar /home/fmasci/Programmi/evosuite-1.2.0.jar \
      -class org.apache.openjpa.kernel.AttachManager \
      -projectCP openjpa-kernel/target/classes:openjpa-lib/target/classes:openjpa-kernel/target/classes:openjpa-jdbc/target/classes:openjpa-persistence/target/classes:openjpa-persistence-jdbc/target/classes:$(cat cp.txt) \
      -Dsearch_budget=5 \
      -criterion "line:branch" \
