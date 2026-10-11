package cr.ac.ucenfotec.pilas.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**Representa un token (unidad léxica mínima) de una expresión aritmética */
public final class Token {

    /** Símbolo interno con el que se representa el menos unario (negación) */
    public static final String MENOS_UNARIO = "~";

    /** Operadores binarios soportados. */
    private static final String OPERADORES_BINARIOS = "+-*/%^";

    /** Símbolos de apertura y su cierre correspondiente en la misma posición */
    private static final String APERTURAS = "([{";
    private static final String CIERRES = ")]}";

    /** Posición usada para tokens generados internamente (no provienen del texto original) */
    public static final int SIN_POSICION = -1;

    private final TipoToken tipo;
    private final String lexema;
    private final double valor;
    private final int posicion;

    /** Constructor privado: se obliga a usar los métodos de fábrica */
    private Token(TipoToken tipo, String lexema, double valor, int posicion) {
        this.tipo = Objects.requireNonNull(tipo, "El tipo del token no puede ser nulo.");
        this.lexema = Objects.requireNonNull(lexema, "El lexema del token no puede ser nulo.");
        this.valor = valor;
        this.posicion = posicion;
    }

    // Métodos de fábrica
  
    /**Crea un token de número literal leído desde la expresión */
    public static Token numero(double valor, String lexema, int posicion) {
        return new Token(TipoToken.NUMERO, lexema, valor, posicion);
    }

    /** Crea un token numérico generado internamente (por ejemplo, un resultado parcial durante la evaluación) */
    public static Token numero(double valor) {
        return new Token(TipoToken.NUMERO, formatear(valor), valor, SIN_POSICION);
    }

    /**Crea un token de variable numérica*/
    public static Token variable(String nombre, int posicion) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la variable no puede estar vacío.");
        }
        return new Token(TipoToken.VARIABLE, nombre, Double.NaN, posicion);
    }

    /**Crea un token de operador*/
    public static Token operador(String simbolo, int posicion) {
        if (!esSimboloOperador(simbolo)) {
            throw new IllegalArgumentException("Operador no soportado: " + simbolo);
        }
        return new Token(TipoToken.OPERADOR, simbolo, Double.NaN, posicion);
    }

    /** Crea un token de paréntesis de apertura*/
    public static Token parentesisIzq(char simbolo, int posicion) {
        if (APERTURAS.indexOf(simbolo) < 0) {
            throw new IllegalArgumentException("Símbolo de apertura no válido: " + simbolo);
        }
        return new Token(TipoToken.PARENTESIS_IZQ, String.valueOf(simbolo), Double.NaN, posicion);
    }

    /** Crea un token de paréntesis de cierre */
    public static Token parentesisDer(char simbolo, int posicion) {
        if (CIERRES.indexOf(simbolo) < 0) {
            throw new IllegalArgumentException("Símbolo de cierre no válido: " + simbolo);
        }
        return new Token(TipoToken.PARENTESIS_DER, String.valueOf(simbolo), Double.NaN, posicion);
    }

    // Utilidades estáticas

    public static boolean esSimboloOperador(String simbolo) {
        return simbolo != null
                && (simbolo.equals(MENOS_UNARIO)
                || (simbolo.length() == 1 && OPERADORES_BINARIOS.contains(simbolo)));
    }

    public static boolean esApertura(char c) {
        return APERTURAS.indexOf(c) >= 0;
    }

    public static boolean esCierre(char c) {
        return CIERRES.indexOf(c) >= 0;
    }

    public static String formatear(double valor) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            return String.valueOf(valor);
        }
        if (valor == Math.rint(valor) && Math.abs(valor) < 1e15) {
            return String.valueOf((long) valor);
        }
        return BigDecimal.valueOf(valor)
                .setScale(8, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    // Consultas sobre el token

    /** Si es número literal o variable (puede ser operando) */
    public boolean esOperando() {
        return tipo == TipoToken.NUMERO || tipo == TipoToken.VARIABLE;
    }

    /** Si es un operador. */
    public boolean esOperador() {
        return tipo == TipoToken.OPERADOR;
    }

    /** Si es un paréntesis de apertura */
    public boolean esParentesisIzq() {
        return tipo == TipoToken.PARENTESIS_IZQ;
    }

    /** Si es un paréntesis de cierre */
    public boolean esParentesisDer() {
        return tipo == TipoToken.PARENTESIS_DER;
    }

    /** Si es el operador menos unario (requiere un solo operando) */
    public boolean esUnario() {
        return esOperador() && lexema.equals(MENOS_UNARIO);
    }

    /** Precedencia del operador (mayor número = se evalúa primero) */
    public int getPrecedencia() {
        if (!esOperador()) {
            return 0;
        }
        return switch (lexema) {
            case "^" -> 4;
            case MENOS_UNARIO -> 3;
            case "*", "/", "%" -> 2;
            case "+", "-" -> 1;
            default -> 0;
        };
    }

    /** La potencia y el menos unario asocian por la derecha */
    public boolean esAsociativoDerecha() {
        return esOperador() && (lexema.equals("^") || lexema.equals(MENOS_UNARIO));
    }

    /** Indica si este token (de apertura) se cierra correctamente con el token recibido */
    public boolean cierraCon(Token cierre) {
        if (!esParentesisIzq() || cierre == null || !cierre.esParentesisDer()) {
            return false;
        }
        return APERTURAS.indexOf(lexema.charAt(0)) == CIERRES.indexOf(cierre.lexema.charAt(0));
    }

    // Getters

    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public double getValor() {
        if (tipo != TipoToken.NUMERO) {
            throw new IllegalStateException("El token '" + lexema + "' no es un número literal.");
        }
        return valor;
    }

    public int getPosicion() {
        return posicion;
    }

    @Override
    public String toString() {
        return lexema;
    }
}
