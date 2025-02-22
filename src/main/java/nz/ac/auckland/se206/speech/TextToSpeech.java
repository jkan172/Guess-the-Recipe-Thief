package nz.ac.auckland.se206.speech;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javafx.concurrent.Task;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.apiproxy.tts.TextToSpeechRequest;
import nz.ac.auckland.apiproxy.tts.TextToSpeechRequest.Provider;
import nz.ac.auckland.apiproxy.tts.TextToSpeechRequest.Voice;
import nz.ac.auckland.apiproxy.tts.TextToSpeechResult;
import nz.ac.auckland.se206.GameStateContext;

/** A utility class for converting text to speech using the specified API proxy. */
public class TextToSpeech {

  /**
   * Converts the given text to speech and plays the audio.
   *
   * @param text the text to be converted to speech
   * @throws IllegalArgumentException if the text is null or empty
   */
  public static void speak(String text) {
    if (text == null || text.isEmpty()) {
      throw new IllegalArgumentException("Text should not be null or empty");
    }

    Task<Void> backgroundTask =
        new Task<>() {
          @Override
          protected Void call() {
            try {
              ApiProxyConfig config = ApiProxyConfig.readConfig();
              Provider provider = Provider.OPENAI;
              Voice voice = Voice.OPENAI_ALLOY;

              TextToSpeechRequest ttsRequest = new TextToSpeechRequest(config);
              ttsRequest.setText(text).setProvider(provider).setVoice(voice);

              TextToSpeechResult ttsResult = ttsRequest.execute();
              String audioUrl = ttsResult.getAudioUrl();
              System.out.println(audioUrl);

              try (InputStream inputStream =
                  new BufferedInputStream(new URL(audioUrl).openStream())) {
                Player player = new Player(inputStream);
                player.play();
              } catch (JavaLayerException | IOException e) {
                e.printStackTrace();
              }

            } catch (ApiProxyException e) {
              e.printStackTrace();
            }
            return null;
          }
        };

    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.setDaemon(true); // Ensure the thread does not prevent JVM shutdown

    System.out.println(text);
  }

  /**
   * Converts the given text to speech and plays the audio.
   *
   * @param text the text to be converted to speech and played as audio.
   * @param context the GameStateContext object that contains the current state of the game.
   */
  public static void speak(String text, GameStateContext context) {
    // Check if the text is null or empty
    if (text == null || text.isEmpty()) {
      throw new IllegalArgumentException("Text should not be null or empty");
    }

    // Create a background task to convert the text to speech and play the audio
    Task<Void> backgroundTask =
        new Task<>() {
          @Override
          protected Void call() {
            try {
              //
              ApiProxyConfig config = ApiProxyConfig.readConfig();
              Provider provider = Provider.OPENAI;

              Voice voice = context.getRoomController().getPerson().getVoice();
              // Create a new TextToSpeechRequest with the given configuration
              TextToSpeechRequest ttsRequest = new TextToSpeechRequest(config);
              ttsRequest.setText(text).setProvider(provider).setVoice(voice);
              // Execute the request and get the result
              TextToSpeechResult ttsResult = ttsRequest.execute();
              String audioUrl = ttsResult.getAudioUrl();
              // Print the audio URL and play the audio
              try (InputStream inputStream =
                  new BufferedInputStream(new URL(audioUrl).openStream())) {
                Player player = new Player(inputStream);
                player.play();
              } catch (JavaLayerException | IOException e) {
                e.printStackTrace();
              }

            } catch (ApiProxyException e) {
              e.printStackTrace();
            }
            return null;
          }
        };

    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.setDaemon(true); // Ensure the thread does not prevent JVM shutdown

    System.out.println(text);
  }
}
