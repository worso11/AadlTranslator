package lukowicz.application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lukowicz.application.aadl.ElementSearcher;
import lukowicz.application.petrinet.PetriNetGenerator;
import lukowicz.application.petrinet.PetriNetGraphicsGenerator;
import lukowicz.application.petrinet.PetriNetPager;
import lukowicz.application.petrinet.PetriNetTranslator;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApplicationRunner extends Application {

    private Desktop desktop = Desktop.getDesktop();

    public static void main(String[] args) {
        launch();
    }

    public static void startProgram(File aadlFile) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        PetriNetPager petriNetPager = new PetriNetPager();
        ElementSearcher elementSearcher = new ElementSearcher(petriNetPager);
        PetriNetGraphicsGenerator petriNetGraphicsGenerator = new PetriNetGraphicsGenerator(petriNetPager);
        PetriNetTranslator petriNetTranslator = new PetriNetTranslator(petriNetGraphicsGenerator);
        PetriNetGenerator petriNetGenerator = new PetriNetGenerator(petriNetGraphicsGenerator, petriNetTranslator, elementSearcher, petriNetPager);
        Parser parser = new Parser(elementSearcher, petriNetGenerator);
        parser.parseFile(aadlFile);
    }

    @Override
    public void start(Stage stage)  {
        stage.setTitle("AADL to PetriNet Translator");
        stage.setHeight(120);
        stage.setWidth(420);

        final FileChooser fileChooser = new FileChooser();

        final Text textField = new Text("Choose AADL-XML file to translate");
        final Button openButton = new Button("Choose file");

        openButton.setOnAction(
                (e) -> {
                    configureFileChooser(fileChooser);
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        openFile(file);
                        try {
                            startProgram(file);
                            stage.hide();
                        } catch (ParserConfigurationException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (SAXException ex) {
                            ex.printStackTrace();
                        } catch (TransformerException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
        );

        final GridPane inputGridPane = new GridPane();

        GridPane.setConstraints(textField,0,0);
        GridPane.setConstraints(openButton, 1, 0);

        inputGridPane.setHgap(6);
        inputGridPane.setVgap(6);
        inputGridPane.getChildren().addAll(openButton,textField);

        final Pane rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(inputGridPane);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));


        stage.setScene(new Scene(rootGroup));
        stage.show();
    }

    private static void configureFileChooser(final FileChooser fileChooser){
        fileChooser.setTitle("View Pictures");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
    }


    private void openFile(File file) {
        try {
            desktop.open(file);
        } catch (IOException ex) {
            Logger.getLogger(
                    ApplicationRunner.class.getName()).log(
                    Level.SEVERE, null, ex
            );
        }
    }
}
