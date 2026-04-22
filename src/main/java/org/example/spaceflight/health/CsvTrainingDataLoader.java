package org.example.spaceflight.health;

import org.example.spaceflight.model.ExperienceMode;
import org.example.spaceflight.model.Gender;
import org.example.spaceflight.model.HealthStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/** Loads labelled training cases from the bundled training_data.csv resource. */
public class CsvTrainingDataLoader implements ITrainingDataLoader {

    private static final Logger log = Logger.getLogger( MethodHandles.lookup( ).lookupClass( ).getName( ) );

    private static final String CSV_PATH = "/training_data.csv";

    @Override
    public List<TrainingCase> load() {
        List<TrainingCase> result = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(CSV_PATH)) {
            if (is == null) {
                log.warning("training_data.csv not found – kNN classifier will have no training data");
                return result;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("bpm")) continue;
                TrainingCase tc = parseLine(line);
                if (tc != null) result.add(tc);
            }
        } catch (IOException e) {
            log.severe("Failed to load training_data.csv: " + e.getMessage());
        }
        log.info("Loaded " + result.size() + " training cases from " + CSV_PATH);
        return result;
    }

    private TrainingCase parseLine(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 9) return null;

            double bpm  = Double.parseDouble(parts[0].trim());
            double spo2 = Double.parseDouble(parts[1].trim());
            double sys  = Double.parseDouble(parts[2].trim());
            double dias = Double.parseDouble(parts[3].trim());
            double rr   = Double.parseDouble(parts[4].trim());
            AgeGroup ag        = AgeGroup.valueOf(parts[5].trim());
            Gender g           = Gender.valueOf(parts[6].trim());
            ExperienceMode m   = ExperienceMode.valueOf(parts[7].trim());
            HealthStatus label = HealthStatus.valueOf(parts[8].trim());

            return new TrainingCase(bpm, spo2, sys, dias, rr, ag, g, m, label);
        } catch (Exception e) {
            log.warning("Skipping malformed CSV line: " + line);
            return null;
        }
    }
}
