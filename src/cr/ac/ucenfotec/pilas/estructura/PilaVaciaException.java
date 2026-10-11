package cr.ac.ucenfotec.pilas.estructura;

/**Excepción lanzada cuando se intenta desapilar o consultar la cima de una pila vacía */
public class PilaVaciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PilaVaciaException(String mensaje) {
        super(mensaje);
    }
}
