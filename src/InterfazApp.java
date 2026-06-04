import org.jpl7.Query;
import org.jpl7.Term;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class InterfazApp extends JFrame {
    // Componentes de la interfaz
    private JComboBox<String> comboCPU;
    private JComboBox<String> comboGPU;
    private JComboBox<String> comboRes;
    private JLabel lblPorcentaje;
    private JTextArea txtDictamen;
    private JTextArea txtJuegos;
    private PanelGrafico panelGrafico; // Nuestro gráfico visual

    public InterfazApp() {
        // 1. Intentar cargar el archivo Prolog al iniciar
        if (!Query.hasSolution("consult('sistema.pl')")) {
            JOptionPane.showMessageDialog(this, "Error crítico: No se pudo cargar sistema.pl\nVerifique que el archivo esté en la raíz del proyecto.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // 2. Configuración de la Ventana Principal
        setTitle("Sistema Experto - Analizador de Hardware (Multi-Paradigma)");
        setSize(750, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 3. Panel Superior: Selección de componentes (Actualizado con la base de datos ampliada)
        JPanel panelInputs = new JPanel(new GridLayout(4, 2, 10, 10));
        panelInputs.setBorder(BorderFactory.createTitledBorder(" Selección de Hardware "));

        panelInputs.add(new JLabel(" Seleccione CPU:"));
        comboCPU = new JComboBox<>(new String[]{
            "intel_i3_10100", "amd_ryzen_3_3200g", "amd_ryzen_5_5500", 
            "intel_core_i5_12400f", "amd_ryzen_7_5700x", "intel_core_i9_14900k", "amd_ryzen_7_7800x3d"
        });
        panelInputs.add(comboCPU);

        panelInputs.add(new JLabel(" Seleccione GPU:"));
        comboGPU = new JComboBox<>(new String[]{
            "gtx_1050_ti", "amd_rx_550", "amd_rx_6600", 
            "rtx_3060", "rtx_4060_ti", "rtx_4090", "amd_rx_7900_xtx"
        });
        panelInputs.add(comboGPU);

        panelInputs.add(new JLabel(" Resolución Objetivo:"));
        comboRes = new JComboBox<>(new String[]{"1080p", "2K", "4K"});
        panelInputs.add(comboRes);

        JButton btnCalcular = new JButton("Analizar Configuración");
        panelInputs.add(btnCalcular);
        add(panelInputs, BorderLayout.NORTH);

        // 4. Panel Central: El Gráfico del Cuello de Botella
        panelGrafico = new PanelGrafico();
        add(panelGrafico, BorderLayout.CENTER);

        // 5. Panel Inferior: Resultados de Texto y Juegos
        JPanel panelResultados = new JPanel(new GridLayout(1, 2, 10, 10));
        panelResultados.setBorder(BorderFactory.createTitledBorder(" Diagnóstico del Sistema Experto "));

        JPanel panelTexto = new JPanel(new BorderLayout());
        lblPorcentaje = new JLabel("Cuello de Botella: --%", SwingConstants.CENTER);
        lblPorcentaje.setFont(new Font("Arial", Font.BOLD, 16));
        panelTexto.add(lblPorcentaje, BorderLayout.NORTH);

        txtDictamen = new JTextArea(4, 20);
        txtDictamen.setEditable(false);
        txtDictamen.setLineWrap(true);
        txtDictamen.setWrapStyleWord(true);
        panelTexto.add(new JScrollPane(txtDictamen), BorderLayout.CENTER);

        JPanel panelJuegos = new JPanel(new BorderLayout());
        panelJuegos.setBorder(BorderFactory.createTitledBorder(" Juegos Base Recomendados "));
        txtJuegos = new JTextArea();
        txtJuegos.setEditable(false);
        txtJuegos.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panelJuegos.add(new JScrollPane(txtJuegos), BorderLayout.CENTER);

        panelResultados.add(panelTexto);
        panelResultados.add(panelJuegos);
        add(panelResultados, BorderLayout.SOUTH);

        // 6. Lógica del Botón (Conexión directa con Prolog)
        btnCalcular.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejecutarAnalisis();
            }
        });
    }

    private void ejecutarAnalisis() {
        String cpu = comboCPU.getSelectedItem().toString();
        String gpu = comboGPU.getSelectedItem().toString();
        String res = "'" + comboRes.getSelectedItem().toString() + "'";

        // Consulta de Cuello de Botella a Prolog
        String consultaTexto = "calcular_bottleneck(" + cpu + ", " + gpu + ", " + res + ", Porcentaje, Mensaje)";
        Query q = new Query(consultaTexto);

        if (q.hasSolution()) {
            Map<String, Term> resBot = q.oneSolution();
            int porcentaje = resBot.get("Porcentaje").intValue();
            String dictamen = resBot.get("Mensaje").toString().replace("'", "");

            // Actualizar textos en la pantalla
            lblPorcentaje.setText("Cuello de Botella: " + porcentaje + "%");
            txtDictamen.setText(dictamen);

            // Extraer los scores técnicos reales directo de Prolog para alimentar el gráfico
            int scoreCPU = 50; 
            int scoreGPU = 50;
            
            Query qScoreCPU = new Query("cpu(" + cpu + ", _, _, _, Score)");
            if(qScoreCPU.hasSolution()) {
                scoreCPU = qScoreCPU.oneSolution().get("Score").intValue();
            }
            
            Query qScoreGPU = new Query("gpu(" + gpu + ", _, _, Score)");
            if(qScoreGPU.hasSolution()) {
                scoreGPU = qScoreGPU.oneSolution().get("Score").intValue();
            }

            // Actualizar el gráfico visual con los scores dinámicos reales
            panelGrafico.actualizarGrafico(scoreCPU, scoreGPU);

            // Consultar la gama deducida dinámicamente y los juegos recomendados
            Query qGama = new Query("gama_gpu(" + gpu + ", Gama)");
            if (qGama.hasSolution()) {
                String gama = qGama.oneSolution().get("Gama").toString().replace("'", "");
                
                Query qJuegos = new Query("juegos_sugeridos_por_gpu(" + gpu + ", JBajo, JMedio, JAlto)");
                if (qJuegos.hasSolution()) {
                    Map<String, Term> resJuegos = qJuegos.oneSolution();
                    txtJuegos.setText(
                        "📌 Gama Detectada: " + gama.toUpperCase() + "\n\n" +
                        "🎮 Para Specs Bajas: " + resJuegos.get("JBajo").toString().replace("_", " ") + "\n" +
                        "🎮 Para Specs Medias: " + resJuegos.get("JMedio").toString().replace("_", " ") + "\n" +
                        "🎮 Para Specs Altas: " + resJuegos.get("JAlto").toString().replace("_", " ")
                    );
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new InterfazApp().setVisible(true);
        });
    }
}

// Clase interna para dibujar el gráfico comparativo basado en las specs de Prolog
class PanelGrafico extends JPanel {
    private int rendimientoCPU = 50;
    private int rendimientoGPU = 50;

    public PanelGrafico() {
        setBorder(BorderFactory.createTitledBorder(" Ritmo de Trabajo y Rendimiento Relativo "));
    }

    public void actualizarGrafico(int cpu, int gpu) {
        this.rendimientoCPU = cpu;
        this.rendimientoGPU = gpu;
        repaint(); // Obliga a Java a redibujar las barras
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int anchoMaximo = getWidth() - 180;

        // Dibujar Barra de CPU
        g2.setColor(new Color(220, 53, 69)); // Rojo
        g2.fillRect(100, 40, (rendimientoCPU * anchoMaximo) / 100, 30);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("Score CPU", 20, 60);
        g2.drawString(rendimientoCPU + " pts", (rendimientoCPU * anchoMaximo) / 100 + 110, 60);

        // Dibujar Barra de GPU
        g2.setColor(new Color(40, 167, 69)); // Verde
        g2.fillRect(100, 90, (rendimientoGPU * anchoMaximo) / 100, 30);
        g2.setColor(Color.BLACK);
        g2.drawString("Score GPU", 20, 110);
        g2.drawString(rendimientoGPU + " pts", (rendimientoGPU * anchoMaximo) / 100 + 110, 110);

        // Nota explicativa dinámica en el gráfico
        g2.setFont(new Font("Arial", Font.ITALIC, 12));
        if (rendimientoCPU > (rendimientoGPU + 15)) {
            g2.drawString("⚠️ La GPU se queda corta frente al procesador (Cuello de Botella Gráfico).", 20, 160);
        } else if (rendimientoGPU > (rendimientoCPU + 15)) {
            g2.drawString("⚠️ El procesador limita la potencia total de la GPU (Cuello de Botella de CPU).", 20, 160);
        } else {
            g2.drawString("✅ Configuración equilibrada. Los componentes cooperan eficientemente.", 20, 160);
        }
    }
}