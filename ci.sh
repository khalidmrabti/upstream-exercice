echo "installing model"
SERVICE="fleet-stations"
cd ../fleet/model
mvn clean install
echo "installing business"
cd ../fleet-business
mvn clean install
cd ../../fleet-stations
mvn clean package
echo "deleting ${SERVICE} old image"
docker rmi -f gcr.io/eu-fleet/"${SERVICE}":1.0.0
echo "building ${SERVICE} image"
docker build . -t gcr.io/eu-fleet/"${SERVICE}":1.0.0
echo "pushing ${SERVICE} image"
docker push gcr.io/eu-fleet/"${SERVICE}":1.0.0
cd ..
