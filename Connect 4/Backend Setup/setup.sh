# To generate a certificate, enter the command
# keytool -genkey -alias tomcat -keyalg RSA -keystore tomcat.jks

echo "Stopping Tomcat10"
sudo systemctl stop tomcat10
echo "Umounting old webapps folder"
sudo umount /var/lib/tomcat10/webapps
echo "Mounting new webapps folder"
sudo mount --bind ./Content /var/lib/tomcat10/webapps
echo "Copying config"
sudo cp server.xml /etc/tomcat10/server.xml
echo "Seting SLL config"
sudo cp -p tomcat.jks /etc/tomcat10/tomcat.jks
echo "Starting Tomcat10"
sudo systemctl start tomcat10
