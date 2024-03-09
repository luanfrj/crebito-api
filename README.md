# crebito-api

Projeto para o desafio da Rinha de Backend:

Stack utilizada:
- Apache Camel;
- Quarkus JVM;
- Hazelcast;
- PostgreSQL;

## Como compilar

Para compilar basta executar o comando maven abaixo:
```
mvn clean package -Dquarkus.package.type=uber-jar
```

## Como executar
Para executar a aplicação basta executar jar da aplicação:

```
java -jar target/*.jar
```

## Configuração 
A configuração pode ser feita usando as seguintes variáveis de ambiente:
- DB_HOSTNAME: hostname do banco de dados;
- DB_INIT_SIZE: tamanho inicial do pool de conexões com o banco de dados;
- DB_MIN_SIZE: tamanho mínimo do pool;
- DB_MIN_SIZE tamanho máximo do pool;
- LOG_LEVEL: nível de log (TRACE, DEBUG, INFO, etc)
- HTTP_HOST: host de exposição do serviço;
- HTTP_PORT: porta http exposta;
- HAZELCAST_HOST: lista de endereços do cluster separadas por virgula;
