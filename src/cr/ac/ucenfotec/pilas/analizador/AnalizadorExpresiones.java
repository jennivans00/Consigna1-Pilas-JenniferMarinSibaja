package cr.ac.ucenfotec.pilas.analizador;

import cr.ac.ucenfotec.pilas.estructura.Pila;
import cr.ac.ucenfotec.pilas.modelo.TipoToken;
import cr.ac.ucenfotec.pilas.modelo.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

// Valida paréntesis, convierte a postfija y evalúa usando pilas

public class AnalizadorExpresiones {

    /** Patrón de un nombre de variable válido */
    private static final Pattern NOMBRE_VARIABLE = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private final Tokenizador tokenizador = new Tokenizador();

    /** Tabla de símbolos: nombre de variable -> valor. Ordenada alfabéticamente */
    private final Map<String, Double> variables = new TreeMap<>();

// 1. Análisis léxico

    /**Divide la expresión en tokens.*/
    public List<Token> tokenizar(String expresion) throws ExpresionInvalidaException {
        return tokenizador.tokenizar(expresion);
    }

    /**Crea un único token a partir de texto (para apilar manualmente desde el menú) */
    public Token crearToken(String texto) throws ExpresionInvalidaException {
        return tokenizador.crearTokenIndividual(texto);
    }

// 2. Validación de paréntesis con pila

    /** Verifica que los símbolos de agrupación estén balanceados y correctamente anidados. Cada apertura se apila; cada cierre debe corresponder a la
    apertura que está en la cima (la más reciente sin cerrar). Al final la pila debe quedar vacía */

    public void validarParentesis(List<Token> tokens, Traza traza) throws ExpresionInvalidaException {
        Pila pila = new Pila();

        for (Token token : tokens) {
            if (token.esParentesisIzq()) {
                pila.apilar(token);
                registrar(traza, token.getLexema(), "Apertura: apilar", pila.toString(), "");

            } else if (token.esParentesisDer()) {
                if (pila.estaVacia()) {
                    registrar(traza, token.getLexema(), "Cierre con pila vacía", pila.toString(), "ERROR");
                    throw new ExpresionInvalidaException(
                            "El símbolo '" + token + "' cierra un grupo que nunca se abrió.",
                            token.getPosicion());
                }
                Token apertura = pila.desapilar();
                if (!apertura.cierraCon(token)) {
                    registrar(traza, token.getLexema(),
                            "Desapilar '" + apertura + "': no coincide", pila.toString(), "ERROR");
                    throw new ExpresionInvalidaException(
                            "El símbolo '" + apertura + "' (posición " + (apertura.getPosicion() + 1)
                                    + ") no puede cerrarse con '" + token + "'.",
                            token.getPosicion());
                }
                registrar(traza, token.getLexema(),
                        "Cierre: desapilar '" + apertura + "' (coincide)", pila.toString(), "OK");
            }
        }

        if (!pila.estaVacia()) {
            Token sinCerrar = pila.verCima();
            registrar(traza, "fin", "Quedan aperturas en la pila", pila.toString(), "ERROR");
            throw new ExpresionInvalidaException(
                    "Falta cerrar " + pila.getTamanio() + " símbolo(s) de agrupación; "
                            + "el más reciente es '" + sinCerrar + "'.",
                    sinCerrar.getPosicion());
        }
        registrar(traza, "fin", "Pila vacía: expresión balanceada", pila.toString(), "OK");
    }

// 3. Validación sintáctica (orden de operandos y operadores)
 
    /** Verifica que los tokens aparezcan en un orden válido usando un autómata de dos estados: "se espera un operando" y "se espera un operador" */
    public void validarSintaxis(List<Token> tokens) throws ExpresionInvalidaException {
        boolean esperaOperando = true;
        Token anterior = null;

        for (Token token : tokens) {
            if (esperaOperando) {
                if (token.esOperando()) {
                    esperaOperando = false;
                } else if (!token.esParentesisIzq() && !token.esUnario()) {
                    throw new ExpresionInvalidaException(
                            "Se esperaba un número, variable o '(' pero se encontró '" + token + "'"
                                    + (anterior == null ? " al inicio." : " después de '" + anterior + "'."),
                            token.getPosicion());
                }
            } else {
                if (token.esOperador() && !token.esUnario()) {
                    esperaOperando = true;
                } else if (!token.esParentesisDer()) {
                    throw new ExpresionInvalidaException(
                            "Falta un operador entre '" + anterior + "' y '" + token + "'.",
                            token.getPosicion());
                }
            }
            anterior = token;
        }

        if (esperaOperando) {
            throw new ExpresionInvalidaException(
                    "La expresión termina de forma incompleta después de '" + anterior + "'.",
                    anterior == null ? ExpresionInvalidaException.SIN_POSICION : anterior.getPosicion());
        }
    }

 
// 4. Conversión infija -> postfija (Shunting-yard)

    public List<Token> infijaAPostfija(List<Token> infija, Traza traza) throws ExpresionInvalidaException {
        Pila operadores = new Pila();
        List<Token> salida = new ArrayList<>();

        for (Token token : infija) {
            if (token.esOperando()) {
                salida.add(token);
                registrar(traza, token.getLexema(), "Operando: a la salida", operadores.toString(), unir(salida));

            } else if (token.esParentesisIzq()) {
                operadores.apilar(token);
                registrar(traza, token.getLexema(), "Apertura: apilar", operadores.toString(), unir(salida));

            } else if (token.esParentesisDer()) {
                // Se vacían los operadores del grupo hasta su apertura
                while (!operadores.estaVacia() && !operadores.verCima().esParentesisIzq()) {
                    salida.add(operadores.desapilar());
                }
                if (operadores.estaVacia()) {
                    throw new ExpresionInvalidaException(
                            "El símbolo '" + token + "' no tiene apertura.", token.getPosicion());
                }
                operadores.desapilar(); // se descarta la apertura
                registrar(traza, token.getLexema(), "Cierre: desapilar hasta la apertura",
                        operadores.toString(), unir(salida));

            } else if (token.esUnario()) {
                // Un operador prefijo no tiene operando izquierdo: no desplaza a nadie
                operadores.apilar(token);
                registrar(traza, token.getLexema(), "Menos unario: apilar", operadores.toString(), unir(salida));

            } else { // operador binario
                int desapilados = 0;
                while (!operadores.estaVacia() && debeDesapilarseAntesDe(operadores.verCima(), token)) {
                    salida.add(operadores.desapilar());
                    desapilados++;
                }
                operadores.apilar(token);
                String accion = desapilados == 0
                        ? "Operador: apilar"
                        : "Desapilar " + desapilados + " de mayor/igual prioridad y apilar";
                registrar(traza, token.getLexema(), accion, operadores.toString(), unir(salida));
            }
        }

        // Se vacían los operadores restantes.
        while (!operadores.estaVacia()) {
            Token cima = operadores.desapilar();
            if (cima.esParentesisIzq()) {
                throw new ExpresionInvalidaException(
                        "El símbolo '" + cima + "' no se cerró.", cima.getPosicion());
            }
            salida.add(cima);
            registrar(traza, "fin", "Vaciar pila: '" + cima + "' a la salida", operadores.toString(), unir(salida));
        }
        return salida;
    }

    /** Regla de precedencia del Shunting-yard: el operador de la cima sale antes de apilar el nuevo si tiene mayor precedencia, o igual precedencia y el nuevo
    es asociativo por la izquierda */
    private boolean debeDesapilarseAntesDe(Token cima, Token nuevo) {
        if (!cima.esOperador()) {
            return false; // una apertura funciona como "barrera"
        }
        return cima.getPrecedencia() > nuevo.getPrecedencia()
                || (cima.getPrecedencia() == nuevo.getPrecedencia() && !nuevo.esAsociativoDerecha());
    }

// 5. Evaluación de la expresión postfija

    public double evaluarPostfija(List<Token> postfija, Traza traza) throws ExpresionInvalidaException {
        Pila operandos = new Pila();

        for (Token token : postfija) {
            if (token.esOperando()) {
                double valor = obtenerValor(token);
                operandos.apilar(Token.numero(valor));
                String accion = token.getTipo() == TipoToken.VARIABLE
                        ? "Variable " + token + " = " + Token.formatear(valor) + ": apilar"
                        : "Número: apilar";
                registrar(traza, token.getLexema(), accion, operandos.toString(), "");

            } else if (token.esUnario()) {
                exigirOperandos(operandos, 1, token);
                double a = operandos.desapilar().getValor();
                double resultado = -a;
                operandos.apilar(Token.numero(resultado));
                registrar(traza, token.getLexema(), "Desapilar " + Token.formatear(a) + ", negar y apilar",
                        operandos.toString(), "-(" + Token.formatear(a) + ") = " + Token.formatear(resultado));

            } else if (token.esOperador()) {
                exigirOperandos(operandos, 2, token);
                double b = operandos.desapilar().getValor(); // operando derecho (último en entrar)
                double a = operandos.desapilar().getValor(); // operando izquierdo
                double resultado = aplicar(token, a, b);
                operandos.apilar(Token.numero(resultado));
                registrar(traza, token.getLexema(), "Desapilar 2, operar y apilar", operandos.toString(),
                        Token.formatear(a) + " " + token + " " + Token.formatear(b)
                                + " = " + Token.formatear(resultado));

            } else {
                throw new ExpresionInvalidaException(
                        "Una expresión postfija no puede contener '" + token + "'.", token.getPosicion());
            }
        }

        if (operandos.getTamanio() != 1) {
            throw new ExpresionInvalidaException(
                    "Expresión mal formada: quedaron " + operandos.getTamanio() + " valores en la pila.");
        }
        double resultado = operandos.desapilar().getValor();
        registrar(traza, "fin", "Desapilar el resultado final", operandos.toString(),
                "Resultado = " + Token.formatear(resultado));
        return resultado;
    }

    /** Verifica que la pila tenga suficientes operandos para el operador. */
    private void exigirOperandos(Pila operandos, int requeridos, Token operador)
            throws ExpresionInvalidaException {
        if (operandos.getTamanio() < requeridos) {
            throw new ExpresionInvalidaException(
                    "El operador '" + operador + "' necesita " + requeridos
                            + " operando(s) y solo hay " + operandos.getTamanio() + ".",
                    operador.getPosicion());
        }
    }

    /** Obtiene el valor numérico de un número literal o de una variable definida */
    private double obtenerValor(Token operando) throws ExpresionInvalidaException {
        if (operando.getTipo() == TipoToken.NUMERO) {
            return operando.getValor();
        }
        Double valor = variables.get(operando.getLexema());
        if (valor == null) {
            throw new ExpresionInvalidaException(
                    "La variable '" + operando + "' no tiene un valor definido.", operando.getPosicion());
        }
        return valor;
    }

    /** Aplica un operador binario a dos operandos, validando los casos indefinidos */
    private double aplicar(Token operador, double a, double b) throws ExpresionInvalidaException {
        double resultado = switch (operador.getLexema()) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> {
                if (b == 0) {
                    throw new ExpresionInvalidaException("División entre cero.", operador.getPosicion());
                }
                yield a / b;
            }
            case "%" -> {
                if (b == 0) {
                    throw new ExpresionInvalidaException("Módulo entre cero.", operador.getPosicion());
                }
                yield a % b;
            }
            case "^" -> Math.pow(a, b);
            default -> throw new ExpresionInvalidaException(
                    "Operador desconocido '" + operador + "'.", operador.getPosicion());
        };
        if (Double.isNaN(resultado) || Double.isInfinite(resultado)) {
            throw new ExpresionInvalidaException(
                    "La operación " + Token.formatear(a) + " " + operador + " " + Token.formatear(b)
                            + " no produce un número real finito.",
                    operador.getPosicion());
        }
        return resultado;
    }


 // 6. Atajo: análisis completo sin traza

    /**Ejecuta todo el proceso (tokenizar, validar, convertir y evaluar) sin traza */
    public double evaluar(String expresion) throws ExpresionInvalidaException {
        List<Token> tokens = tokenizar(expresion);
        validarParentesis(tokens, null);
        validarSintaxis(tokens);
        return evaluarPostfija(infijaAPostfija(tokens, null), null);
    }


// 7. Tabla de símbolos (variables numéricas)
    
    /**Define o actualiza el valor de una variable */
    public void definirVariable(String nombre, double valor) {
        if (nombre == null || !NOMBRE_VARIABLE.matcher(nombre).matches()) {
            throw new IllegalArgumentException(
                    "Nombre de variable no válido. Debe iniciar con letra o '_' "
                            + "y contener solo letras, dígitos o '_'.");
        }
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw new IllegalArgumentException("El valor de la variable debe ser un número finito.");
        }
        variables.put(nombre, valor);
    }

    public boolean eliminarVariable(String nombre) {
        return variables.remove(nombre) != null;
    }

    public Map<String, Double> getVariables() {
        return Collections.unmodifiableMap(variables);
    }

    public Set<String> variablesSinDefinir(List<Token> tokens) {
        Set<String> faltantes = new LinkedHashSet<>();
        for (Token t : tokens) {
            if (t.getTipo() == TipoToken.VARIABLE
                    && !variables.containsKey(t.getLexema())) {
                faltantes.add(t.getLexema());
            }
        }
        return faltantes;
    }

// Utilidades

    public static String unir(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Token t : tokens) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(t.getLexema());
        }
        return sb.toString();
    }

    /** Registra un paso solo si se solicitó traza */
    private static void registrar(Traza traza, String... valores) {
        if (traza != null) {
            traza.registrar(valores);
        }
    }
}
