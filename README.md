
# CDC  - Kafka, Debezium, Spring Boot
Kafka, Debezium, Spring Boot, MySQL, ORACLE을 사용하여 CDC 환경 구축 (DML)
REST API를 이용하여 Debezuim Source Connect를 생성하고, Spring boot와 kafka 연동을 통해 Sink Connect 생성..

### 구조도

- 소스 DB : MySQL
- 타겟 DB : Oracle, MySQL
- Source Connector : debezium
- Sink Connector : Spring boot (jdbc, ojdbc)


![Untitled 1](https://user-images.githubusercontent.com/72342550/197522848-9b8694fa-d7b5-4d0e-ae1f-2663f68b65d3.png)


### 작업환경

- MacBook Air(M1, 2020)
- OS : macOS Monterey v12.6
- Memory : 16GB

### 버 전

- Docker :  version 20.10.12, build e91ed57
- JDK : 11 AdoptOpenJDK (HotSpot)
- JDBC : spring-jdbc 5.3.9
- OJDBC : ojdbc8 v12.2.0.1
- Oracle : 12c (카카오 인스턴스 도커환경으로 구성)
- Spring boot : 2.5.4
- Spirng boot data JPA : 2.5.4
- Spring-kafka

### 도커 컨테이너 (Compose)

Mysql, Kafka, Zookeper, Debezium

```docker
version: "3.8"

services:
  mysql:
    container_name: mysql
    image: debezium/example-mysql
    restart: always
    ports:
      - 3307:3306
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: db
      
  kafka:
    container_name: kafka
    image: confluentinc/cp-kafka:latest
    restart: always
    depends_on:
      - zookeeper
    ports:
      - 9092:9092
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_BROKER_ID: 1
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      
  zookeeper:
    container_name: zookeeper
    image: confluentinc/cp-zookeeper:latest
    ports:
      - 2181:2181
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
      
  debezium:
    container_name: debezium
    image: debezium/connect:latest
    ports:
      - 8083:8083
    depends_on:
      - zookeeper
      - kafka
      - mysql
    environment:
      GROUP_ID: 1
      BOOTSTRAP_SERVERS: kafka:29092
      CONFIG_STORAGE_TOPIC: my_connect_configs
      OFFSET_STORAGE_TOPIC: my_connect_offsets
      STATUS_STORAGE_TOPIC: my_connect_statuses
```

 * debezium에서 제공하는 example-mysql 및 connect 사용.

 * 추가 설정 및 플러그인 설치 필요 없음.



### Source Connector 생성

```json
{
    "name": "mysql-connector",
    "config": {
        "connector.class": "io.debezium.connector.mysql.MySqlConnector",
        "tasks.max": "1",
        "database.hostname": "mysql",
        "database.port": "3306",
        "database.user": "root",
        "database.password": "root",
        "database.server.id": "184054",
        "database.server.name": "dbserver1",
        "database.include.list": "src_db",
        "database.history.kafka.bootstrap.servers": "kafka:29092",
        "database.history.kafka.topic": "schema-changes.db",
        "table.whitelist": "src_db.user",
        "include.schema.changes": "true"
    }
}
```

### Topic 확인

![Untitled 2](https://user-images.githubusercontent.com/72342550/197522880-9ec8476b-a3a7-4389-bd1d-c72c9801fe48.png)


### DB 구성

- Mysql : src_db, target_db 2개로 나누고 각각의 테이블 구성
    

    ![Untitled 3](https://user-images.githubusercontent.com/72342550/197522898-6fa6b948-d80f-4c38-b95f-fe97e63ce11c.png)


- Oracle : TB_TARGET table 구성
    
![Untitled 4](https://user-images.githubusercontent.com/72342550/197522917-c8f6e634-0656-4a38-af44-c82ac202b00b.png)

    

- 모든 테이블은 아래와 같이 구성 (아이디와 이름)
    

    ![Untitled 5](https://user-images.githubusercontent.com/72342550/197522932-80853857-2f58-4084-94e5-c5a0c2bfc61e.png)


- 테이블 작성 SQL문
    
    ```sql
    ### MYSQL ###
    CREATE TABLE user (
    id int primary key not null auto_increment,
    name varchar(255));
    
    ### ORACLE ###
    CREATE TABLE tb_target (
    id number(4) not null primary key,
    name VARCHAR2(255));
    
    ### ORACLE은 auto_increment 지원 X, sequence 생성하여 이용.
    ```
    

### Spring 구성

- Kafka Consumer 설정 (KafkaConsumerConfig.java)
    
    ```sql
       @Bean
        public Map<String, Object> consumerConfigs() {
            Map<String, Object> configurations = new HashMap<>();
    				## kafka bootstrap 서버주소
            configurations.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    				## Group ID Debezium -> dbz
            configurations.put(ConsumerConfig.GROUP_ID_CONFIG, "dbz");
    				## 직렬화, 역직렬화 설정
            configurations.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            configurations.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    				## offset 설정 (kafka에 offset이 없을 경우 자동으로 earliest로 재설정)
            configurations.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    				## poll 함수 단일 호출에서 반환된 최대 레코드 수
            configurations.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");
            return configurations;
        }
    }
    ```
    

- KafkaListener 함수 (Consumer.java)
    
    ```sql
    @KafkaListener(topics = "dbserver1.src_db.user")
        public void consumeUser(ConsumerRecord<String, String> record) throws JsonProcessingException {
            String consumedValue = record.value();
    
            var jsonNode = mapper.readTree(consumedValue);
            JsonNode payload = jsonNode.path("payload");
            JsonNode after = payload.path("after");
            System.out.println(payload);
            System.out.println(after.get("name").toString());
    
            User mysqlUser = User.builder()
                    .name(after.get("name").toString())
                    .build();
    
            OracleUser oracleUser = OracleUser.builder()
                    .name(after.get("name").toString())
                    .build();
    
            secondUserRepository.save(mysqlUser);
            oracleUserRepository.save(oracleUser);
        }
    ```
    

### 결 과

- 소스DB에 데이터 입력/삽입
    
![Untitled 6](https://user-images.githubusercontent.com/72342550/197522982-ec924913-632b-4121-afbc-1b95764c5cc7.png)

    

- kafka-console-consumer 확인
    

    ![Untitled 7](https://user-images.githubusercontent.com/72342550/197522994-382e62ec-ba2b-4d94-9275-d11f6877fe67.png)


- spring-boot console 확인
    
![Untitled 8](https://user-images.githubusercontent.com/72342550/197523008-5129aef6-cf73-4745-8c4d-78800c1ae635.png)

    

- Mysql target db 확인
    

    ![Untitled 9](https://user-images.githubusercontent.com/72342550/197523018-aecfea3d-bb19-400d-b96a-d1b75a2611f1.png)


- Oracle target db 확인
    
    
![Untitled 10](https://user-images.githubusercontent.com/72342550/197523039-d2218bfd-01db-4bb6-8a8e-875d18539931.png)

# Trouble Shooting

### Spring Boot Project 진행 중 - 오라클 커넥트 생성

- 오라클 커넥트를 아래와 같이 생성
    
    ```json
    {
        "name": "oracle-connector",
        "config": {
            "connector.class" : "io.debezium.connector.oracle.OracleConnector",
            "database.hostname" : "210.109.60.233",
            "database.port" : "1521",
            "database.user" : "logminer",
            "database.password" : "logminer",
            "database.dbname" : "XE",
            "database.server.name" : "dbserver2",
            "tasks.max" : "1",
            "table.include.list" : "LOGMINER.TB_SRC",
            "database.history.kafka.bootstrap.servers" : "kafka:29092",
            "database.history.kafka.topic": "schema-changes.inventory"
        }
    }
    ```
    
    - license 문제로 debezium에서 ojdbc를 제공하지 않음 → 직접 다운받아 libs에 넣어주어야함
- topic 생성 확인
    

    ![Untitled 12](https://user-images.githubusercontent.com/72342550/197523148-42d4d145-7ce4-484f-b4e8-ca25b7608e8c.png)


- 초기에는 Consumer가 메세지를 잘 받는 것을 확인, 점점 메세지 받아오는게 느려지더니, debezium connect에서 unregistered 되는 현상 발견…
- 원인 파악 중입니다..

# Reference

- 카카오엔터프라이즈 CDC pilot - Readme
- 5주차 강의자료 - Streaming data with Debezium, DBMS and Kafka
- [https://debezium.io/documentation/reference/stable/connectors/oracle.html](https://debezium.io/documentation/reference/stable/connectors/oracle.html)
- [https://debezium.io/documentation/reference/stable/connectors/mysql.html](https://debezium.io/documentation/reference/stable/connectors/mysql.html)
- [https://semtax.tistory.com/83](https://semtax.tistory.com/83)
- [https://www.baeldung.com/spring-kafka#consuming-messages](https://www.baeldung.com/spring-kafka#consuming-messages)

## 감사합니다. 🥰

방근호
G-MAIL : panggeunho@gmail.com
Github : [https://github.com/banggeunho](https://github.com/banggeunho)
