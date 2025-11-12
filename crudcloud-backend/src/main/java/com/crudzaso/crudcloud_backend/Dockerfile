# Etapa 1: Construcción del JAR
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen ligera de ejecución
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copiamos el JAR compilado
COPY --from=builder /app/target/*.jar app.jar

# Puerto de la app
EXPOSE 8080

# Cargar variables de entorno si existen
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
