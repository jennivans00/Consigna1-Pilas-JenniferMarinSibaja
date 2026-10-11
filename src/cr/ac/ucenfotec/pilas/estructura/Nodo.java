package cr.ac.ucenfotec.pilas.estructura;

import cr.ac.ucenfotec.pilas.modelo.Token;

import java.util.Objects;

/**Nodo de la lista simplemente enlazada sobre la que se construye la pila */
public class Nodo {

    /** Token almacenado en el nodo. Es inmutable, por eso el campo es final */
    private final Token dato;

    /** Referencia al nodo inmediatamente inferior en la pila */
    private Nodo siguiente;

    /**Crea un nodo sin sucesor (útil para el primer elemento de la pila) */
    public Nodo(Token dato) {
        this(dato, null);
    }

    /**Crea un nodo enlazado con el nodo que quedará debajo de él */
    public Nodo(Token dato, Nodo siguiente) {
        this.dato = Objects.requireNonNull(dato, "Un nodo no puede almacenar un token nulo.");
        this.siguiente = siguiente;
    }

    /**Token almacenado */
    public Token getDato() {
        return dato;
    }

    public Nodo getSiguiente() {
        return siguiente;
    }

    /** Siguiente nuevo nodo inferior */
    public void setSiguiente(Nodo siguiente) {
        this.siguiente = siguiente;
    }

    @Override
    public String toString() {
        return "Nodo[" + dato + "]";
    }
}
