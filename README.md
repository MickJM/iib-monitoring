# iib-monitoring 

A Java Spring Boot micro-service, to get monitoring metrics from IBM Integration Bus (IIB v10).

Metrics are generated in Prometheus format.

The following metrics are returned;

# build 
mvn clean install -DskipTests


https://nickolasfisher.com/blog/A-Concise-Guide-to-Using-Jasypt-In-Spring-Boot-for-Configuration-Encryption

# Encrpypt string 

java -cp C:\Users\mickm\.m2\repository\org\jasypt\jasypt\1.9.3\jasypt-1.9.3.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input=”Passw0rd″ password=secretkey algorithm=PBEWithMD5AndDES

# Decrpypt string 

java -cp C:\Users\mickm\.m2\repository\org\jasypt\jasypt\1.9.3\jasypt-1.9.3.jar org.jasypt.intf.cli.JasyptPBEStringDecryptionCLI input=”8o/WhBx/04qezP8KiJEtxPt6iGs0qs7u″ password=secretkey algorithm=PBEWithMD5AndDES


https://mbcoder.com/spring-boot-how-to-encrypt-properties-in-application-properties/

