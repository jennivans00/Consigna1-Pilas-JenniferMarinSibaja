package cr.ac.ucenfotec.pilas.modelo;

/** Enumera las categorías léxicas que puede tener un token dentro de una expresión aritmética */
public enum TipoToken {

    /** Número literal entero o decimal */
    NUMERO("Número literal"),

    /** Identificador de una variable numérica */
    VARIABLE("Variable numérica"),

    /** Operador aritmético */
    OPERADOR("Operador"),

    /** Símbolo de agrupación de apertura */
    PARENTESIS_IZQ("Paréntesis de apertura"),

    /** Símbolo de agrupación de cierre */
    PARENTESIS_DER("Paréntesis de cierre");

    /** Descripción legible del tipo, usada al mostrar información al usuario */
    private final String descripcion;

    TipoToken(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
