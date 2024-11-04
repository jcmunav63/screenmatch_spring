package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.DatosEpisodio;
import com.aluracursos.screenmatch.model.DatosSerie;
import com.aluracursos.screenmatch.model.DatosTemporada;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
        System.out.println(json);
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
        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));
    }
}
