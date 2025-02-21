# ParcialArepPrimerTercio

## La solucion del parcial esta en la rama MASTER

## Descripcion 
Usted debe construir un "Reflective ChatGPT". La solución consta de un servidor backend que responde a solicitudes HTTP POST y/o GET de la Facade, un Servidor Facade que responde a solicitudes HTTP POST y/o GET del cliente  y un cliente Html+JS que envía los comandos y muestra las respuestas. El api permite explorar clases del API de java. Cuando el usuario solicita información de una clase el chat le responde con el nombre de la clase, la lista de los campos declarados en la clase y la lista de los métodos declarados en la clase. Además el API debe permitir invocar y mostrar la salida de métodos estáticos con 0, 1 o 2 parámetros. Los parámetros de entrada pueden ser numéricos o Strings.

los comandos que soporta el chat son los siguientes:
1. Class([class name]): Retorna una lista de campos declarados y métodos declarados
2. invoke([class name],[method name]): retorna el resultado de la invocación del método.  Ejemplo: invoke(java.lang.System, getenv).
3. unaryInvoke([class name],[method name],[paramtype],[param value]): retorna el resultado de la invocación del método. paramtype = int | double | String.


