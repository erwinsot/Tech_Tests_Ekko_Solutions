-- Cambiar al contenedor XEPDB1
ALTER SESSION SET CONTAINER = XEPDB1;

-- Crear usuario con contraseña segura
CREATE USER libros_user IDENTIFIED BY "Libros_2024$Secure";

-- Asignar privilegios y espacio
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE PROCEDURE, CREATE VIEW TO libros_user;
ALTER USER libros_user DEFAULT TABLESPACE users QUOTA UNLIMITED ON users;

-- Conectar como el nuevo usuario
CONNECT libros_user/"Libros_2024$Secure"@XEPDB1

-- Crear tablas y secuencias
CREATE TABLE autores (
                         autor_id NUMBER PRIMARY KEY,
                         nombre VARCHAR2(100) NOT NULL,
                         fecha_nacimiento DATE,
                         nacionalidad VARCHAR2(50)
);

CREATE SEQUENCE seq_autor_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE libros (
                        libro_id NUMBER PRIMARY KEY,
                        titulo VARCHAR2(200) NOT NULL,
                        isbn VARCHAR2(20) UNIQUE,
                        fecha_publicacion DATE,
                        autor_id NUMBER,
                        CONSTRAINT fk_autor FOREIGN KEY (autor_id) REFERENCES autores(autor_id)
);

CREATE SEQUENCE seq_libro_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- Paquete completo con todas las funciones implementadas y corregidas
CREATE OR REPLACE PACKAGE libros_pkg AS
    PROCEDURE insertar_autor(
        p_nombre IN VARCHAR2,
        p_fecha_nacimiento IN DATE,
        p_nacionalidad IN VARCHAR2,
        p_autor_id OUT NUMBER
    );

    PROCEDURE actualizar_autor(
        p_autor_id IN NUMBER,
        p_nombre IN VARCHAR2,
        p_fecha_nacimiento IN DATE,
        p_nacionalidad IN VARCHAR2
    );

    PROCEDURE eliminar_autor(
        p_autor_id IN NUMBER,
        p_resultado OUT NUMBER
    );

    PROCEDURE insertar_libro(
        p_titulo IN VARCHAR2,
        p_isbn IN VARCHAR2,
        p_fecha_publicacion IN DATE,
        p_autor_id IN NUMBER,
        p_libro_id OUT NUMBER
    );

    PROCEDURE actualizar_libro(
        p_libro_id IN NUMBER,
        p_titulo IN VARCHAR2,
        p_isbn IN VARCHAR2,
        p_fecha_publicacion IN DATE,
        p_autor_id IN NUMBER
    );

    PROCEDURE eliminar_libro(
        p_libro_id IN NUMBER
    );

    FUNCTION obtener_autor(p_autor_id IN NUMBER) RETURN SYS_REFCURSOR;
    FUNCTION obtener_libro(p_libro_id IN NUMBER) RETURN SYS_REFCURSOR;
    FUNCTION listar_autores RETURN SYS_REFCURSOR;
    FUNCTION listar_libros RETURN SYS_REFCURSOR;
    FUNCTION listar_libros_por_autor(p_autor_id IN NUMBER) RETURN SYS_REFCURSOR;
END libros_pkg;
/

CREATE OR REPLACE PACKAGE BODY libros_pkg AS

    PROCEDURE insertar_autor(
        p_nombre IN VARCHAR2,
        p_fecha_nacimiento IN DATE,
        p_nacionalidad IN VARCHAR2,
        p_autor_id OUT NUMBER
    ) IS
BEGIN
        p_autor_id := seq_autor_id.NEXTVAL;
INSERT INTO autores(autor_id, nombre, fecha_nacimiento, nacionalidad)
VALUES(p_autor_id, p_nombre, p_fecha_nacimiento, p_nacionalidad);
EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20001, 'Error al insertar autor: ' || SQLERRM);
END insertar_autor;

    PROCEDURE actualizar_autor(
        p_autor_id IN NUMBER,
        p_nombre IN VARCHAR2,
        p_fecha_nacimiento IN DATE,
        p_nacionalidad IN VARCHAR2
    ) IS
BEGIN
UPDATE autores
SET nombre = p_nombre,
    fecha_nacimiento = p_fecha_nacimiento,
    nacionalidad = p_nacionalidad
WHERE autor_id = p_autor_id;

IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20002, 'Autor no encontrado');
END IF;
EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20003, 'Error al actualizar autor: ' || SQLERRM);
END actualizar_autor;

    PROCEDURE eliminar_autor(
        p_autor_id IN NUMBER,
        p_resultado OUT NUMBER
    ) IS
        v_count NUMBER;
BEGIN
SELECT COUNT(*) INTO v_count FROM libros WHERE autor_id = p_autor_id;

IF v_count = 0 THEN
DELETE FROM autores WHERE autor_id = p_autor_id;
p_resultado := 1;
ELSE
            p_resultado := 0;
END IF;
EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20004, 'Error al eliminar autor: ' || SQLERRM);
END eliminar_autor;

    PROCEDURE insertar_libro(
        p_titulo IN VARCHAR2,
        p_isbn IN VARCHAR2,
        p_fecha_publicacion IN DATE,
        p_autor_id IN NUMBER,
        p_libro_id OUT NUMBER
    ) IS
        v_count NUMBER;
BEGIN
SELECT COUNT(*) INTO v_count FROM autores WHERE autor_id = p_autor_id;
IF v_count = 0 THEN
            RAISE_APPLICATION_ERROR(-20005, 'El autor especificado no existe');
END IF;

        p_libro_id := seq_libro_id.NEXTVAL;
INSERT INTO libros(libro_id, titulo, isbn, fecha_publicacion, autor_id)
VALUES(p_libro_id, p_titulo, p_isbn, p_fecha_publicacion, p_autor_id);
EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20006, 'Error al insertar libro: ' || SQLERRM);
END insertar_libro;

    PROCEDURE actualizar_libro(
        p_libro_id IN NUMBER,
        p_titulo IN VARCHAR2,
        p_isbn IN VARCHAR2,
        p_fecha_publicacion IN DATE,
        p_autor_id IN NUMBER
    ) IS
BEGIN
UPDATE libros
SET titulo = p_titulo,
    isbn = p_isbn,
    fecha_publicacion = p_fecha_publicacion,
    autor_id = p_autor_id
WHERE libro_id = p_libro_id;

IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20007, 'Libro no encontrado');
END IF;
EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20008, 'Error al actualizar libro: ' || SQLERRM);
END actualizar_libro;

    PROCEDURE eliminar_libro(
        p_libro_id IN NUMBER
    ) IS
BEGIN
DELETE FROM libros WHERE libro_id = p_libro_id;

IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20009, 'Libro no encontrado');
END IF;
EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20010, 'Error al eliminar libro: ' || SQLERRM);
END eliminar_libro;

    FUNCTION obtener_autor(p_autor_id IN NUMBER) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT * FROM autores WHERE autor_id = p_autor_id;
RETURN v_cursor;
EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20011, 'Error al obtener autor: ' || SQLERRM);
END obtener_autor;

    FUNCTION obtener_libro(p_libro_id IN NUMBER) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT * FROM libros WHERE libro_id = p_libro_id;
RETURN v_cursor;
EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20012, 'Error al obtener libro: ' || SQLERRM);
END obtener_libro;

    FUNCTION listar_autores RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT * FROM autores ORDER BY nombre;
RETURN v_cursor;
END listar_autores;

    FUNCTION listar_libros RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT l.*, a.nombre as autor_nombre
FROM libros l
         JOIN autores a ON l.autor_id = a.autor_id
ORDER BY l.titulo;
RETURN v_cursor;
END listar_libros;

    FUNCTION listar_libros_por_autor(p_autor_id IN NUMBER) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
BEGIN
OPEN v_cursor FOR
SELECT * FROM libros WHERE autor_id = p_autor_id ORDER BY titulo;
RETURN v_cursor;
EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20013, 'Error al listar libros por autor: ' || SQLERRM);
END listar_libros_por_autor;

END libros_pkg;
/

-- Datos de ejemplo
DECLARE
v_autor_id NUMBER;
    v_libro_id NUMBER;
BEGIN
    -- Insertar autores y libros de ejemplo
    libros_pkg.insertar_autor('Gabriel García Márquez', TO_DATE('06/03/1927', 'DD/MM/YYYY'), 'Colombiano', v_autor_id);
    libros_pkg.insertar_libro('Cien años de soledad', '9780307474728', TO_DATE('30/05/1967', 'DD/MM/YYYY'), v_autor_id, v_libro_id);

    libros_pkg.insertar_autor('Jorge Luis Borges', TO_DATE('24/08/1899', 'DD/MM/YYYY'), 'Argentino', v_autor_id);
    libros_pkg.insertar_libro('Ficciones', '9788420674274', TO_DATE('01/01/1944', 'DD/MM/YYYY'), v_autor_id, v_libro_id);

    libros_pkg.insertar_autor('Isabel Allende', TO_DATE('02/08/1942', 'DD/MM/YYYY'), 'Chilena', v_autor_id);
    libros_pkg.insertar_libro('La casa de los espíritus', '9788401359254', TO_DATE('01/01/1982', 'DD/MM/YYYY'), v_autor_id, v_libro_id);

COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error al insertar datos de ejemplo: ' || SQLERRM);
ROLLBACK;
END;
/

-- Verificar que el paquete se compiló correctamente
SELECT object_name, object_type, status
FROM user_objects
WHERE object_name IN ('LIBROS_PKG', 'AUTORES', 'LIBROS');

-- Mostrar los datos insertados
SET SERVEROUTPUT ON
DECLARE
v_cursor SYS_REFCURSOR;
    v_autor_id NUMBER;
    v_nombre VARCHAR2(100);
    v_fecha_nacimiento DATE;
    v_nacionalidad VARCHAR2(50);
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== Autores ===');
    v_cursor := libros_pkg.listar_autores();
    LOOP
FETCH v_cursor INTO v_autor_id, v_nombre, v_fecha_nacimiento, v_nacionalidad;
        EXIT WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE(v_autor_id || ' | ' || v_nombre || ' | ' ||
                            TO_CHAR(v_fecha_nacimiento, 'DD/MM/YYYY') || ' | ' || v_nacionalidad);
END LOOP;
CLOSE v_cursor;

DBMS_OUTPUT.PUT_LINE('=== Libros ===');
    v_cursor := libros_pkg.listar_libros();
    DECLARE
v_libro_id NUMBER;
        v_titulo VARCHAR2(200);
        v_isbn VARCHAR2(20);
        v_fecha_publicacion DATE;
        v_autor_id NUMBER;
        v_autor_nombre VARCHAR2(100);
BEGIN
        LOOP
FETCH v_cursor INTO v_libro_id, v_titulo, v_isbn, v_fecha_publicacion, v_autor_id, v_autor_nombre;
            EXIT WHEN v_cursor%NOTFOUND;
            DBMS_OUTPUT.PUT_LINE(v_libro_id || ' | ' || v_titulo || ' | ' || v_isbn || ' | ' ||
                                TO_CHAR(v_fecha_publicacion, 'DD/MM/YYYY') || ' | ' || v_autor_nombre);
END LOOP;
CLOSE v_cursor;
END;
END;
/