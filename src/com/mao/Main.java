package com.mao;

import com.audio.AudioDevice;
import com.audio.Common;
import com.dsss.Decoder;
import com.dsss.Encoder;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.plaf.nimbus.State;
import java.io.File;
import java.util.ArrayList;

public class Main extends Application {

    Stage currentState;

    void draw_waveForm(GraphicsContext gc,File file,ArrayList<Float> spread) throws Exception {

        float width = 1200.0f;
        int height = 80;
        int halfHeight = height / 2;

        int scale = halfHeight;
        float f = 1;


        ArrayList<Float> wave = new ArrayList<>();
        if(file != null){
            wave  = Common.getWaveFromAudio(file);
            f = width / wave.size();
        }
        if(spread != null){
            scale = 10;
            f = 2;

            wave = spread;
        }



        gc.clearRect(0,0,width,height);

        gc.setFill(Color.RED);
        gc.setLineWidth(1);


        float x = 0;
        for(int i =0 ;i < wave.size()-500; i++){
            gc.strokeLine(x, halfHeight, x, halfHeight + wave.get(i) * scale);
            x += f;
        }

    }

    Tab initTabEncode() throws Exception {
        Pane root = new Pane();


        Label passwordLabel = new Label("Your Password: ");
        passwordLabel.setLayoutX(20);
        passwordLabel.setLayoutY(20);

        TextField passTextField = new TextField();
        passTextField.setLayoutX(20);
        passTextField.setLayoutY(40);
        passTextField.setMinWidth(100);


        Label messageLabel = new Label("Your Secret Message: ");
        messageLabel.setLayoutX(20);
        messageLabel.setLayoutY(80);



        TextArea textArea = new TextArea();
        textArea.setLayoutX(20);
        textArea.setLayoutY(100);
        textArea.setMinWidth(100);

        Label fileLabel = new Label("No File Selected");
        fileLabel.setLayoutX(800);
        fileLabel.setLayoutY(60);
        Button selectFileBtn = new Button("Select Audio");
        selectFileBtn.setLayoutX(800);
        selectFileBtn.setLayoutY(100);

        Button encodeButton = new Button("Start Encode");
        encodeButton.setLayoutX(800);
        encodeButton.setLayoutY(160);
        encodeButton.setDisable(true);

        Button originalSoundSignalBtn = new Button("Play Original Signal");
        originalSoundSignalBtn.setLayoutX(20);
        originalSoundSignalBtn.setLayoutY(320);
        originalSoundSignalBtn.setOpacity(0);
        Button spreadSoundSignalBtn = new Button("Play Spread Signal");
        spreadSoundSignalBtn.setLayoutX(20);
        spreadSoundSignalBtn.setLayoutY(420);
        spreadSoundSignalBtn.setOpacity(0);
        Button encodeSoundSignalBtn = new Button("Play Encoded Signal");
        encodeSoundSignalBtn.setLayoutX(20);
        encodeSoundSignalBtn.setLayoutY(520);
        encodeSoundSignalBtn.setOpacity(0);

        Canvas originalSoundCanvas = new Canvas(1200.0f, 80);
        originalSoundCanvas.setLayoutX(20);
        originalSoundCanvas.setLayoutY(340);
        Canvas spreadSignalCanvas = new Canvas(1200.0f, 80);
        spreadSignalCanvas.setLayoutX(20);
        spreadSignalCanvas.setLayoutY(440);
        Canvas encodeSignalCanvas = new Canvas(1200.0f, 80);
        encodeSignalCanvas.setLayoutX(20);
        encodeSignalCanvas.setLayoutY(540);

        GraphicsContext gc_originalSignal = originalSoundCanvas.getGraphicsContext2D();
        GraphicsContext gc_spreadSignal = spreadSignalCanvas.getGraphicsContext2D();
        GraphicsContext gc_encodeSignal = encodeSignalCanvas.getGraphicsContext2D();

        final File[] selectedFile = new File[1];

        selectFileBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fil_chooser = new FileChooser();
                File file = fil_chooser.showOpenDialog(currentState);
                if (file != null) {
                    fileLabel.setText("Selected: " + file.getAbsolutePath());
                    selectedFile[0] = file;
                    originalSoundSignalBtn.setOpacity(1);
                    spreadSoundSignalBtn.setOpacity(0);
                    encodeSoundSignalBtn.setOpacity(0);
                    gc_spreadSignal.clearRect(0,0,1200,80);
                    gc_encodeSignal.clearRect(0,0,1200,80);

                    originalSoundSignalBtn.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            try {
                                AudioDevice.playSample(Common.getWaveFromAudio(file));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    if(!passTextField.getText().isEmpty() && !textArea.getText().isEmpty()){
                        encodeButton.setDisable(false);
                    }

                    try {
                        draw_waveForm(gc_originalSignal,file,null);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        passTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if(!newValue.isEmpty() && selectedFile[0] != null){
                    encodeButton.setDisable(false);
                }
            }
        });
        textArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if(!newValue.isEmpty() && selectedFile[0] != null){
                    encodeButton.setDisable(false);
                }
            }
        });

        encodeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String message = textArea.getText();
                Long key = Long.parseLong(passTextField.getText());
                File file = selectedFile[0];
                Encoder encoder = new Encoder(message,key,file);
                try {
                    encoder.encode();
                    String encodeFile = encoder.outputAudio(currentState);
                    if(encodeFile != null && !encodeFile.isEmpty()){
                        spreadSoundSignalBtn.setOpacity(1);
                        encodeSoundSignalBtn.setOpacity(1);

                        spreadSoundSignalBtn.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                try {
                                    AudioDevice.playSample(encoder.getSpreadSignal());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        encodeSoundSignalBtn.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                try {
                                    AudioDevice.playSample(Common.getWaveFromAudio(new File(encodeFile)));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });


                        draw_waveForm(gc_spreadSignal,null,encoder.getSpreadSignal());
                        draw_waveForm(gc_encodeSignal,new File(encodeFile),null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        root.getChildren().addAll(messageLabel,textArea,fileLabel,selectFileBtn,encodeButton,originalSoundCanvas,
                passwordLabel,passTextField,originalSoundSignalBtn,spreadSignalCanvas,encodeSignalCanvas,
                spreadSoundSignalBtn,encodeSoundSignalBtn);

        Tab tab = new Tab("Encode", root);
        return tab;
    }

    Tab initTabDecode(){
        Label fileLabel = new Label("No File Selected");
        Button selectFileBtn = new Button("Select Audio");
        Label passwordLabel = new Label("Password: ");
        TextField passTextField = new TextField();
        passTextField.setMinWidth(100);

        TextArea textArea = new TextArea();
        textArea.setMinWidth(100);
        textArea.setMinHeight(100);
        Button encodeButton = new Button("Start Decode");

        encodeButton.setOpacity(0);
        passTextField.setOpacity(0);
        passwordLabel.setOpacity(0);
        textArea.setOpacity(0);


        selectFileBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fil_chooser = new FileChooser();
                File file = fil_chooser.showOpenDialog(currentState);
                if (file != null) {
                    fileLabel.setText("Selected: " + file.getAbsolutePath());

                    passTextField.setOpacity(1);
                    passwordLabel.setOpacity(1);
                    encodeButton.setOpacity(1);

                    encodeButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            long key = Long.parseLong(passTextField.getText());
                            Decoder decoder = new Decoder(file,key);
                            try {
                                decoder.decode();
                                textArea.setText(decoder.getDecodedMessage());
                                textArea.setOpacity(1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }
        });

        VBox vbox = new VBox(30, fileLabel,selectFileBtn,passwordLabel,passTextField,encodeButton,textArea);
        vbox.setAlignment(Pos.CENTER);


        Tab tab = new Tab("Decode", vbox);
        return tab;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        this.currentState = primaryStage;


        // create a Label
        Label label = new Label("Nguyen Van Mao - https://github.com/nvmao");
        label.setFont(new Font(50));

        // create a Button



        VBox vbox = new VBox(30, label);

        // set Alignment
        vbox.setAlignment(Pos.CENTER);

        // create a scene

        TabPane tabPane = new TabPane();

        VBox authorVBox = new VBox(30,label);
        authorVBox.setAlignment(Pos.CENTER);

        Tab tab1 = this.initTabEncode();
        Tab tab2 = this.initTabDecode();
        Tab tab3 = new Tab("Author" , authorVBox);
        tab1.setClosable(false);
        tab2.setClosable(false);
        tab3.setClosable(false);


        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);
        tabPane.getTabs().add(tab3);

        VBox vBox = new VBox(tabPane);
        Scene scene = new Scene(vBox,1280,720);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Hide Message In Audio - (This only supports 16-bit signal stereo and mono Wav files with a sampling rate of 44100)");

        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
