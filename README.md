Jennifer Marin Sibaja

src/cr/ac/ucenfotec/pilas/
├── modelo/
│   ├── TipoToken.java          Enum: NUMERO, VARIABLE, OPERADOR, PARENTESIS_IZQ, PARENTESIS_DER
│   └── Token.java              Dato del nodo (inmutable): tipo, lexema, valor, posición, precedencia
├── estructura/
│   ├── Nodo.java               Clase nodo: Token + referencia al nodo inferior
│   ├── Pila.java               Clase pila: apilar, desapilar, verCima, estaVacia, getTamanio, vaciar, dibujar
│   └── PilaVaciaException.java
├── analizador/
│   ├── Tokenizador.java        Analizador léxico (texto → tokens)
│   ├── AnalizadorExpresiones.java  Validación de paréntesis, sintaxis, Shunting-yard, evaluación postfija, variables
│   ├── Traza.java              Tabla paso a paso del estado de la pila
│   └── ExpresionInvalidaException.java
└── ui/
    └── Main.java               main() + menu()
