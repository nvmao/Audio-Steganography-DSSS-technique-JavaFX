package com.dsss;


import com.audio.AudioDevice;
import com.audio.Common;
import com.binary.BinaryTool;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;

public class Encoder {

    private String message;
    private File originalAudioFile;
    private long key;

    public ArrayList<Float> getSamplesWave() {
        return samplesWave;
    }

    public void setSamplesWave(ArrayList<Float> samplesWave) {
        this.samplesWave = samplesWave;
    }

    public ArrayList<Float> getSpreadSignal() {
        return spreadSignal;
    }

    public void setSpreadSignal(ArrayList<Float> spreadSignal) {
        this.spreadSignal = spreadSignal;
    }

    private ArrayList<Float> samplesWave;
    private ArrayList<Float> spreadSignal;

    public Encoder(String message,long key, File originalAudioFile) {
        this.message = message.trim();
        if(this.message.length() % 2 != 0){
            this.message += " ";
        }
        this.key = key;
        this.originalAudioFile = originalAudioFile;
    }

    public void encode() throws Exception {

        int[] message_binary = BinaryTool.convertStringToBinary(message);
        System.out.println("message: ");
        for(int i =0 ; i < message_binary.length;i++){
            System.out.print(message_binary[i]);
        }
        System.out.println();

        for(int i =0 ; i < message_binary.length;i++){
            if(message_binary[i] == 0){
                message_binary[i] = -1;
            }
            System.out.print(message_binary[i]);
        }
        System.out.println();

        ArrayList<Float> samplesWave = Common.getWaveFromAudio(originalAudioFile);

        System.out.println("wave: ");
        for(float f : samplesWave){
            System.out.print(f + " ");
        }
        System.out.println();

        System.out.println(samplesWave.size());
        System.out.println(message_binary);

        int[] pnSequence = Common.pnSequenceKey(key,samplesWave.size());

        System.out.println("pn random from key: ");
        for(int o : pnSequence){
            System.out.print(o);
        }
        System.out.println();

        System.out.println(pnSequence.length);

        int num_per_character = samplesWave.size() / message_binary.length;

        System.out.println("wave size : " + samplesWave.size());
        System.out.println("message size: " + message_binary.length);
        System.out.println("num per char: " + num_per_character);

        ArrayList<Integer> spreadSequences = new ArrayList<Integer>();

        int current_pn = 0;
        for(int i = 0; i < message_binary.length;i++){

            int data = message_binary[i];

            for(int j = 0; j < num_per_character;j++){
                int spread = data * pnSequence[current_pn];
                current_pn++;
                spreadSequences.add(spread);
            }
        }

        System.out.println("prev spread sequences size: " + spreadSequences.size());

        if(spreadSequences.size() < samplesWave.size()){
            int diff = samplesWave.size() - spreadSequences.size();
            for(int i = 0; i < diff;i++){
                spreadSequences.add(1);
            }
        }
        System.out.println("spread sequences size: " + spreadSequences.size());
        for(int i = 0; i < spreadSequences.size();i++){
//            System.out.print(spreadSequences.get(i) + " ");
        }
        System.out.println();

        // embed
        for(int i =0 ; i < samplesWave.size();i++){
            if(spreadSequences.get(i) == -1){
                samplesWave.set(i,0.000001f);
            }
        }


        System.out.println("sample waves: ");
        for(int i = 0; i < 10;i++){
            System.out.print(samplesWave.get(i) + " ");
        }
        System.out.println();

//        for(int i = 0 ; i < samplesWave.size();i++){
//            if(spreadSequences.get(i) == 0){
//                samplesWave.set(i,0.0f);
//            }
//        }

        this.samplesWave = samplesWave;
        this.spreadSignal = new ArrayList<>();
        for(int i = 0 ; i < spreadSequences.size();i++){
            spreadSignal.add((float)spreadSequences.get(i));
        }
    }

    public String outputAudio(Stage stage) throws Exception {
        AudioDevice device = new AudioDevice();

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(stage);

        device.writeToFile(this.samplesWave,file);

        return file.getAbsolutePath();
    }

}
