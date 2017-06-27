package es.tamarit.emtwidget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

public class CommandBuilder {

    @Test
    public void searchService() throws IOException {

        String command = "sc query AxInstSV";

        Process process = Runtime.getRuntime().exec("cmd /c " + command);

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        String result = builder.toString();

        System.out.println(result);
    }
}
