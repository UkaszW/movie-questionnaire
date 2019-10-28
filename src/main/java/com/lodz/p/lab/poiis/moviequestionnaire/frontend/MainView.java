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
import com.vaadin.flow.component.select.Select;
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
    private static final String API_KEY = "?api_key=3b06cc2e3d595ab056a5c1846175d9a8";
    private static final String PL = "&language=pl-PL";
    private static final String EN = "&language=en-US";
    private static final String POSTER_PRE_URL = "http://image.tmdb.org/t/p/w185/";
    private static final String NOT_SEEN = "Not Seen";

    @Autowired
    private ResultRepository resultRepository;

    public MainView() {
        Label welcomeLabel = new Label("Welcome!");

        Select<String> language = new Select<>();
        language.setLabel("Choose language");
        language.setItems("English", "Polish");
        language.addValueChangeListener(event -> renderStartPage(event.getValue()));
        add(welcomeLabel, language);
    }

    private void renderStartPage(String language) {
        removeAll();
        String personId = RandomStringUtils.random(10, true, true);

        List<Input> inputs = CsvInputUtils.read(MOVIE_CSV_PATH);

        Label startLabelPl = new Label("Zapraszamy do niezapomnianej przygody. Proszimy oceniaj uważnie.\n");
        Label startLabelEng = new Label("Welcome to unforgetable journey ;) Please rate every movie carefully.\n");

        String welcomeTxtPl = "Oceń każdy obejrzany film, od 0 do 5. Jeśli nie widzialeś/widziałaś danego filmu, wybierz opcje \'Nie widziałem/widziałam\' " +
                "Ankieta potrwa około 15-20 minut. Jeśli jesteś gotowy naciśnij poniższy przycisk. " +
                "Z góry dziękujemy.";

        String welcomeTxtEng = "Rate every movie from 0 to 5 if you have seen it. If not, choose \'Not Seen\' option. " +
                "The Questionnaire takes circa 15-20 minutes. If you are ready, click the button below. " +
                "Thank you in advance.";

        String startedEng = "Get Started";
        String startedPl = "Zaczynamy";

        if (language.equals("English")) {
            Label surveyDesc = new Label(welcomeTxtEng);
            Button startedBtn = new Button(startedEng, buttonClickEvent -> renderNext(inputs.iterator(), personId, language));
            startedBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            add(startLabelEng, surveyDesc, startedBtn);
        } else {
            Label surveyDesc = new Label(welcomeTxtPl);
            Button startedBtn = new Button(startedPl, buttonClickEvent -> renderNext(inputs.iterator(), personId, language));
            startedBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            add(startLabelPl, surveyDesc, startedBtn);
        }

    }

    private void renderNext(Iterator<Input> iterator, String personId, String language) {
        removeAll();
        if (iterator.hasNext()) {
            Input input = iterator.next();
            var id = input.getId();
            var tmdbId = input.getTmdbId();
            var languageVar = language.equals("English") ? EN : PL;

            var requestUrl = TMDB_URL + tmdbId + API_KEY + languageVar;

            try {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);

                Optional.ofNullable(response.getBody()).ifPresentOrElse(it -> {
                    JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
                    JsonElement posterPath = jsonObject.get("poster_path");
                    JsonElement description = jsonObject.get("overview");
                    JsonElement title = jsonObject.get("title");

                    String titleLabel2 = language.equals("English") ? "Title: " : "Tytuł: ";
                    Label idLabel = new Label("ID: " + id + "/200");
                    Label titleLabel = new Label(titleLabel2 + title);
                    add(idLabel, titleLabel);

                    Image image = new Image(POSTER_PRE_URL + posterPath.getAsString(), "Poster");
                    Label desc = new Label(description.getAsString());
                    add(image, desc);
                }, () -> new Label("Upps.. TMDB service not available!"));

                RadioButtonGroup<String> group = new RadioButtonGroup<>();
                String groupLabel = language.equals("English") ? "How do you rate me?" : "Jak mnie oceniasz?";
                group.setLabel(groupLabel);
                String notSeenTxt = language.equals("English") ? "Not Seen" : "Nie widziałem/widziałam";
                group.setItems(notSeenTxt, "0", "1", "2", "3", "4", "5");
                group.addValueChangeListener(event -> {
                    saveResult(iterator, personId, id, group);
                    renderNext(iterator, personId, language);
                });
                add(group);
            } catch (Exception e) {
                add(new Label("Upps.. Something goes wrong! Please, try later."));
                log.error("Exception occurred: ", e);
            }
        } else {
            String endLabelTxt = language.equals("English") ? "Thank you for your time!" : "Dziękujemy za poświęcony czas!";
            Label endLabel = new Label(endLabelTxt);
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
