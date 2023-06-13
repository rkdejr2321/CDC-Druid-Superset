curl --location --request GET '132.226.238.176:8085/connector-plugins'

curl --location --request GET  '132.226.238.176:8083/connectors/mysql-connector/config'  --header 'Content-Type:application/json'
#curl --location --request GET  'http://localhost:8083/connectors/oracle2-connector/config'  --header 'Content-Type:application/json'