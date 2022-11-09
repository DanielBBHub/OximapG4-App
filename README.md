# OximapG4-App
Repositorio para albergar el código relacionado a la aplicación del proyecto
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
PROYECTO DE BIOMETRIA: Repositorio app

En este repositorio se encuentran los códigos correspondientes a la aplicación y conexión entre arduino, app y servidor.
La aplicación se divide en diferentes actividades, empezando desde la actividad para iniciar sesión, donde nos podemos registrar por google o por correo, y esto nos llevará al MainActivity, donde se establece
la conexión entre la app, arduino y servidor.
Posteriormente podremos visualizar nuestros datos desde la actividad Perfil, así como editar nuestros datos desde la actividad EditarPerfil.
El servidor ha sido mediante reglas REST. Se encuentra diferenciado entre la lógica del negocio y la lógica fake, correspondiente a la parte de UX (donde encontramos el html en el cual se muestra la información).

Para ejecutar la app, en caso de tener andorid studio y el proyecto, hay que seleccionar un dispositivo y darle al "run" para instalar la aplicación en el dispositivo. En caso de tener el .apk, habrá que acceder a 
él desde archivos y ponerlo a instalar.

Para poder acceder a la app, es necesario registrarse, ya sea mediante correo y contraseña o por google.

La app se enceuntra dividida en dos partes, la parte de clases java (en la carpeta app>src>main>java) y la parte de diseño (en la carpeta app>src>main>res).
