package com.example.tracklist.tracklist_api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.channels.Pipe.SourceChannel;
import java.util.ArrayList;
import java.util.List;


@RestController
public class AudioRecognitionController{
    static class YouTubeUrlRequest{
        private String url;
        private String cookiesFile;

        public String getUrl(){
            return url;
        }

        public void setUrl(String url){
            this.url = url;
        }

        public String getCookiesFile() {
            return cookiesFile;
        }

        public void setCookiesFile(String cookiesFile) {
            this.cookiesFile = cookiesFile;
        }

    }

    @PostMapping("/recognize-youtube-audio")
    public ResponseEntity<String> recognizeYoutubeAudio(@RequestBody YouTubeUrlRequest request){
        String youtubeUrl = request.getUrl();

        if(youtubeUrl == null || youtubeUrl.trim().isEmpty()){
            return new ResponseEntity<>("Error: YouTube video ID is required.", HttpStatus.BAD_REQUEST);
        }

        System.out.println("Received request to recognize audio for video ID: " + youtubeUrl);
        //try downloading the video with yt-dlp
        try{
            List<String> command = new ArrayList<>();
            command.add("yt-dlp");
            command.add("-x");
            command.add("--audio-format");
            command.add("mp3");
            command.add("-o");
            command.add("videos/%(title)s.%(ext)s");

            //add cookies file for authentication
            if (request.getCookiesFile() != null && !request.getCookiesFile().trim().isEmpty()) {
                command.add("--cookies");
                command.add(request.getCookiesFile());
            }


            command.add(youtubeUrl);

            ProcessBuilder processBuilder = new ProcessBuilder(command);

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            //read output from command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder out = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                out.append(line).append("\n");
            }

            //halt until download complete
            int exitCode = process.waitFor();

            if(exitCode == 0){
                String successMsg = "Audio successfully downloaded: " + youtubeUrl + ".\nCommand Output:\n" + out;
                System.out.println(successMsg);
                return new ResponseEntity<>(successMsg, HttpStatus.OK);
            }
            else{
                String errorMsg = "Failed to download audio from URL: " + youtubeUrl + ".\nCommand Output:\n" + out;
                System.err.println(errorMsg);
                return new ResponseEntity<>(errorMsg, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity<>("Error processing URL: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}