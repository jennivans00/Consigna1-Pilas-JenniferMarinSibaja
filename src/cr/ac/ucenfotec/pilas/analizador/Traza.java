package cr.ac.ucenfotec.pilas.analizador;

import java.util.ArrayList;
import java.util.List;

/**Registra, paso a paso, lo que ocurre con la pila durante un algoritmo y lo muestra como una tabla de texto alineada */
public class Traza {

    private final String titulo;
    private final String[] encabezados;
    private final List<String[]> filas = new ArrayList<>();

    /**título que se muestra sobre la tabla.Y nombres de las columnas (la columna "Paso" se agrega automáticamente) */
    public Traza(String titulo, String... encabezados) {
        this.titulo = titulo;
        this.encabezados = new String[encabezados.length + 1];
        this.encabezados[0] = "Paso";
        System.arraycopy(encabezados, 0, this.encabezados, 1, encabezados.length);
    }

    /**Agrega una fila a la traza. El número de paso se calcula automáticamente */
    public void registrar(String... valores) {
        String[] fila = new String[encabezados.length];
        fila[0] = String.valueOf(filas.size() + 1);
        for (int i = 1; i < fila.length; i++) {
            fila[i] = (i - 1 < valores.length && valores[i - 1] != null) ? valores[i - 1] : "";
        }
        filas.add(fila);
    }

    /**cantidad de pasos registrados */
    public int getCantidadPasos() {
        return filas.size();
    }

    /**Genera la tabla con columnas de ancho ajustado al contenido */
    @Override
    public String toString() {
        int[] anchos = new int[encabezados.length];
        for (int c = 0; c < encabezados.length; c++) {
            anchos[c] = encabezados[c].length();
        }
        for (String[] fila : filas) {
            for (int c = 0; c < fila.length; c++) {
                anchos[c] = Math.max(anchos[c], fila[c].length());
            }
        }

        StringBuilder separador = new StringBuilder("+");
        for (int ancho : anchos) {
            separador.append("-".repeat(ancho + 2)).append('+');
        }
        separador.append('\n');

        StringBuilder sb = new StringBuilder();
        sb.append("\n  ").append(titulo).append('\n');
        sb.append(separador);
        sb.append(formatearFila(encabezados, anchos));
        sb.append(separador);
        for (String[] fila : filas) {
            sb.append(formatearFila(fila, anchos));
        }
        sb.append(separador);
        return sb.toString();
    }

    private static String formatearFila(String[] celdas, int[] anchos) {
        StringBuilder sb = new StringBuilder("|");
        for (int c = 0; c < celdas.length; c++) {
            sb.append(' ').append(celdas[c]).append(" ".repeat(anchos[c] - celdas[c].length())).append(" |");
        }
        return sb.append('\n').toString();
    }
}
