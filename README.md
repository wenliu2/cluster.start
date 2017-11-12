1. Change $HOME/.m2/settings.xml as below to use maven repository in China:

<settings xmlns="http://maven.apache.org/SETTINGS/1.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 http://maven.apache.org/xsd/settings-1.1.0.xsd">
  <mirrors>
    <mirror>
      <id>CN</id>
      <name>China Central</name>
      <url>http://maven.aliyun.com/nexus/content/groups/public/</url> <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>

2. Download zookeeper and start it on localhost:2181 before start the server.
