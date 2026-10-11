package cr.ac.ucenfotec.pilas.analizador;

/**Excepción verificada que indica que una expresión aritmética no es válida: caracteres no reconocidos, paréntesis desbalanceados, operadores sin
operandos, variables sin definir, división entre cero, etc */

public class ExpresionInvalidaException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Valor usado cuando el error no corresponde a una posición concreta */
    public static final int SIN_POSICION = -1;

    private final int posicion;

    /** Sin posición */
    public ExpresionInvalidaException(String mensaje) {
        this(mensaje, SIN_POSICION);
    }

     /** Posición */
    public ExpresionInvalidaException(String mensaje, int posicion) {
        super(mensaje);
        this.posicion = posicion;
    }

    public int getPosicion() {
        return posicion;
    }

    public boolean tienePosicion() {
        return posicion >= 0;
    }
}
