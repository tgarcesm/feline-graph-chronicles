# Uso de IA en el proyecto

Este documento registra de forma honesta el uso de herramientas de inteligencia artificial durante el desarrollo de **The Feline Graph Chronicles**.

El uso de LLMs fue utilizado como apoyo para analizar requisitos, desarrollar, depurar, probar e integrar partes del proyecto. Las respuestas generadas no se asumieron automáticamente como correctas: se contrastaron con el enunciado, los contratos reales del repositorio, los samples oficiales y las pruebas automatizadas.

---

## Herramientas utilizadas

| Herramienta | Parte del proyecto | Integrante |
|---|---|---|
| ChatGPT (OpenAI) | Análisis del enunciado y de la guía, implementación y revisión de BFS, DFS, Disjoint Set y Kruskal, integración de las Misiones 1 y 4, GridRenderer, tests JUnit, Maven, Git y revisión de requisitos. | Samuel |
| Claude Code (Anthropic, Claude Sonnet 5) | Lectura del enunciado (PDF) y de la guia de equipo (Word), diseno e implementacion de Floyd-Warshall y Bellman-Ford en version de maximizacion, Mission3Churun (parser, precedencia de salida, matriz, cross-check), MatrixRenderer, Mission3Test, flujo de Git (rama, commits, Pull Request), preparacion de la defensa individual y revision de los Pull Requests de Samuel. | Tomas |
| PENDIENTE | Sebastian debe indicar qué herramienta de IA utilizó y para qué partes. | Sebastian |

### Uso realizado por Samuel

ChatGPT se utilizó principalmente como apoyo durante las siguientes actividades:

- Análisis detallado del enunciado original y de la guía de trabajo.
- Planeación de la arquitectura y separación entre algoritmos, misiones y GUI.
- Implementación y revisión de `Bfs.java`.
- Implementación y revisión de `Dfs.java`.
- Implementación de `Mission1Minefield.java`.
- Implementación y revisión de `DisjointSet.java`.
- Implementación y revisión de `Kruskal.java`.
- Implementación de `Mission4Network.java`.
- Implementación de `GridRenderer.java`.
- Creación y ampliación de `Mission1Test.java` y `Mission4Test.java`.
- Diagnóstico de problemas relacionados con Maven en IntelliJ y PowerShell.

El flujo de trabajo utilizado fue:

1. Revisar primero el requisito del profesor.
2. Proponer o generar una implementación.
3. Compararla con el código real del repositorio.
4. Compilar y ejecutar tests.
5. Comparar los resultados con los samples oficiales.
6. Corregir cualquier diferencia antes de integrar el código.

### Uso realizado por Tomas

Claude Code se uso como asistente de desarrollo dentro del propio entorno (terminal + editor), no como un chat aparte. Las actividades principales fueron:

- Extraccion y lectura completa del enunciado (`parcial.pdf`, 17 paginas) y de la guia de trabajo del equipo (`Guia_de_Trabajo_Feline_Graph_Chronicles.docx`), incluyendo la tabla de reparto de trabajo y el checklist final.
- Revision del estado real del repositorio (contratos ya subidos por Sebastian, commits existentes) antes de escribir una sola linea, para no inventar interfaces que ya existian.
- Implementacion de `FloydWarshall.java` (maximizacion todos-contra-todos con la pasada de marcado de ciclos no acotados).
- Implementacion de `BellmanFord.java` (maximizacion desde un origen, deteccion y propagacion de ciclos de ganancia positiva, reconstruccion de la ruta y del ciclo responsable).
- Implementacion de `Mission3Churun.java` (parser, precedencia de los tres mensajes, matriz N x N, cross-check entre los dos algoritmos).
- Implementacion de `MatrixRenderer.java`.
- Creacion de `Mission3Test.java` (26 pruebas cubriendo las tres ramas de la precedencia).
- Diagnostico de un entorno sin JDK 17 ni Maven en el PATH, y verificacion de que `mvn -q clean verify` pasa completo (la parte de Tomas y la de sus companeros).
- Creacion de la rama `feat/tomas-m3-floyd-bellman-churun`, commits separados por responsabilidad, push y preparacion del Pull Request #1.
- Explicacion linea por linea del propio codigo para preparar la defensa individual.
- Revision de las ramas/PRs de Samuel (`feat/samuel-m1`, `feat/samuel-m4`, `feat/samuel-grid-renderer`) comparando el codigo real contra el enunciado y la guia, no solo confiando en la descripcion del PR.
- Consolidacion de este archivo `AI_USAGE.md` con el aporte de cada integrante.

El flujo de trabajo fue el mismo que el de Samuel, con un paso extra de lectura de documentos y verificacion matematica manual del algoritmo antes de escribir los tests:

1. Leer el enunciado y la guia completos antes de tocar codigo.
2. Ubicar exactamente que archivos son responsabilidad de Tomas segun la tabla de propiedad (seccion 5.4 de la guia).
3. Trazar a mano el algoritmo sobre los samples del enunciado antes de escribirlo (por ejemplo, verificar manualmente que el caso 2 del sample de la Mision 3 detecta el ciclo 1<->2 con ganancia neta +20).
4. Implementar.
5. Compilar y correr los tests con Maven.
6. Comparar la salida contra los samples oficiales caracter por caracter.
7. Confirmar antes de integrar o hacer commit.

---

## Prompts decisivos

### 1. Análisis completo del trabajo antes de programar

**Prompt:**

> "Quiero que la analices muy bien, que entiendas muy bien la forma de trabajar y los requisitos con los que debe cumplir la entrega."

**Por qué se necesitó:**

El trabajo tenía muchos requisitos simultáneos y era necesario convertirlos en tareas concretas antes de comenzar a programar.

El análisis permitió identificar aspectos importantes como:

- DFS no recursivo.
- Orden obligatorio del DFS: arriba, abajo, izquierda, derecha.
- Límites máximos de entrada.
- Límites obligatorios de visualización.
- Uso de `long` para costos acumulados.
- Kruskal apoyado en Union-Find.
- Path compression y union by size.
- Separación entre algoritmos y JavaFX.
- Uso de samples oficiales como tests.

**Qué se hizo con la respuesta:**

Se utilizó como plan inicial, pero posteriormente cada punto importante se volvió a verificar contra el PDF original del profesor.

### Prompts decisivos de Tomas

#### 1. Punto de partida: leer ambos documentos y ubicar la propia parte

**Prompt:**

> "Debo hacer mi parte del parcial de lenguajes y compiladores. Te adjunto dos documentos, uno en PDF que envia el profe, y otro en Word que es como un documento maestro para hacer el trabajo, en el repo actualmente esta la parte de Higuita, revisa bien, estructura, etc, y empezamos con mi parte, debo hacerla en otra rama para luego unirla al main (revisa lo que dice el documento acerca de git)."

**Por qué se necesitó:**

Sin leer los dos documentos completos (no solo hojearlos) era imposible saber que la Mision 3 era la parte de Tomas, cuales archivos exactos le correspondian (tabla 5.4 de la guia), ni que el flujo de Git esperado era rama por tarea + Pull Request, nunca commit directo a `main`.

**Qué se hizo con la respuesta:**

Se extrajo el texto completo del PDF y del Word (el PDF requirio instalar `pdfplumber`, el Word `python-docx`), se identifico la Mision 3 como la responsabilidad de Tomas, se leyeron los contratos ya existentes en el repositorio (`Tokenizer`, `Mission`, `CaseResult`, `MissionResult`, `GraphDrawing`, `MatrixDrawing`) para no reinventarlos, y se creo la rama `feat/tomas-m3-floyd-bellman-churun` antes de escribir codigo.

#### 2. Preparación de la defensa: entender a fondo, no solo tener el código funcionando

**Prompt:**

> "Ahora, ya con mi parte lista vamos a profundizar en el proyecto en general para tener claridad completa."

**Por qué se necesitó:**

El enunciado es explicito en que la nota individual depende de poder explicar el codigo en la defensa oral (factor de 0.0 a 1.0), no solo de que el codigo funcione. Hacia falta una explicacion que conectara las reglas generales del enunciado con decisiones concretas de implementacion (por que dos algoritmos, por que los centinelas, por que la pasada de marcado va aparte).

**Qué se hizo con la respuesta:**

Se genero una explicacion estructurada en niveles (el parcial en general, la arquitectura del repo, la Mision 3 en profundidad archivo por archivo, preguntas probables de defensa con respuesta corta), en vez de simplemente reafirmar que el codigo estaba bien.

#### 3. Verificación cruzada del trabajo de un compañero

**Prompt:**

> "¿Tienes acceso al repositorio y a los pull requests que hay? Debemos verificar que lo que hizo Samuel esta bien, esta en 3 pull requests."

**Por qué se necesitó:**

El enunciado exige que cada integrante pueda explicar y modificar cualquier parte del proyecto, no solo la propia, y la guia advierte que "aprobar sin correr nada no sirve de nada". Como no habia `gh` (GitHub CLI) instalado ni sesion de navegador autenticada, no se podia usar la interfaz de Pull Requests directamente.

**Qué se hizo con la respuesta:**

Se listaron las ramas remotas de Samuel con `git fetch` / `git branch -r`, se comparo cada una contra `main` con `git diff --stat`, y se leyo el codigo real (`Bfs.java`, `Dfs.java`, `DisjointSet.java`, `Kruskal.java`, `Mission1Minefield.java`, `Mission4Network.java`) verificando puntos especificos del enunciado: indexacion plana `row * cols + col`, orden de vecinos del DFS (arriba, abajo, izquierda, derecha), compresion de caminos y union por tamano en el union-find, y la conversion de nodos 1..N a 0..N-1 que solo aplica en la Mision 4.

---

## Casos en que la salida generada estuvo mal o fue subóptima

### 1. Se asumió inicialmente que Maven estaba disponible mediante el comando `mvn`

**Problema:**

Inicialmente se indicó ejecutar:

```text
mvn -q clean verify
```

PowerShell respondió que `mvn` no era un comando reconocido porque Maven no estaba instalado globalmente en el PATH de Windows.

**Cómo se detectó:**

La terminal mostró un `CommandNotFoundException` al intentar ejecutar `mvn`.

**Cómo se corrigió:**

Se revisó la configuración de Maven dentro de IntelliJ y se encontró la instalación Bundled Maven del IDE. Después se ejecutó directamente:

```text
C:\Program Files\JetBrains\IntelliJ IDEA 2025.1.3\plugins\maven\lib\maven3\bin\mvn.cmd
```

Con esta corrección Maven ejecutó la compilación y las pruebas correctamente y produjo `BUILD SUCCESS`.

**Aprendizaje obtenido:**

Una instrucción generada por IA puede ser conceptualmente correcta pero no necesariamente compatible con el entorno local donde se ejecutará.

### 2. El repositorio oficial se clonó inicialmente dentro del proyecto provisional

**Problema:**

El primer `git clone` se ejecutó cuando la terminal seguía ubicada dentro de la carpeta del proyecto temporal `ParcialCompi`.

Esto produjo una estructura equivalente a:

```text
ParcialCompi/
    feline-graph-chronicles/
```

lo que dejaba un repositorio dentro de otro proyecto y podía generar confusión con IntelliJ, ramas y commits.

**Cómo se detectó:**

Se revisó la ruta mostrada por PowerShell y se observó:

```text
C:\Users\samue\IdeaProjects\ParcialCompi\feline-graph-chronicles
```

**Cómo se corrigió:**

El clon recién creado se eliminó y se volvió a clonar desde:

```text
C:\Users\samue\IdeaProjects
```

quedando finalmente:

```text
C:\Users\samue\IdeaProjects\feline-graph-chronicles
```

Después se verificó el repositorio con `git status` antes de seguir trabajando.

### 3. Trabajar inicialmente sin el repositorio real hacía subóptimo asumir contratos

**Problema:**

Al comienzo Samuel todavía no tenía acceso al repositorio oficial. Se creó un proyecto temporal para adelantar BFS, DFS, Disjoint Set y Kruskal.

Esto era útil para los algoritmos independientes, pero crear las clases completas de misión en ese momento habría requerido inventar o asumir interfaces pertenecientes al trabajo de otros integrantes.

**Cómo se detectó:**

La guía indicaba que clases como `Tokenizer`, `Mission`, `MissionResult`, `CaseResult`, `GridDrawing` y `GraphDrawing` tenían contratos compartidos que debían respetarse.

**Cómo se corrigió:**

Se decidió no crear versiones alternativas de esos contratos.

Cuando el repositorio estuvo disponible:

1. Se clonó el proyecto oficial.
2. Se revisaron las clases reales.
3. Se conservaron los algoritmos independientes ya probados.
4. Se implementaron las clases de misión directamente contra los contratos reales.

Esto evitó mantener dos arquitecturas incompatibles.

### Casos detectados por Tomas

#### 4. JDK 17 esperado por el pom, pero Tomas ya tenía instalado OpenJDK 25

**Problema:**

El `pom.xml` fija `maven.compiler.release=17`, pero Tomas no tenía exactamente esa versión instalada, sino un OpenJDK 25 (bajo `~/.jdks`, instalado por el propio IntelliJ). Tampoco había `mvn` accesible desde PowerShell ni Bash. No estaba claro de entrada si ese JDK más nuevo serviría para un proyecto que pide "JDK 17 o superior".

**Cómo se detectó:**

`mvn -q compile` devolvió "El término 'mvn' no se reconoce..." tanto en PowerShell como en Bash.

**Cómo se corrigió:**

Se localizó el Maven empaquetado con IntelliJ (`...plugins\maven\lib\maven3\bin\mvn`) y se apuntó `JAVA_HOME` al OpenJDK 25 que Tomas ya tenía instalado. Como `maven.compiler.release=17` lo sigue respetando un compilador más nuevo, ese JDK 25 compiló y corrió todos los tests sin problema, cumpliendo "JDK 17 o superior" tal como pide el enunciado, así que no hizo falta instalar nada adicional.

**Aprendizaje obtenido:**

"JDK 17 o superior" no significa tener instalada exactamente la versión 17; un JDK más nuevo con `--release 17` produce bytecode compatible, y conviene verificarlo con una compilación real antes de asumir que hace falta instalar algo.

---

## Qué aprendió cada integrante

### Samuel

Durante este proyecto aprendí y reforcé varios conceptos:

- Por qué BFS garantiza un camino mínimo en grafos no ponderados.
- Por qué DFS puede producir un camino válido pero no necesariamente óptimo.
- Por qué un DFS sobre una grilla de hasta un millón de celdas debe ser iterativo para evitar `StackOverflowError`.
- Cómo el funcionamiento LIFO de una pila obliga a insertar vecinos en orden inverso para obtener un orden de exploración específico.
- Cómo el momento exacto en que un nodo se marca como visitado puede modificar el árbol generado por DFS.
- Cómo representar una grilla mediante un arreglo plano usando `row * cols + col`.
- Cómo funciona Disjoint Set o Union-Find.
- Qué hacen path compression y union by size y cómo mejoran el costo amortizado.
- Cómo Kruskal utiliza Union-Find para construir un Minimum Spanning Tree.
- Por qué los costos acumulados del proyecto deben manejarse con `long`.
- Cómo separar un núcleo algorítmico de la GUI para permitir pruebas automatizadas independientes de JavaFX.
- Cómo utilizar Maven para validar un proyecto completo.
- Cómo trabajar con ramas y Pull Requests en un proyecto colaborativo.
- Cómo comprobar código generado o sugerido por IA contra requisitos, tests y resultados oficiales en lugar de asumir que es correcto.

### Tomas

Durante este proyecto aprendí y reforcé varios conceptos:

- Por qué Floyd-Warshall y Bellman-Ford, adaptados a maximización, invierten cada comparación (`>` en vez de `<`) y cambian el centinela de "sin ruta" de `+∞` a `Long.MIN_VALUE`.
- Por qué un ciclo con ganancia neta positiva alcanzable desde el origen y que puede llegar al destino hace que la respuesta sea "infinita", y por qué eso no es lo mismo que decir que todo el grafo es no acotado.
- Por qué la pasada de marcado de "no acotado" de Floyd-Warshall se calcula sobre una copia booleana antes de escribir el centinela `UNBOUNDED` en la matriz real, y qué pasaría si se escribiera directamente mientras se recorre.
- La técnica estándar para extraer un ciclo desde el árbol de padres de Bellman-Ford: caminar N pasos hacia atrás desde un nodo que todavía mejora garantiza caer dentro del ciclo.
- Por qué el enunciado pide correr los dos algoritmos en cada caso y compararlos entre sí (cross-check), y cómo exponer esa comparación como una función pura y estática para poder probarla de forma aislada, forzando una discrepancia a mano.
- Que revisar el Pull Request de un compañero en serio implica leer el código real y compararlo contra el enunciado línea por línea, no solo leer la descripción del PR ni confiar en que "ya pasó los tests".
- Que un JDK más nuevo que el mínimo exigido no es un problema si el `pom.xml` fija `maven.compiler.release`, y cómo verificar eso con una compilación real en vez de asumirlo.

### Sebastian

**PENDIENTE:** Sebastian debe documentar qué aprendió durante el proyecto y que no sabía previamente.

---

## Verificación del trabajo apoyado por IA

Para la parte desarrollada por Samuel se realizaron verificaciones concretas antes de integrarla:

- Se utilizaron los samples oficiales como resultados esperados.
- La Misión 1 produjo exactamente `Case #1: BFS 18 DFS 32`.
- La Misión 4 produjo exactamente `Case #1: 55`.
- Se probaron inputs inválidos y casos borde.
- Se probó una grilla de hasta `1000 x 1000` para validar el DFS iterativo.
- Se comprobaron situaciones con lazos y cables repetidos en Kruskal.
- Se comprobó el uso de `long` en costos acumulados.
- Se ejecutaron los tests mediante Maven.
- Las ramas de Samuel se integraron localmente y se volvió a ejecutar la batería de pruebas antes de crear los Pull Requests.

### Verificación realizada por Tomas

Para la parte desarrollada por Tomas (Misión 3) se realizaron las siguientes verificaciones antes de integrar:

- El sample oficial produjo exactamente `Case #1: 110`, `Case #2: Infinite churun!`, `Case #3: -65`.
- Se probó cada rama de la precedencia por separado: inalcanzable, ciclo positivo que sí llega al destino, ciclo positivo que NO llega al destino (el caso que la guía marca como "el que más se rompe"), y un máximo negativo.
- Se verificó a mano, sobre el caso 2 del sample, que el ciclo detectado por Bellman-Ford (nodos 1 y 2, ganancia neta +20 por vuelta) coincide con el nodo `k` que Floyd-Warshall usa para marcar `(0,3)` como no acotado.
- Se probó que el aviso de discrepancia entre Floyd-Warshall y Bellman-Ford se dispara cuando se le pasan categorías o valores distintos a mano, y que no se dispara con el sample real (los dos algoritmos coinciden).
- Se comprobó que la matriz N x N siempre está presente (nunca se omite, porque N nunca supera 100) y que el dibujo del grafo sí se omite por encima de 60 nodos, con un caso en 60 (se dibuja) y otro en 61 (no se dibuja).
- Se ejecutó `mvn -q clean verify` sobre el proyecto completo (no solo la Misión 3) para confirmar que no se rompió nada de lo ya integrado por Sebastian y Samuel.

El uso de IA se tomó como una herramienta de apoyo para acelerar el análisis, generar propuestas y detectar problemas. La salida generada se consideró una propuesta que debía verificarse antes de formar parte del proyecto.

---


