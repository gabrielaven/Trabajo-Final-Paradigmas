import org.jpl7.Query;
import org.jpl7.Term;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // 1. Cargar el archivo de Prolog
        if (!Query.hasSolution("consult('sistema.pl')")) {
            System.out.println("❌ Error al cargar sistema.pl");
            return;
        }

        // 2. Simulamos las entradas que el usuario elegiría en la interfaz gráfica
        String cpuElegida = "intel_i3";
        String gpuElegida = "rtx4090";
        String resolucionElegida = "'4K'"; // Las strings en Prolog van con comilla simple si llevan mayúsculas

        // 3. Consulta de Cuello de Botella
        // Estructura: calcular_bottleneck(intel_i3, rtx4090, '4K', Porcentaje, Mensaje)
        String consultaBottleneck = "calcular_bottleneck(" + cpuElegida + ", " + gpuElegida + ", " + resolucionElegida + ", Porcentaje, Mensaje)";
        Query q1 = new Query(consultaBottleneck);

        if (q1.hasSolution()) {
            Map<String, Term> resultado = q1.oneSolution();
            System.out.println("====== DIAGNÓSTICO DE COMPONENTES ======");
            System.out.println("Resolución analizada: " + resolucionElegida);
            System.out.println("Porcentaje estimado: " + resultado.get("Porcentaje") + "%");
            System.out.println("Dictamen técnico: " + resultado.get("Mensaje"));
        }

        System.out.println();

        // 4. Consulta de los 3 Juegos Base (Eje central del TP)
        // Primero necesitamos saber qué gama tiene la GPU elegida para pasarle a la regla
        Query qGama = new Query("gpu(" + gpuElegida + ", Gama, _)");
        if (qGama.hasSolution()) {
            String gama = qGama.oneSolution().get("Gama").toString();
            
            // Le pedimos los 3 juegos base a Prolog
            String consultaJuegos = "juegos_sugeridos(" + gama + ", JBajo, JMedio, JAlto)";
            Query q3 = new Query(consultaJuegos);
            
            if (q3.hasSolution()) {
                Map<String, Term> resJuegos = q3.oneSolution();
                System.out.println("====== JUEGOS BASE RECOMENDADOS ======");
                System.out.println("🎮 Gama Baja: " + resJuegos.get("JBajo"));
                System.out.println("🎮 Gama Media: " + resJuegos.get("JMedio"));
                System.out.println("🎮 Gama Alta: " + resJuegos.get("JAlto"));
            }
        }
    }
}