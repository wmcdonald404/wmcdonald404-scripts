Pre-reqs:

- jars/commons-cli-1.0.jar
- jars/ojdbc7.jar
- jars/jt400.jar

To use:

- Compile the Java class

```
export CLASSPATH="${CLASSPATH}:$(pwd)/jars/commons-cli-1.0.jar"
export CLASSPATH="${CLASSPATH}:$(pwd)/jdbc-test"
javac ./jdbc-test/TestJdbcConnection.java
```

- Run the test

```
cd ./jdbc-test
java TestJdbcConnection
```
