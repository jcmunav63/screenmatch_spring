package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.DatosEpisodio;
import com.aluracursos.screenmatch.model.DatosSerie;
import com.aluracursos.screenmatch.model.DatosTemporada;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=a7f1d8ad";

    private ConvierteDatos conversor = new ConvierteDatos();

    public void muestraElMenu() {
        System.out.println("Escribe el nombre de la serie que deseas buscar: ");
        // Busca los datos generales de la Serie
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        // System.out.println(json);
        var datos = conversor.obtenerDatos(json, DatosSerie.class);
        System.out.println("Datos generales de la Serie: " + datos);

        // Buscar datos de todas las temporadas y agrega en una Lista
        List<DatosTemporada> temporadas = new ArrayList<>();
        System.out.println("Datos de las temporadas...");
        for (int i = 1; i <= datos.totalDeTemporadas() ; i++) {
            json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + "&Season=" + i + API_KEY);
            var datosTemporadas = conversor.obtenerDatos(json, DatosTemporada.class);
            temporadas.add(datosTemporadas);
        }
        // temporadas.forEach(System.out::println);

        // Mostrar sólo los títulos de los episodios para las temporadas encontradas
        for (int i = 0; i < datos.totalDeTemporadas(); i++) {
            System.out.println("  Temporada " + (i+1));
            System.out.println("  ------------");
            List<DatosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
            for (int j = 0; j < episodiosTemporada.size(); j++) {
                System.out.println("Episodio "+ (j+1)+ ":  " + episodiosTemporada.get(j).titulo());
            }
            System.out.println(" ");
        }
        System.out.println(" =============");
//        System.out.println(" ");
//        System.out.println("Lista de episodios (sólo el nombre)");
//        System.out.println("-----------------------------------");
//        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        // Convertir toda la información de episodios a una lista del tipo DatosEpisodio
        List<DatosEpisodio> datosEpisodios = temporadas.stream()
                .flatMap(t-> t.episodios().stream())
                        .collect(Collectors.toList());

        // Utilizando 'peek' en los streams (clase 4)
        // Hallar el top 5 de episodios
        System.out.println(" ");
        System.out.println("Lista del top 5 de episodios de todas las temporadas...");
        System.out.println("----------------------------------------------------");
        datosEpisodios.stream()
                .filter(e-> !e.evaluacion().equalsIgnoreCase("N/A"))
                .peek(e -> System.out.println("Primer filtro (N/A) " + e))
                .sorted(Comparator.comparing(DatosEpisodio::evaluacion).reversed())
                .peek(e -> System.out.println("Segundo filtro (ordenación) " + e))
                .map(e -> e.titulo().toUpperCase())
                .peek(e -> System.out.println("Tercer filtro (mayúsculas) " + e))
                .limit(5)
                .forEach(System.out::println);

        // Convirtiendo los datos a una Lista del tipo Episodio
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t-> t.episodios().stream()
                        .map(d-> new Episodio(t.numero(),d)))
                .collect(Collectors.toList());
        System.out.println(" ");
        System.out.println("Temporadas y Episodios");
        System.out.println("----------------------");
        episodios.forEach(System.out::println);

        // Búsqueda de episodios a partir de un año específico
//        System.out.println("Indica el año a partir del cual desea buscar episodios...");
//        var fecha = teclado.nextInt();
//        teclado.nextLine();
//
//        LocalDate fechaBusqueda = LocalDate.of(fecha, 1, 1);
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodios.stream()
//                .filter(e-> e.getFechaDeLanzamiento() != null &&
//                        e.getFechaDeLanzamiento().isAfter(fechaBusqueda))
//                .forEach(e-> System.out.println(
//                        "Temporada: " + e.getTemporada() +
//                                " | Episodio: " + e.getTitulo() +
//                                " | Fecha lanz.: " + e.getFechaDeLanzamiento().format(dtf)
//                ));

        // Buscar episodio por una parte del título
//        System.out.println("Escribe por favor el título o parte del título del episodio que deseas buscar: ");
//        var parteTitulo = teclado.nextLine();
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase().contains(parteTitulo.toUpperCase()))
//                .findFirst();
//        if(episodioBuscado.isPresent()) {
//            System.out.println("El primer episodio que coincide con la búsqueda es: ");
//            System.out.println(episodioBuscado.get());
//        } else {
//            System.out.println("El episodio no fue encontrado.");
//        }

        // Implementando un Mapa de Datos por Temporada
        Map<Integer, Double> evaluacionesPorTemporada = episodios.stream()
                .filter(e -> e.getEvaluacion() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getEvaluacion)));
        System.out.println(" ");
        System.out.println("  ------------------------------- ");
        System.out.println(" ");
        System.out.println("Mapa de Evaluación por Temporada");
        System.out.println(evaluacionesPorTemporada);

        // Recolectando Estadísticas
        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getEvaluacion() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));
        // System.out.println("Estadísticas de evaluación: " + est);
        System.out.println(" ");
        System.out.println("  ------------------------------- ");
        System.out.println("Estadísticas de evaluación");
        System.out.println("Promedio: " + est.getAverage());
        System.out.println("Conteo: " + est.getCount());
        System.out.println("Ev. Mínima: " + est.getMin());
        System.out.println("Ev. Máxima: " + est.getMax());
    }
}
