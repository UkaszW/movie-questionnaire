package com.lodz.p.lab.poiis.moviequestionnaire.frontend;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lodz.p.lab.poiis.moviequestionnaire.backend.CsvInputUtils;
import com.lodz.p.lab.poiis.moviequestionnaire.backend.entity.Input;
import com.lodz.p.lab.poiis.moviequestionnaire.backend.entity.Result;
import com.lodz.p.lab.poiis.moviequestionnaire.backend.repository.ResultRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Route
public class MainView extends VerticalLayout {

    private static final String MOVIE_CSV_PATH = "./src/main/resources/static/movies.csv";
    private static final String TMDB_URL = "https://api.themoviedb.org/3/movie/";
    private static final String API_KEY = "?api_key=3b06cc2e3d595ab056a5c1846175d9a8&language=en-US";
    private static final String POSTER_PRE_URL = "http://image.tmdb.org/t/p/w185/";
    private static final String NOT_SEEN = "Not Seen";

    @Autowired
    private ResultRepository resultRepository;

    public MainView() {
        String personId = RandomStringUtils.random(10, true, true);

        List<Input> inputs = CsvInputUtils.read(MOVIE_CSV_PATH);

        Label startLabel = new Label("Welcome to unforgetable journey ;) Please rate every movie carefully.");
        Button startedBtn = new Button("Get Started", buttonClickEvent -> {
            renderNext(inputs.iterator(), personId);
        });
        startedBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(startLabel, startedBtn);
    }

    private void renderNext(Iterator<Input> iterator, String personId) {
        if (iterator.hasNext()) {
            removeAll();

            Input input = iterator.next();
            var id = input.getId();
            var tmdbId = input.getTmdbId();
            var title = input.getTitle();

            var requestUrl = TMDB_URL + tmdbId + API_KEY;

            try {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);

                Label idLabel = new Label("ID: " + id + "/200");
                Label titleLabel = new Label("Title: " + title);

                add(idLabel, titleLabel);

                Optional.ofNullable(response.getBody()).ifPresentOrElse(it -> {
                    JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
                    JsonElement jsonElement = jsonObject.get("poster_path");

                    Image image = new Image(POSTER_PRE_URL + jsonElement.getAsString(), "Poster");
                    add(image);
                }, () -> new Label("Upps.. Poster service not available!"));

                RadioButtonGroup<String> group = new RadioButtonGroup<>();
                group.setLabel("How do you rate me?");
                group.setItems(NOT_SEEN, "0", "1", "2", "3", "4", "5");
                add(group);

                Button nextBtn = new Button("Next", buttonClickEvent -> {
                    saveResult(iterator, personId, id, group);
                    renderNext(iterator, personId);
                });
                add(nextBtn);
            } catch (Exception e) {
                add(new Label("Upps.. Something goes wrong! Please, try later."));
                log.error("Exception occurred: ", e);
            }
        } else {
            Label endLabel = new Label("Thank you for your time!");
            add(endLabel);
        }
    }

    private void saveResult(Iterator<Input> iterator, String personId, Long id, RadioButtonGroup<String> group) {
        Integer evaluate = Objects.isNull(group.getValue()) || NOT_SEEN.equals(group.getValue())
                ? null : Integer.parseInt(group.getValue());
        Result result = new Result(id, personId, evaluate);
        saveRate(result);
    }

    private void saveRate(Result result) {
        resultRepository.save(result);
    }
}
