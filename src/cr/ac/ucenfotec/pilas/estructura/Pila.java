package cr.ac.ucenfotec.pilas.estructura;

import cr.ac.ucenfotec.pilas.modelo.Token;

import java.util.Objects;

public class Pila {

    /** Nodo que está en la cima (último en entrar). Null si la pila está vacía */
    private Nodo cima;

    /** Cantidad de elementos en la pila */
    private int tamanio;

    /** Crea una pila vacía */
    public Pila() {
        this.cima = null;
        this.tamanio = 0;
    }

    // Operaciones fundamentales

    public void apilar(Token token) {
        Objects.requireNonNull(token, "No se puede apilar un token nulo.");
        cima = new Nodo(token, cima); // el nuevo nodo apunta a la cima anterior
        tamanio++;
    }

    /**Retira y devuelve el token de la cima */
    public Token desapilar() {
        if (estaVacia()) {
            throw new PilaVaciaException("No se puede desapilar: la pila está vacía.");
        }
        Token dato = cima.getDato();
        Nodo anterior = cima;
        cima = cima.getSiguiente();
        anterior.setSiguiente(null); // se desliga el nodo para facilitar la recolección de basura
        tamanio--;
        return dato;
    }

    /**Devuelve el token de la cima sin retirarlo */
    public Token verCima() {
        if (estaVacia()) {
            throw new PilaVaciaException("No hay cima: la pila está vacía.");
        }
        return cima.getDato();
    }

    /**Indica si la pila no tiene elementos */
    public boolean estaVacia() {
        return cima == null;
    }

    /**Cantidad de elementos almacenados */
    public int getTamanio() {
        return tamanio;
    }

    /**Elimina todos los elementos de la pila */
    public void vaciar() {
        cima = null;
        tamanio = 0;
    }

    // Representación

    /**Copia los tokens a un arreglo ordenado desde la base hasta la cima */
    public Token[] aArreglo() {
        Token[] elementos = new Token[tamanio];
        Nodo actual = cima;
        for (int i = tamanio - 1; i >= 0; i--) {
            elementos[i] = actual.getDato();
            actual = actual.getSiguiente();
        }
        return elementos;
    }

    /**Representación horizontal compacta, de la base (izquierda) a la cima (derecha) */
    @Override
    public String toString() {
        if (estaVacia()) {
            return "[ ]";
        }
        StringBuilder sb = new StringBuilder("[ ");
        for (Token t : aArreglo()) {
            sb.append(t.getLexema()).append(' ');
        }
        return sb.append(']').toString();
    }

    /**Dibuja la pila verticalmente en ASCII */
    public String dibujar() {
        if (estaVacia()) {
            return "          +--------------+\n"
                    + "          | (pila vacía) |\n"
                    + "          +--------------+\n";
        }
        // El ancho de la casilla se ajusta al lexema más largo
        int ancho = 7;
        for (Nodo n = cima; n != null; n = n.getSiguiente()) {
            ancho = Math.max(ancho, n.getDato().getLexema().length() + 4);
        }
        String borde = "          +" + "-".repeat(ancho) + "+\n";
        StringBuilder sb = new StringBuilder(borde);
        boolean esCima = true;
        for (Nodo n = cima; n != null; n = n.getSiguiente()) {
            sb.append(esCima ? "  cima -> |" : "          |")
                    .append(centrar(n.getDato().getLexema(), ancho))
                    .append("|\n")
                    .append(borde);
            esCima = false;
        }
        sb.append(" ".repeat(10)).append(centrar("base", ancho + 2)).append('\n');
        return sb.toString();
    }

    /** Centra un texto dentro de un ancho dado */
    private static String centrar(String texto, int ancho) {
        int espacio = Math.max(0, ancho - texto.length());
        int izquierda = espacio / 2;
        return " ".repeat(izquierda) + texto + " ".repeat(espacio - izquierda);
    }
}
