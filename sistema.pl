% =========================================================================
% 1. BASE DE CONOCIMIENTO AMPLIADA (Hechos con Datos Técnicos)
% =========================================================================

% --- PROCESADORES (CPUs) ---
% cpu(Modelo, Nucleos, Hilos, FrecuenciaBaseGHz, ScoreRendimiento)

% Gama Baja / Entrada
cpu(intel_i3_10100, 4, 8, 3.6, 30).
cpu(amd_ryzen_3_3200g, 4, 4, 3.6, 25).

% Gama Media / Masivos
cpu(amd_ryzen_5_5500, 6, 12, 3.6, 65).
cpu(intel_core_i5_12400f, 6, 12, 2.5, 72).
cpu(amd_ryzen_7_5700x, 8, 16, 3.4, 78).

% Gama Alta / Entusiasta
cpu(intel_core_i9_14900k, 24, 32, 3.2, 98).
cpu(amd_ryzen_7_7800x3d, 8, 16, 4.2, 95).


% --- TARJETAS GRÁFICAS (GPUs) ---
% gpu(Modelo, VRAM_GB, Bus_Bits, ScoreRendimiento)

% Gama Baja / Entrada
gpu(gtx_1050_ti, 4, 128, 25).
gpu(amd_rx_550, 4, 128, 15).

% Gama Media / Masivos
gpu(amd_rx_6600, 8, 128, 60).
gpu(rtx_3060, 12, 192, 65).
gpu(rtx_4060_ti, 8, 128, 75).

% Gama Alta / Entusiasta
gpu(rtx_4090, 24, 384, 100).
gpu(amd_rx_7900_xtx, 24, 384, 94).


% --- JUEGOS ---
% juego(Nombre, GamaRequerida)
juego(counter_strike_2, baja).
juego(warzone, media).
juego(cyberpunk_2077, alta).


% =========================================================================
% 2. REGLAS DE INFERENCIA PARA DETERMINAR LA GAMA DINÁMICAMENTE
% =========================================================================

% Una CPU es gama ALTA si tiene +8 núcleos o un score muy alto
gama_cpu(Modelo, alta) :- 
    cpu(Modelo, Nucleos, _, _, Score), 
    (Nucleos > 8 ; Score >= 85), !.

% Una CPU es gama MEDIA si tiene entre 6 y 8 núcleos
gama_cpu(Modelo, media) :- 
    cpu(Modelo, Nucleos, _, _, Score), 
    Nucleos >= 6, Nucleos =< 8, 
    Score >= 50, Score < 85, !.

% Si no cumple lo anterior, es gama BAJA
gama_cpu(Modelo, baja) :- 
    cpu(Modelo, _, _, _, _).


% Una GPU es gama ALTA si tiene mucha VRAM y un bus ancho, o score top
gama_gpu(Modelo, alta) :- 
    gpu(Modelo, VRAM, Bus, Score), 
    (VRAM >= 12, Bus >= 192 ; Score >= 85), !.

% Una GPU es gama MEDIA por sus specs intermedias
gama_gpu(Modelo, media) :- 
    gpu(Modelo, VRAM, _, Score), 
    VRAM >= 6, 
    Score >= 50, Score < 85, !.

gama_gpu(Modelo, baja) :- 
    gpu(Modelo, _, _, _).


% =========================================================================
% 3. LÓGICA DE CÁLCULO Y RECOMENDACIÓN
% =========================================================================

% Regla para ajustar el impacto del CPU según la resolución
ajustar_scores(ScoreCPU, ScoreGPU, '1080p', ScoreCPU, ScoreGPU).
ajustar_scores(ScoreCPU, ScoreGPU, '2K', NewCPU, ScoreGPU) :- NewCPU is ScoreCPU * 0.85.
ajustar_scores(ScoreCPU, ScoreGPU, '4K', NewCPU, ScoreGPU) :- NewCPU is ScoreCPU * 0.60.

% Regla principal para calcular el cuello de botella (Con matemática dinámica)
calcular_bottleneck(CPU, GPU, Resolucion, Porcentaje, Mensaje) :-
    cpu(CPU, _, _, _, ScoreCPU_Raw),
    gpu(GPU, _, _, ScoreGPU_Raw),
    ajustar_scores(ScoreCPU_Raw, ScoreGPU_Raw, Resolucion, ScoreCPU, ScoreGPU),
    Diferencia is ScoreCPU - ScoreGPU,
    (Diferencia > 15 -> 
        % La CPU es mucho + fuerte -> Cuello en la GPU
        Porcentaje_Raw is (Diferencia / ScoreCPU) * 100,
        Porcentaje is round(Porcentaje_Raw),
        Mensaje = 'Cuello de botella en la GPU: la CPU exige demasiado' 
    ; Diferencia < -15 -> 
        % La GPU es mucho + fuerte -> Cuello en la CPU
        Porcentaje_Raw is (abs(Diferencia) / ScoreGPU) * 100,
        Porcentaje is round(Porcentaje_Raw),
        Mensaje = 'Cuello de botella en la CPU: la GPU esta ociosa' 
    ; 
        % Equilibrio cooperativo entre componentes
        Porcentaje = 0, 
        Mensaje = 'Configuracion equilibrada ideal'
    ).

% SUGERENCIA DE COMPONENTE
recomendar_gpu_para_cpu(CPU, GPU_Sugerida) :-
    gama_cpu(CPU, Gama),
    gama_gpu(GPU_Sugerida, Gama).

% JUEGOS SUGERIDOS
juegos_sugeridos_por_gpu(GPU, JuegoBajo, JuegoMedio, JuegoAlto) :-
    gama_gpu(GPU, _), 
    juego(JuegoBajo, baja),
    juego(JuegoMedio, media),
    juego(JuegoAlto, alta).