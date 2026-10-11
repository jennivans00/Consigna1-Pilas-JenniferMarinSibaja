package cr.ac.ucenfotec.pilas.analizador;

import cr.ac.ucenfotec.pilas.modelo.TipoToken;
import cr.ac.ucenfotec.pilas.modelo.Token;

import java.util.ArrayList;
import java.util.List;

/**Analizador léxico (<i>lexer</i>): recorre el texto de una expresión aritmética y lo convierte en una lista de tokens. Un token es un número, una variable, 
un operador o un paréntesis. Los espacios se ignoran */
public class Tokenizador {

    /**Convierte el texto de una expresión en una lista de tokens.*/
    public List<Token> tokenizar(String expresion) throws ExpresionInvalidaException {
        if (expresion == null || expresion.isBlank()) {
            throw new ExpresionInvalidaException("La expresión está vacía.");
        }

        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < expresion.length()) {
            char c = expresion.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;   // los espacios se ignoran
            } else if (Character.isDigit(c) || c == '.') {
                i = leerNumero(expresion, i, tokens);
            } else if (Character.isLetter(c) || c == '_') {
                i = leerVariable(expresion, i, tokens);
            } else if (Token.esSimboloOperador(String.valueOf(c))) {
                agregarOperador(c, i, tokens);
                i++;
            } else if (Token.esApertura(c)) {
                tokens.add(Token.parentesisIzq(c, i));
                i++;
            } else if (Token.esCierre(c)) {
                tokens.add(Token.parentesisDer(c, i));
                i++;
            } else {
                throw new ExpresionInvalidaException("Carácter no reconocido '" + c + "'.", i);
            }
        }

        if (tokens.isEmpty()) {
            throw new ExpresionInvalidaException("La expresión no contiene tokens válidos.");
        }
        return tokens;
    }

    /**Crea un único token a partir de un texto corto */
    public Token crearTokenIndividual(String texto) throws ExpresionInvalidaException {
        if (texto == null || texto.isBlank()) {
            throw new ExpresionInvalidaException("Debe escribir un token.");
        }
        String limpio = texto.trim();
        // Un operador solo ("-", "+") se trata como operador binario, no como signo
        if (Token.esSimboloOperador(limpio)) {
            return Token.operador(limpio, 0);
        }
        List<Token> tokens = tokenizar(limpio);
        if (tokens.size() != 1) {
            throw new ExpresionInvalidaException(
                    "'" + limpio + "' contiene " + tokens.size() + " tokens; escriba solo uno.");
        }
        return tokens.get(0);
    }

    /**Lee un número literal a partir de la posición dada */
    private int leerNumero(String expresion, int inicio, List<Token> tokens)
            throws ExpresionInvalidaException {
        int i = inicio;
        int puntos = 0;
        while (i < expresion.length()
                && (Character.isDigit(expresion.charAt(i)) || expresion.charAt(i) == '.')) {
            if (expresion.charAt(i) == '.') {
                puntos++;
            }
            i++;
        }
        String lexema = expresion.substring(inicio, i);
        if (puntos > 1 || lexema.equals(".")) {
            throw new ExpresionInvalidaException("Número mal formado '" + lexema + "'.", inicio);
        }
        // Un número seguido directamente de una letra (p. ej. "3x") es ambiguo
        if (i < expresion.length()
                && (Character.isLetter(expresion.charAt(i)) || expresion.charAt(i) == '_')) {
            throw new ExpresionInvalidaException(
                    "Número seguido de una letra; use un operador explícito (p. ej. 3*x).", i);
        }
        tokens.add(Token.numero(Double.parseDouble(lexema), lexema, inicio));
        return i;
    }

    /**Lee un identificador de variable: letra o '_' seguido de letras, dígitos o '_'.*/
    private int leerVariable(String expresion, int inicio, List<Token> tokens) {
        int i = inicio;
        while (i < expresion.length()
                && (Character.isLetterOrDigit(expresion.charAt(i)) || expresion.charAt(i) == '_')) {
            i++;
        }
        tokens.add(Token.variable(expresion.substring(inicio, i), inicio));
        return i;
    }

    /**Agrega un operador decidiendo si un '+' o '-' es unario (signo) o binario, según el token anterior */
    private void agregarOperador(char simbolo, int posicion, List<Token> tokens) {
        boolean esUnario = (simbolo == '-' || simbolo == '+') && estaEnPosicionUnaria(tokens);
        if (!esUnario) {
            tokens.add(Token.operador(String.valueOf(simbolo), posicion));
        } else if (simbolo == '-') {
            tokens.add(Token.operador(Token.MENOS_UNARIO, posicion));
        }
        // Un '+' unario no altera el valor, por lo que simplemente se omite
    }

    private boolean estaEnPosicionUnaria(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return true;
        }
        TipoToken anterior = tokens.get(tokens.size() - 1).getTipo();
        return anterior == TipoToken.OPERADOR || anterior == TipoToken.PARENTESIS_IZQ;
    }
}
