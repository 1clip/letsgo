#Service Starter
###pre-requirement
0. install zookeeper

```
brew update
brew install zookeeper
```
0. start zookeeper

```
zkServer start
```

###build the project
```
cd workspace/letsgo
mvn clean install
```

###start services
```
cd service-starter
mvn exec:java -Dexec.mainClass="coffee.letsgo.dev.ServiceStarter"
```
