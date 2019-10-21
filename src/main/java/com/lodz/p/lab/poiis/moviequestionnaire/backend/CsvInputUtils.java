package com.lodz.p.lab.poiis.moviequestionnaire.backend;

import com.lodz.p.lab.poiis.moviequestionnaire.backend.entity.Input;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvInputUtils {

    @SuppressWarnings("unchecked")
    public static List<Input> read(String path) {
        List<Input> inputs = new ArrayList<>();
        try {
            Reader reader = Files.newBufferedReader(Paths.get(path));
            ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
            strategy.setType(Input.class);
            String[] memberFieldsToBindTo = {"id", "tmdbId", "title"};
            strategy.setColumnMapping(memberFieldsToBindTo);

            CsvToBean<Input> csvToBean = new CsvToBeanBuilder(reader)
                    .withSeparator(';')
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            inputs = csvToBean.parse();
        } catch (IOException e) {
            log.error("Exception occurred: ", e);
        }

        return inputs;
    }
}
