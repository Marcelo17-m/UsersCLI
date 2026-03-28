# Users CLI - Guía de Configuración y Ejecución

Este proyecto es una herramienta de línea de comandos (CLI) escrita en Java para gestionar usuarios en una base de datos MySQL utilizando MyBatis.

## Requisitos Previos
* **Docker** y **Docker Compose**
* **Java 17** (o superior)
* **Maven**

---

## 1. Configuración de la Base de Datos

Levantaremos un contenedor de MySQL que incluye la creación automática de todas las tablas del proyecto.

1. Abre una terminal en la raíz del proyecto.
2. Ejecuta el siguiente comando:
   ```bash
   docker-compose -f db/docker-compose.yml up -d
   ```

*Esto creará la base de datos `sd3` en el puerto **3314** con la contraseña `sd5` y ejecutará los siguientes scripts en orden:*

| Script | Tabla creada |
|---|---|
| `01_init.sql` | `users` |
| `02_topics.sql` | `topics` |
| `03_ideas.sql` | `ideas` |
| `04_votes.sql` | `votes` |

> Los scripts solo se ejecutan la primera vez (volumen vacío). Para forzar una re-inicialización: `docker-compose -f db/docker-compose.yml down -v && docker-compose -f db/docker-compose.yml up -d`

---

## 2. Compilación del Proyecto

Para generar el archivo ejecutable (`.jar`), utiliza Maven:

```bash
mvn clean package

```

*El archivo generado se ubicará en la carpeta `target/`.*

---

## 3. Uso de la Aplicación

La aplicación requiere siempre el parámetro `-config` apuntando al archivo XML de configuración.

### Ver lista de usuarios (Read)

```bash
java -jar target/userscli-1.0-SNAPSHOT.jar -config=sd3.xml -read

```

### Crear un usuario (Create)

```bash
java -jar target/userscli-1.0-SNAPSHOT.jar -config=sd3.xml -create -n "Nombre Usuario" -l "login123" -p "password456"

```

### Eliminar un usuario (Delete)

```bash
java -jar target/userscli-1.0-SNAPSHOT.jar -config=sd3.xml -delete <ID_DEL_USUARIO>

```

### Actualizar un usuario (Update)

```bash
java -jar target/userscli-1.0-SNAPSHOT.jar -config=sd3.xml -update -i <ID> -n "Nuevo Nombre" -l "nuevoLogin" -p "nuevaPass"

```

---

## Notas Importantes

* **Archivo de Configuración:** El archivo `sd3.xml` debe estar en la raíz desde donde ejecutas el comando Java.
* **Puerto de Conexión:** Si cambias el puerto en el `docker-compose.yml`, recuerda actualizar la URL en `sd3.xml`.
* **Persistencia:** Si borras el contenedor, los datos se perderán a menos que configures un volumen de datos en el YAML.


Hay que asegurarse de que el archivo `sd3.xml` en la raíz tenga esta sección de conexión actualizada para que se comunique con el Docker:

```xml
<dataSource type="POOLED">
    <property name="driver" value="com.mysql.cj.jdbc.Driver" />
    <property name="url" value="jdbc:mysql://localhost:3307/sd3" />
    <property name="username" value="root" />
    <property name="password" value="sd5" />
</dataSource>
```
