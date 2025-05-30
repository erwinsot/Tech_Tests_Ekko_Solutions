# Usamos una imagen base de OpenJDK (por ejemplo, Java 17)
FROM openjdk:17-jdk-slim

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el JAR construido al contenedor (ajusta el nombre según tu proyecto)
COPY /target/librosapi-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto en el que corre la aplicación (por defecto 8080 en Spring Boot)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
