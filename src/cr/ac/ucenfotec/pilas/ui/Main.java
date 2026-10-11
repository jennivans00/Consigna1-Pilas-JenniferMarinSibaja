package cr.ac.ucenfotec.pilas.ui;

import cr.ac.ucenfotec.pilas.analizador.AnalizadorExpresiones;
import cr.ac.ucenfotec.pilas.analizador.ExpresionInvalidaException;
import cr.ac.ucenfotec.pilas.analizador.Traza;
import cr.ac.ucenfotec.pilas.estructura.Pila;
import cr.ac.ucenfotec.pilas.estructura.PilaVaciaException;
import cr.ac.ucenfotec.pilas.modelo.Token;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**Punto de entrada de la aplicación*/
public class Main {

    /** Lector único de la entrada estándar (no se cierra para no cerrar System.in) */
    private static final Scanner ENTRADA = new Scanner(System.in);

    /** Pila de tokens con la que el usuario practica las operaciones fundamentales */
    private static final Pila PILA_MANUAL = new Pila();

    /** Analizador que contiene la lógica de pilas y la tabla de variables */
    private static final AnalizadorExpresiones ANALIZADOR = new AnalizadorExpresiones();

    /** Expresión infija con la que se trabaja actualmente (vacía si no hay) */
    private static String expresionActual = "";

    /** Indica que la entrada estándar terminó (por ejemplo, al redirigir un archivo) */
    private static boolean finDeEntrada = false;

    /** Línea separadora reutilizada en la interfaz */
    private static final String LINEA = "=".repeat(66);

    /** Constructor privado: la clase solo contiene métodos estáticos */
    private Main() {
    }

    // Rutina principal

    public static void main(String[] args) {
        mostrarBienvenida();
        menu();
        System.out.println("\nGracias por utilizar el analizador de expresiones. ¡Hasta pronto!");
    }

    /** Ciclo del menú principal: muestra las opciones, lee la elección del usuario y la ejecuta, hasta que se elija la opción 0 (Salir) */
    public static void menu() {
        int opcion;
        do {
            mostrarOpciones();
            opcion = leerEntero("  Seleccione una opción: ");
            System.out.println();
            ejecutarOpcion(opcion);
            if (opcion != 0) {
                pausar();
            }
        } while (opcion != 0);
    }

    /** Despacha la opción elegida al método correspondiente */
    private static void ejecutarOpcion(int opcion) {
        switch (opcion) {
            // --- Operaciones fundamentales de la pila ---
            case 1 -> apilarToken();
            case 2 -> desapilarToken();
            case 3 -> consultarCima();
            case 4 -> mostrarPila();
            case 5 -> vaciarPila();
            // --- Análisis de expresiones ---
            case 6 -> ingresarExpresion();
            case 7 -> definirVariable();
            case 8 -> administrarVariables();
            case 9 -> mostrarTokens();
            case 10 -> validarParentesis();
            case 11 -> convertirAPostfija();
            case 12 -> evaluarExpresion();
            case 13 -> analisisCompleto();
            // --- Otros ---
            case 14 -> cargarEjemplo();
            case 15 -> mostrarAyuda();
            case 0 -> { /* salir: el ciclo del menú termina */ }
            default -> System.out.println("  [!] Opción no válida. Digite un número entre 0 y 15.");
        }
    }

    // Presentación del menú

    private static void mostrarBienvenida() {
        System.out.println(LINEA);
        System.out.println("     UNIVERSIDAD CENFOTEC  -  SOFT-10 ESTRUCTURAS DE DATOS");
        System.out.println(LINEA);
        System.out.println("  Tokens soportados: números (3, 2.5), variables (x, tasa_1),");
        System.out.println("  operadores (+ - * / % ^ y menos unario) y paréntesis ( ) [ ] { }.");
        System.out.println("  Sugerencia: elija la opción 14 para cargar un ejemplo listo.");
    }

    private static void mostrarOpciones() {
        System.out.println("\n" + LINEA);
        System.out.println("  MENÚ PRINCIPAL");
        System.out.println("  Expresión actual : " + (expresionActual.isEmpty() ? "(ninguna)" : expresionActual));
        System.out.println("  Variables        : " + describirVariables());
        System.out.println("  Pila manual      : " + PILA_MANUAL + "  (tamaño " + PILA_MANUAL.getTamanio() + ")");
        System.out.println(LINEA);
        System.out.println("  -- Operaciones fundamentales de la pila --");
        System.out.println("   1. Apilar un token (push)");
        System.out.println("   2. Desapilar un token (pop)");
        System.out.println("   3. Consultar la cima (peek)");
        System.out.println("   4. Mostrar la pila");
        System.out.println("   5. Vaciar la pila");
        System.out.println("  -- Análisis de expresiones aritméticas --");
        System.out.println("   6. Ingresar o cambiar la expresión");
        System.out.println("   7. Definir / modificar una variable");
        System.out.println("   8. Ver / eliminar variables");
        System.out.println("   9. Ver los tokens de la expresión");
        System.out.println("  10. Validar paréntesis (paso a paso)");
        System.out.println("  11. Convertir a notación postfija (paso a paso)");
        System.out.println("  12. Evaluar la expresión (paso a paso)");
        System.out.println("  13. Análisis completo");
        System.out.println("  -- Otros --");
        System.out.println("  14. Cargar expresión de ejemplo");
        System.out.println("  15. Ayuda");
        System.out.println("   0. Salir");
        System.out.println(LINEA);
    }

    // Sección 1: operaciones fundamentales de la pila

    /** Opción 1: crea un token a partir del texto del usuario y lo apila */
    private static void apilarToken() {
        System.out.println("  APILAR (push)");
        System.out.println("  Escriba UN token: número (7, 2.5), variable (x), operador (+ - * / % ^ ~)");
        System.out.println("  o paréntesis ( ) [ ] { }. ENTER vacío para cancelar.");
        String texto = leerLinea("  Token: ");
        if (texto.isBlank()) {
            System.out.println("  Operación cancelada.");
            return;
        }
        try {
            Token token = ANALIZADOR.crearToken(texto);
            PILA_MANUAL.apilar(token);
            System.out.println("  [OK] Se apiló '" + token + "' (" + token.getTipo().getDescripcion() + ").\n");
            System.out.print(PILA_MANUAL.dibujar());
        } catch (ExpresionInvalidaException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    /** Opción 2: retira el token de la cima */
    private static void desapilarToken() {
        System.out.println("  DESAPILAR (pop)");
        try {
            Token token = PILA_MANUAL.desapilar();
            System.out.println("  [OK] Se desapiló '" + token + "' (" + token.getTipo().getDescripcion() + ").\n");
            System.out.print(PILA_MANUAL.dibujar());
        } catch (PilaVaciaException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    /** Opción 3: muestra la cima sin retirarla */
    private static void consultarCima() {
        System.out.println("  CONSULTAR CIMA (peek)");
        try {
            Token cima = PILA_MANUAL.verCima();
            System.out.println("  La cima es '" + cima + "' (" + cima.getTipo().getDescripcion()
                    + "). La pila no se modificó.");
        } catch (PilaVaciaException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    /** Opción 4: dibuja la pila con su tamaño */
    private static void mostrarPila() {
        System.out.println("  CONTENIDO DE LA PILA  (tamaño: " + PILA_MANUAL.getTamanio()
                + ", vacía: " + (PILA_MANUAL.estaVacia() ? "sí" : "no") + ")\n");
        System.out.print(PILA_MANUAL.dibujar());
    }

    /** Opción 5: vacía la pila previa confirmación */
    private static void vaciarPila() {
        if (PILA_MANUAL.estaVacia()) {
            System.out.println("  La pila ya está vacía.");
            return;
        }
        if (confirmar("  ¿Desea eliminar los " + PILA_MANUAL.getTamanio() + " elementos de la pila? (s/n): ")) {
            PILA_MANUAL.vaciar();
            System.out.println("  [OK] La pila quedó vacía.");
        } else {
            System.out.println("  Operación cancelada.");
        }
    }

    // Sección 2: análisis de expresiones

    /** Opción 6: lee una expresión y solo la acepta si es léxica y sintácticamente válida */
    private static void ingresarExpresion() {
        System.out.println("  INGRESAR EXPRESIÓN (notación infija)");
        System.out.println("  Ejemplos:  (a + 3) * b - 10 / 2^2      -x + [4 * (y - 1)]");
        String texto = leerLinea("  Expresión: ").trim();
        if (texto.isEmpty()) {
            System.out.println("  Operación cancelada; se conserva la expresión anterior.");
            return;
        }
        try {
            List<Token> tokens = ANALIZADOR.tokenizar(texto);
            ANALIZADOR.validarParentesis(tokens, null);
            ANALIZADOR.validarSintaxis(tokens);
            expresionActual = texto;
            System.out.println("  [OK] Expresión válida con " + tokens.size() + " tokens.");
            Set<String> faltantes = ANALIZADOR.variablesSinDefinir(tokens);
            if (!faltantes.isEmpty()) {
                System.out.println("  Nota: las variables " + faltantes
                        + " aún no tienen valor; se le pedirán al evaluar.");
            }
        } catch (ExpresionInvalidaException e) {
            mostrarError(e, texto);
            System.out.println("  La expresión no se guardó.");
        }
    }

    /** Opción 7: define o actualiza una variable numérica */
    private static void definirVariable() {
        System.out.println("  DEFINIR VARIABLE");
        String nombre = leerLinea("  Nombre (ENTER para cancelar): ").trim();
        if (nombre.isEmpty()) {
            System.out.println("  Operación cancelada.");
            return;
        }
        double valor = leerDecimal("  Valor de " + nombre + ": ");
        try {
            ANALIZADOR.definirVariable(nombre, valor);
            System.out.println("  [OK] " + nombre + " = " + Token.formatear(valor));
        } catch (IllegalArgumentException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    /** Opción 8: lista las variables y permite eliminar una */
    private static void administrarVariables() {
        System.out.println("  VARIABLES DEFINIDAS");
        Map<String, Double> variables = ANALIZADOR.getVariables();
        if (variables.isEmpty()) {
            System.out.println("  No hay variables definidas. Use la opción 7.");
            return;
        }
        for (Map.Entry<String, Double> v : variables.entrySet()) {
            System.out.printf("    %-15s = %s%n", v.getKey(), Token.formatear(v.getValue()));
        }
        String nombre = leerLinea("\n  Nombre de la variable a eliminar (ENTER para volver): ").trim();
        if (!nombre.isEmpty()) {
            System.out.println(ANALIZADOR.eliminarVariable(nombre)
                    ? "  [OK] Variable '" + nombre + "' eliminada."
                    : "  [!] No existe la variable '" + nombre + "'.");
        }
    }

    /** Opción 9: muestra la tabla de tokens de la expresión actual */
    private static void mostrarTokens() {
        if (!hayExpresion()) {
            return;
        }
        try {
            List<Token> tokens = ANALIZADOR.tokenizar(expresionActual);
            Traza tabla = new Traza("TOKENS DE: " + expresionActual, "Lexema", "Tipo", "Posición");
            for (Token t : tokens) {
                String lexema = t.esUnario() ? "~  (menos unario)" : t.getLexema();
                tabla.registrar(lexema, t.getTipo().getDescripcion(), String.valueOf(t.getPosicion() + 1));
            }
            System.out.print(tabla);
        } catch (ExpresionInvalidaException e) {
            mostrarError(e, expresionActual);
        }
    }

    /** Opción 10: valida los paréntesis mostrando la pila de aperturas */
    private static void validarParentesis() {
        if (!hayExpresion()) {
            return;
        }
        Traza traza = new Traza("VALIDACIÓN DE PARÉNTESIS: " + expresionActual,
                "Token", "Acción", "Pila de aperturas", "Estado");
        try {
            ANALIZADOR.validarParentesis(ANALIZADOR.tokenizar(expresionActual), traza);
            System.out.print(traza);
            System.out.println("  [OK] Los símbolos de agrupación están balanceados y bien anidados.");
        } catch (ExpresionInvalidaException e) {
            System.out.print(traza);
            mostrarError(e, expresionActual);
        }
    }

    /** Opción 11: convierte a postfija mostrando la pila de operadores */
    private static void convertirAPostfija() {
        if (!hayExpresion()) {
            return;
        }
        try {
            List<Token> postfija = convertirConTraza("");
            System.out.println("  Infija  : " + expresionActual);
            System.out.println("  Postfija: " + AnalizadorExpresiones.unir(postfija));
            System.out.println("  (El símbolo ~ representa el menos unario.)");
        } catch (ExpresionInvalidaException e) {
            mostrarError(e, expresionActual);
        }
    }

    /** Opción 12: evalúa mostrando la pila de operandos */
    private static void evaluarExpresion() {
        if (!hayExpresion()) {
            return;
        }
        try {
            List<Token> tokens = prepararTokens();
            List<Token> postfija = ANALIZADOR.infijaAPostfija(tokens, null);
            System.out.println("  Postfija: " + AnalizadorExpresiones.unir(postfija));
            double resultado = evaluarConTraza(postfija, "");
            System.out.println("\n  RESULTADO:  " + expresionActual + "  =  " + Token.formatear(resultado));
        } catch (ExpresionInvalidaException e) {
            mostrarError(e, expresionActual);
        }
    }

    /** Opción 13: ejecuta todas las etapas del análisis, una tras otra */
    private static void analisisCompleto() {
        if (!hayExpresion()) {
            return;
        }
        System.out.println("  ANÁLISIS COMPLETO DE: " + expresionActual);
        try {
            // Etapa 1: análisis léxico
            List<Token> tokens = ANALIZADOR.tokenizar(expresionActual);
            System.out.println("\n  [1/4] Tokens (" + tokens.size() + "): " + AnalizadorExpresiones.unir(tokens));

            // Etapa 2: validación de paréntesis y sintaxis
            Traza trazaParentesis = new Traza("[2/4] VALIDACIÓN DE PARÉNTESIS",
                    "Token", "Acción", "Pila de aperturas", "Estado");
            ANALIZADOR.validarParentesis(tokens, trazaParentesis);
            ANALIZADOR.validarSintaxis(tokens);
            System.out.print(trazaParentesis);
            System.out.println("  Sintaxis correcta.");

            // Etapa 3: conversión a postfija
            List<Token> postfija = convertirConTraza("[3/4] ");
            System.out.println("  Postfija: " + AnalizadorExpresiones.unir(postfija));

            // Etapa 4: evaluación
            solicitarVariablesFaltantes(tokens);
            double resultado = evaluarConTraza(postfija, "[4/4] ");
            System.out.println("\n  RESULTADO:  " + expresionActual + "  =  " + Token.formatear(resultado));
        } catch (ExpresionInvalidaException e) {
            mostrarError(e, expresionActual);
        }
    }

    // Sección 3: otros

    /** Opción 14: carga una expresión y variables de ejemplo */
    private static void cargarEjemplo() {
        expresionActual = "(a + 3) * b - 10 / 2 ^ 2";
        ANALIZADOR.definirVariable("a", 4);
        ANALIZADOR.definirVariable("b", 5);
        System.out.println("  [OK] Ejemplo cargado:");
        System.out.println("       Expresión: " + expresionActual);
        System.out.println("       Variables: a = 4, b = 5   (resultado esperado: 32.5)");
        System.out.println("  Ahora pruebe las opciones 9 a 13.");
    }

    /** Opción 15: explica el funcionamiento del programa */
    private static void mostrarAyuda() {
        System.out.println("  AYUDA");
        System.out.println("  - Una PILA es una estructura LIFO: el último elemento en entrar es el primero");
        System.out.println("    en salir. Solo se puede acceder a la CIMA.");
        System.out.println("  - Opciones 1-5: practique push, pop, peek, mostrar y vaciar con tokens.");
        System.out.println("  - Opciones 6-13: el programa usa pilas para analizar una expresión:");
        System.out.println("      * Validar paréntesis: cada apertura se apila; cada cierre debe coincidir");
        System.out.println("        con la cima. Al final la pila debe quedar vacía.");
        System.out.println("      * Infija -> postfija (algoritmo Shunting-yard de Dijkstra): los operadores");
        System.out.println("        esperan en una pila según su precedencia.");
        System.out.println("      * Evaluar postfija: los operandos se apilan; cada operador desapila sus");
        System.out.println("        operandos y apila el resultado.");
        System.out.println("  - Precedencia (mayor a menor): ^   menos unario   * / %   + -");
        System.out.println("    ^ asocia a la derecha (2^3^2 = 512); el resto a la izquierda.");
        System.out.println("  - Las variables usan letras, dígitos y '_' (no pueden iniciar con dígito).");
        System.out.println("  - Escriba la multiplicación explícitamente: 3*x en lugar de 3x.");
    }

    // Auxiliares del análisis

    /** Tokeniza y valida la expresión actual; pide las variables faltantes */
    private static List<Token> prepararTokens() throws ExpresionInvalidaException {
        List<Token> tokens = ANALIZADOR.tokenizar(expresionActual);
        ANALIZADOR.validarParentesis(tokens, null);
        ANALIZADOR.validarSintaxis(tokens);
        solicitarVariablesFaltantes(tokens);
        return tokens;
    }

    /** Convierte la expresión actual a postfija e imprime la traza (prefijo: etiqueta de etapa, puede ser vacío) */
    private static List<Token> convertirConTraza(String prefijo) throws ExpresionInvalidaException {
        List<Token> tokens = ANALIZADOR.tokenizar(expresionActual);
        ANALIZADOR.validarParentesis(tokens, null);
        ANALIZADOR.validarSintaxis(tokens);
        Traza traza = new Traza(prefijo + "CONVERSIÓN INFIJA -> POSTFIJA (Shunting-yard)",
                "Token", "Acción", "Pila de operadores", "Salida postfija");
        List<Token> postfija = ANALIZADOR.infijaAPostfija(tokens, traza);
        System.out.print(traza);
        return postfija;
    }

    /** Evalúa la expresión postfija e imprime la traza, también si ocurre un error (prefijo: etiqueta de etapa) */
    private static double evaluarConTraza(List<Token> postfija, String prefijo) throws ExpresionInvalidaException {
        Traza traza = new Traza(prefijo + "EVALUACIÓN DE LA EXPRESIÓN POSTFIJA",
                "Token", "Acción", "Pila de operandos", "Operación");
        try {
            return ANALIZADOR.evaluarPostfija(postfija, traza);
        } finally {
            System.out.print(traza);
        }
    }

    /** Pide al usuario el valor de cada variable de la expresión que aún no está definida */
    private static void solicitarVariablesFaltantes(List<Token> tokens) {
        Set<String> faltantes = ANALIZADOR.variablesSinDefinir(tokens);
        if (faltantes.isEmpty()) {
            return;
        }
        System.out.println("\n  La expresión usa variables sin valor. Ingréselos:");
        for (String nombre : faltantes) {
            ANALIZADOR.definirVariable(nombre, leerDecimal("    " + nombre + " = "));
        }
    }

    /** Verifica que haya una expresión cargada; si no, orienta al usuario */
    private static boolean hayExpresion() {
        if (expresionActual.isEmpty()) {
            System.out.println("  [!] Primero ingrese una expresión (opción 6) o cargue el ejemplo (opción 14).");
            return false;
        }
        return true;
    }

    /** Muestra un error de análisis señalando con '^' la posición donde ocurrió */
    private static void mostrarError(ExpresionInvalidaException e, String expresion) {
        System.out.println("\n  [ERROR] " + e.getMessage());
        if (e.tienePosicion() && e.getPosicion() < expresion.length()) {
            System.out.println("          " + expresion);
            System.out.println("          " + " ".repeat(e.getPosicion()) + "^  (posición " + (e.getPosicion() + 1) + ")");
        }
    }

    /** Resume las variables definidas en una sola línea */
    private static String describirVariables() {
        Map<String, Double> variables = ANALIZADOR.getVariables();
        if (variables.isEmpty()) {
            return "(ninguna)";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> v : variables.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(v.getKey()).append(" = ").append(Token.formatear(v.getValue()));
        }
        return sb.toString();
    }

    // Lectura segura de datos

    /**Lee una línea; si la entrada terminó, devuelve cadena vacía y marca el fin */
    private static String leerLinea(String mensaje) {
        System.out.print(mensaje);
        if (finDeEntrada || !ENTRADA.hasNextLine()) {
            finDeEntrada = true;
            System.out.println();
            return "";
        }
        return ENTRADA.nextLine();
    }

    /** Lee un entero, repitiendo hasta que el dato sea válido. Si la entrada termina, devuelve 0 (Salir) */
    private static int leerEntero(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje).trim();
            if (finDeEntrada) {
                return 0;
            }
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Debe digitar un número entero.");
            }
        }
    }

    /** Lee un número decimal (acepta coma o punto), repitiendo hasta que sea válido */
    private static double leerDecimal(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje).trim().replace(',', '.');
            if (finDeEntrada) {
                return 0;
            }
            try {
                double valor = Double.parseDouble(texto);
                if (!Double.isNaN(valor) && !Double.isInfinite(valor)) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // se informa abajo
            }
            System.out.println("  [!] Debe digitar un número válido (por ejemplo 4 o 2.5).");
        }
    }

    /** Pide una confirmación s/n */
    private static boolean confirmar(String mensaje) {
        String respuesta = leerLinea(mensaje).trim().toLowerCase();
        return respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí");
    }

    /** Detiene la ejecución hasta que el usuario presione ENTER */
    private static void pausar() {
        leerLinea("\n  Presione ENTER para continuar...");
    }
}
