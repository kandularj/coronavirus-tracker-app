package com.portfolio.coronavirustracker.services;

import com.portfolio.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    private static String COVID_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    /*public void setAllStats(List<LocationStats> allStats) {
        this.allStats = allStats;
    }*/

            @PostConstruct
            @Scheduled(cron = "* * 1 * * *")//Tells the below method to run every second

            public void fetchCovidData() throws IOException, InterruptedException {

                List<LocationStats> newStats = new ArrayList<>();//creating another instance as running the method clears the output everytime and there will be data in allStats which can be printed to output
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(COVID_DATA_URL)).build();
                HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(httpResponse.body());
                //code to read csv file and print it in certain format
                StringReader csvBodyReader = new StringReader(httpResponse.body());
                Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
                for (CSVRecord record : records) {
                    LocationStats locationStat = new LocationStats();
                    locationStat.setState(record.get("Province/State"));
                    locationStat.setCountry(record.get("Country/Region"));
                    int latestCases = Integer.parseInt(record.get(record.size() - 1));
                    int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
                    locationStat.setLatestTotalCases(latestCases);
                    locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
                    newStats.add(locationStat);
                }
                this.allStats = newStats;
            }
}
