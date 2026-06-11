import org.jpl7.Query;
import org.jpl7.Term;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.io.File;
import java.util.Map;

public class InterfazApp extends JFrame {
    // Componentes de la interfaz
    private JComboBox<String> comboCPU;
    private JComboBox<String> comboGPU;
    private JComboBox<String> comboRes;
    private PanelMedidor panelMedidor;
    private JTextArea txtDictamen;
    private JPanel panelListaJuegos; 
    private PanelGrafico panelGrafico;

    // --- COLORES DEL TEMA ---
    private final Color FONDO_PRINCIPAL = new Color(27, 32, 43);
    private final Color FONDO_PANELES = new Color(34, 40, 52);
    private final Color BORDE_CYAN = new Color(55, 143, 179);
    private final Color TEXTO_CLARO = new Color(230, 230, 230);
    private final Color TEXTO_CYAN = new Color(74, 192, 224);

    public InterfazApp() {
        // Cargar archivo Prolog
        if (!Query.hasSolution("consult('sistema.pl')")) {
            JOptionPane.showMessageDialog(this, "Error crítico: No se pudo cargar sistema.pl\nVerifique que el archivo esté en la raíz del proyecto.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        setTitle("Sistema Experto - Analizador de Hardware");
        setSize(800, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(FONDO_PRINCIPAL);

        JPanel panelContenedor = new JPanel(new BorderLayout(15, 15));
        panelContenedor.setBackground(FONDO_PRINCIPAL);
        panelContenedor.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setContentPane(panelContenedor);

        JPanel panelInputs = new JPanel(new GridLayout(4, 2, 10, 15));
        panelInputs.setBackground(FONDO_PANELES);
        panelInputs.setBorder(crearBordePersonalizado(" Selección de Hardware "));

        panelInputs.add(crearLabel(" Seleccione CPU:"));
        comboCPU = crearComboBox(new String[]{
            "intel_i3_10100", "amd_ryzen_3_3200g", "amd_ryzen_5_5500", 
            "intel_core_i5_12400f", "amd_ryzen_7_5700x", "intel_core_i9_14900k", "amd_ryzen_7_7800x3d"
        });
        panelInputs.add(comboCPU);

        panelInputs.add(crearLabel(" Seleccione GPU:"));
        comboGPU = crearComboBox(new String[]{
            "gtx_1050_ti", "amd_rx_550", "amd_rx_6600", 
            "rtx_3060", "rtx_4060_ti", "rtx_4090", "amd_rx_7900_xtx"
        });
        panelInputs.add(comboGPU);

        panelInputs.add(crearLabel(" Resolución Objetivo:"));
        comboRes = crearComboBox(new String[]{"1080p", "2K", "4K"});
        panelInputs.add(comboRes);

        JButton btnCalcular = new JButton("ANALIZAR CONFIGURACIÓN");
        btnCalcular.setBackground(new Color(30, 60, 90));
        btnCalcular.setForeground(new Color(255, 215, 0));
        btnCalcular.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCalcular.setFocusPainted(false);
        btnCalcular.setBorder(BorderFactory.createLineBorder(BORDE_CYAN, 1));
        panelInputs.add(btnCalcular);
        
        panelContenedor.add(panelInputs, BorderLayout.NORTH);

        panelGrafico = new PanelGrafico();
        panelGrafico.setBackground(FONDO_PANELES);
        panelGrafico.setBorder(crearBordePersonalizado(" Carga Relativa de Trabajo (Simulación) "));
        panelContenedor.add(panelGrafico, BorderLayout.CENTER);

        JPanel panelResultados = new JPanel(new GridLayout(1, 2, 15, 15));
        panelResultados.setBackground(FONDO_PRINCIPAL);

        JPanel panelDiagnostico = new JPanel(new BorderLayout(5, 5));
        panelDiagnostico.setBackground(FONDO_PANELES);
        panelDiagnostico.setBorder(crearBordePersonalizado(" Diagnóstico del Sistema "));

        panelMedidor = new PanelMedidor();
        panelMedidor.setBackground(FONDO_PANELES);
        panelMedidor.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panelDiagnostico.add(panelMedidor, BorderLayout.NORTH);

        txtDictamen = new JTextArea(3, 20);
        estilizarTextArea(txtDictamen);
        panelDiagnostico.add(new JScrollPane(txtDictamen), BorderLayout.CENTER);

        JPanel panelJuegosContenedor = new JPanel(new BorderLayout());
        panelJuegosContenedor.setBackground(FONDO_PANELES);
        panelJuegosContenedor.setBorder(crearBordePersonalizado(" Rendimiento Estimado en Juegos "));
        
        panelListaJuegos = new JPanel();
        panelListaJuegos.setLayout(new BoxLayout(panelListaJuegos, BoxLayout.Y_AXIS));
        panelListaJuegos.setBackground(FONDO_PANELES);
        panelListaJuegos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panelJuegosContenedor.add(new JScrollPane(panelListaJuegos), BorderLayout.CENTER);

        panelResultados.add(panelDiagnostico);
        panelResultados.add(panelJuegosContenedor);
        panelContenedor.add(panelResultados, BorderLayout.SOUTH);

        btnCalcular.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejecutarAnalisis();
            }
        });
    }

    // --- UTILIDADES VISUALES Y DE FORMATO ---
    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(TEXTO_CLARO);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return lbl;
    }

    private JComboBox<String> crearComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.BLACK);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return combo;
    }

    private TitledBorder crearBordePersonalizado(String titulo) {
        Border bordeLineal = BorderFactory.createLineBorder(BORDE_CYAN, 1);
        return BorderFactory.createTitledBorder(bordeLineal, titulo, TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), TEXTO_CYAN);
    }

    private void estilizarTextArea(JTextArea area) {
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(FONDO_PANELES);
        area.setForeground(TEXTO_CLARO);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private String formatearNombreJuego(String idJuego) {
        String[] palabras = idJuego.split("_");
        StringBuilder nombreFormateado = new StringBuilder();
        for (String palabra : palabras) {
            if (palabra.length() > 0) {
                nombreFormateado.append(Character.toUpperCase(palabra.charAt(0)))
                                .append(palabra.substring(1))
                                .append(" ");
            }
        }
        return nombreFormateado.toString().trim();
    }

    private JPanel crearFilaJuego(String nivelSpecs, String idJuego) {
        JPanel panelFila = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panelFila.setBackground(FONDO_PANELES);
        panelFila.setAlignmentX(Component.LEFT_ALIGNMENT);

        String nombreBonito = formatearNombreJuego(idJuego);
        JLabel lblTexto = new JLabel("Specs " + nivelSpecs + ": " + nombreBonito);
        lblTexto.setForeground(TEXTO_CLARO);
        lblTexto.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String rutaImagen = "assets/juegos/" + idJuego + ".png";
        JLabel lblIcono = new JLabel();
        
        File archivoImg = new File(rutaImagen);
        if (archivoImg.exists()) {
            ImageIcon iconoOriginal = new ImageIcon(rutaImagen);
            Image imgEscalada = iconoOriginal.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            lblIcono.setIcon(new ImageIcon(imgEscalada));
        } else {
            lblIcono.setText("[Sin imagen]");
            lblIcono.setForeground(new Color(150, 150, 150)); 
            lblIcono.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        }

        panelFila.add(lblTexto);
        panelFila.add(lblIcono);
        
        return panelFila;
    }

    // --- LÓGICA DE PROLOG ---
    private void ejecutarAnalisis() {
        String cpu = comboCPU.getSelectedItem().toString();
        String gpu = comboGPU.getSelectedItem().toString();
        String res = "'" + comboRes.getSelectedItem().toString() + "'";

        // 1. Calcular Cuello de Botella General
        String consultaTexto = "calcular_bottleneck(" + cpu + ", " + gpu + ", " + res + ", Porcentaje, Mensaje)";
        Query q = new Query(consultaTexto);

        if (q.hasSolution()) {
            Map<String, Term> resBot = q.oneSolution();
            int porcentaje = resBot.get("Porcentaje").intValue();
            String dictamen = resBot.get("Mensaje").toString().replace("'", "");

            panelMedidor.actualizarPorcentaje(porcentaje);
            txtDictamen.setText(dictamen);

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

            String recomendacionFinal = "";
            String consultaRecom = "obtener_recomendaciones_exclusivas(" + cpu + ", " + gpu + ", " + res + ", Tipo, Lista)";
            Query qRecom = new Query(consultaRecom);

            if (qRecom.hasSolution()) {
                Map<String, Term> resRecom = qRecom.oneSolution();
                String tipoCambio = resRecom.get("Tipo").toString().replace("'", "");

                if (!tipoCambio.equals("NINGUNO")) {
                    String listaCruda = resRecom.get("Lista").toString().replaceAll("[\\[\\]']", "");
                    
                    if (!listaCruda.trim().isEmpty()) {
                        String[] componentes = listaCruda.split(",");
                        StringBuilder textoRec = new StringBuilder("💡 Recomendación: Mejorar " + tipoCambio + " a -> ");
                        
                        int limite = Math.min(componentes.length, 3);
                        for (int i = 0; i < limite; i++) {
                            textoRec.append(componentes[i].trim().toUpperCase().replace("_", " "));
                            if (i < limite - 1) textoRec.append(" | ");
                        }
                        if (componentes.length > 3) textoRec.append(" (entre otras)");
                        
                        recomendacionFinal = textoRec.toString();
                    } else {
                        recomendacionFinal = "💡 Recomendación: Considerar actualizar " + tipoCambio + " (No hay opciones superiores en BD).";
                    }
                }
            }

            panelGrafico.actualizarGrafico(scoreCPU, scoreGPU, recomendacionFinal);

            Query qGama = new Query("gama_gpu(" + gpu + ", Gama)");
            if (qGama.hasSolution()) {
                String gama = qGama.oneSolution().get("Gama").toString().replace("'", "");
                
                Query qJuegos = new Query("juegos_sugeridos_por_gpu(" + gpu + ", JBajo, JMedio, JAlto)");
                if (qJuegos.hasSolution()) {
                    Map<String, Term> resJuegos = qJuegos.oneSolution();
                    String idBajo = resJuegos.get("JBajo").toString();
                    String idMedio = resJuegos.get("JMedio").toString();
                    String idAlto = resJuegos.get("JAlto").toString();

                    panelListaJuegos.removeAll(); 
                    
                    JLabel lblGama = new JLabel("GAMA DETECTADA: " + gama.toUpperCase());
                    lblGama.setForeground(TEXTO_CYAN);
                    lblGama.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    lblGama.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                    lblGama.setAlignmentX(Component.LEFT_ALIGNMENT);
                    
                    panelListaJuegos.add(lblGama);
                    panelListaJuegos.add(crearFilaJuego("Bajas", idBajo));
                    panelListaJuegos.add(crearFilaJuego("Medias", idMedio));
                    panelListaJuegos.add(crearFilaJuego("Altas", idAlto));

                    panelListaJuegos.revalidate(); 
                    panelListaJuegos.repaint();
                }
            }
        }
    }

    public static void main(String[] args) {
        UIManager.put("ScrollBar.background", new Color(27, 32, 43));
        UIManager.put("OptionPane.background", new Color(34, 40, 52));
        UIManager.put("Panel.background", new Color(34, 40, 52));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        
        SwingUtilities.invokeLater(() -> {
            new InterfazApp().setVisible(true);
        });
    }
}

// --- CLASE PARA EL MEDIDOR SEMICIRCULAR ---
class PanelMedidor extends JPanel {
    private int porcentaje = 0;

    public PanelMedidor() {
        setPreferredSize(new Dimension(200, 190));
    }

    public void actualizarPorcentaje(int porcentaje) {
        this.porcentaje = porcentaje;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int grosor = 30;
        int size = Math.min(getWidth(), getHeight() * 2) - grosor - 20;
        int x = (getWidth() - size) / 2;
        int y = 20;

        g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(210, 210, 210));
        g2.draw(new Arc2D.Double(x, y, size, size, 0, 180, Arc2D.OPEN));

        Color colorBarra = (porcentaje > 20) ? new Color(230, 80, 80) : new Color(80, 200, 120);
        g2.setColor(colorBarra);
        int anguloLlenado = (int) (180 * (porcentaje / 100.0));
        g2.draw(new Arc2D.Double(x, y, size, size, 180, -anguloLlenado, Arc2D.OPEN));

        String texto = porcentaje + "%";
        g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(texto)) / 2;
        int textY = y + (size / 2) + 15;
        g2.drawString(texto, textX, textY);
    }
}

// --- CLASE PARA LAS BARRAS HORIZONTALES ---
class PanelGrafico extends JPanel {
    private int rendimientoCPU = 50;
    private int rendimientoGPU = 50;
    private String recomendacion = "";

    public void actualizarGrafico(int cpu, int gpu, String recomendacion) {
        this.rendimientoCPU = cpu;
        this.rendimientoGPU = gpu;
        this.recomendacion = recomendacion;
        repaint(); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int anchoMaximo = getWidth() - 220;
        int inicioX = 150;
        int altoBarra = 25;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        g2.drawString("PROCESADOR", 20, 50);

        g2.setColor(new Color(50, 55, 70));
        g2.fillRoundRect(inicioX, 32, anchoMaximo, altoBarra, 10, 10);
        
        g2.setColor(new Color(25, 140, 190)); 
        int anchoCPU = (rendimientoCPU * anchoMaximo) / 100;
        g2.fillRoundRect(inicioX, 32, anchoCPU, altoBarra, 10, 10);
        
        g2.setColor(Color.WHITE);
        g2.drawString(rendimientoCPU + "%", inicioX + anchoMaximo + 15, 50);

        g2.drawString("GRÁFICA (GPU)", 20, 95);

        g2.setColor(new Color(50, 55, 70));
        g2.fillRoundRect(inicioX, 77, anchoMaximo, altoBarra, 10, 10);

        g2.setColor(new Color(80, 180, 110)); 
        int anchoGPU = (rendimientoGPU * anchoMaximo) / 100;
        g2.fillRoundRect(inicioX, 77, anchoGPU, altoBarra, 10, 10);

        g2.setColor(Color.WHITE);
        g2.drawString(rendimientoGPU + "%", inicioX + anchoMaximo + 15, 95);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (rendimientoCPU > (rendimientoGPU + 15)) {
            g2.setColor(new Color(220, 100, 100));
            g2.drawString("Cuello de Botella Gráfico: La GPU se queda corta frente al procesador.", 20, 135);
        } else if (rendimientoGPU > (rendimientoCPU + 15)) {
            g2.setColor(new Color(220, 160, 80));
            g2.drawString("Cuello de Botella CPU: El procesador limita la potencia de la GPU.", 20, 135);
        } else {
            g2.setColor(new Color(80, 200, 120));
            g2.drawString("Equilibrio óptimo: Ambos componentes están sincronizados de manera eficiente.", 20, 135);
        }

        if (recomendacion != null && !recomendacion.isEmpty()) {
            g2.setColor(new Color(255, 215, 0));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString(recomendacion, 20, 165);
        }
    }
}