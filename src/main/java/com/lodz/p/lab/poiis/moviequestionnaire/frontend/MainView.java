package com.lodz.p.lab.poiis.moviequestionnaire.frontend;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lodz.p.lab.poiis.moviequestionnaire.backend.CsvInputUtils;
import com.lodz.p.lab.poiis.moviequestionnaire.backend.entity.Input;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.Route;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Route
public class MainView extends VerticalLayout {

    private static final String MOVIE_CSV_PATH = "./src/main/resources/static/movies.csv";

    public MainView() {
        add(new Button("Click me", buttonClickEvent -> Notification.show("Clicked!")));

        List<Input> inputs = CsvInputUtils.read(MOVIE_CSV_PATH);

        String TMDB_URL = "https://api.themoviedb.org/3/movie/";
        String API_KEY = "?api_key=3b06cc2e3d595ab056a5c1846175d9a8&language=en-US";
        String POSTER_PRE_URL = "http://image.tmdb.org/t/p/w185/";

        Input input = inputs.get(147);
        var id = input.getId();
        var tmdbId = input.getTmdbId();
        var title = input.getTitle();

        var requestUrl = TMDB_URL + tmdbId + API_KEY;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response
                = restTemplate.getForEntity(requestUrl, String.class);

        JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
        JsonElement jsonElement = jsonObject.get("poster_path");

        Label idLabel = new Label("ID: " + id);
        Label titleLabel = new Label("Title: " + title);

        add(idLabel, titleLabel);

        Image image = new Image(POSTER_PRE_URL + jsonElement.getAsString(), "Poster");
        add(image);

        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setLabel("How do you rate me?");
        group.setItems("Not Seen", "0", "1", "2", "3", "4", "5");
        add(group);

        Button nextBtn = new Button("Next");
        add(nextBtn);

    }
}
