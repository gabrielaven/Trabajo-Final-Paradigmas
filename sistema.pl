% 1. BASE DE CONOCIMIENTO (Hechos)

% cpu(Modelo, ComponenteGama, ScoreRendimiento)
cpu(intel_i3, baja, 30).
cpu(amd_ryzen5, media, 65).
cpu(intel_i9, alta, 95).

% gpu(Modelo, ComponenteGama, ScoreRendimiento)
gpu(gtx1050, baja, 25).
gpu(rtx3060, media, 60).
gpu(rtx4090, alta, 100).

% juego(Nombre, GamaRequerida)
juego(counter_strike_2, baja).
juego(gta_v, media).
juego(cyberpunk_2077, alta).


% 2. LÓGICA DE INFERENCIA (Reglas)

% Regla para ajustar el impacto del CPU según la resolución
ajustar_scores(ScoreCPU, ScoreGPU, '1080p', ScoreCPU, ScoreGPU).
ajustar_scores(ScoreCPU, ScoreGPU, '2K', NewCPU, ScoreGPU) :- NewCPU is ScoreCPU * 0.85.
ajustar_scores(ScoreCPU, ScoreGPU, '4K', NewCPU, ScoreGPU) :- NewCPU is ScoreCPU * 0.60.

% Regla principal para calcular el cuello de botella
calcular_bottleneck(CPU, GPU, Resolucion, Porcentaje, Mensaje) :-
    cpu(CPU, _, ScoreCPU_Raw),
    gpu(GPU, GamaGPU, ScoreGPU_Raw),
    ajustar_scores(ScoreCPU_Raw, ScoreGPU_Raw, Resolucion, ScoreCPU, ScoreGPU),
    Diferencia is ScoreCPU - ScoreGPU,
    (Diferencia > 20 -> 
        Porcentaje = 35, Mensaje = 'Cuello de botella en la GPU (La CPU exige demasiado)' 
    ; Diferencia < -20 -> 
        Porcentaje = 45, Mensaje = 'Cuello de botella en la CPU (La GPU esta ociosa)' 
    ; 
        Porcentaje = 0, Mensaje = 'Configuracion equilibrada ideal'
    ).

% Regla para sugerir los 3 juegos base según la gama de la GPU elegida
juegos_sugeridos(GamaGPU, JuegoBajo, JuegoMedio, JuegoAlto) :-
    juego(JuegoBajo, baja),
    juego(JuegoMedio, media),
    juego(JuegoAlto, alta).