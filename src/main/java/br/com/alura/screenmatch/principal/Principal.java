package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados converteDados = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=4f39ef6";
    private final String SEASON = "&season=";


    public void exibeMenu(){
        System.out.println("digite o nome da serie:");
        var nomeSerie = leitura.nextLine();
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        var dadosSerie = converteDados.obeterDados(json, DadosSerie.class);
        System.out.println(dadosSerie);

        List<DadosTemporada> dadosTemporadaList = new ArrayList<>();
        for(int i=1; i<=dadosSerie.totalTemporadas();i++){
            json = consumoApi.obterDados(ENDERECO+nomeSerie.replace(" ", "+")+SEASON+i+API_KEY);
            dadosTemporadaList.add(converteDados.obeterDados(json, DadosTemporada.class));
        }
        for(DadosTemporada temporada : dadosTemporadaList){
            System.out.println(temporada);
        }

        dadosTemporadaList.forEach(temporada -> System.out.println(temporada));

        dadosTemporadaList.forEach(System.out::println);

        dadosTemporadaList.stream().filter(temporada -> temporada.numero() %2 ==0).forEach(temporada -> System.out.println(temporada));

        for(int i=0; i<dadosSerie.totalTemporadas();i++){
            List<DadosEpisodio> episodios = dadosTemporadaList.get(i).episodios();
            for(int e=0; e< episodios.size();e++){
                System.out.println(episodios.get(e).titulo());
            }
        }
        dadosTemporadaList.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        System.out.println("--------------");

        List<DadosEpisodio> dadosEpisodioList = dadosTemporadaList.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

//        dadosEpisodioList.stream()
//                //.filter(e -> e.avaliacao().matches("-?\\d+"))
//                .sorted()
//                .limit(5)
//                .forEach(System.out::println);

        dadosEpisodioList.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);

        List<Episodio> episodios = dadosTemporadaList.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);

        System.out.println("informa um ano para sua busca: ");
        Integer ano = leitura.nextInt();
        LocalDate dataBusca = LocalDate.of(ano, 1, 1);

        DateTimeFormatter formataData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream()
                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println("Temporada: " + e.getNumeroTemporada() +
                        " Episódio: " + e.getTitulo() +
                        " Data lançamento: " + e.getDataLancamento().format(formataData))
                );

//        List<String> nome = new ArrayList<>(Arrays.asList("bruno", "maria", "natalia", "teste"));
//
//        nome.stream()
//                .sorted()
//                .filter(n -> n.startsWith("n"))
//                .forEach(System.out::println);

    }
}
