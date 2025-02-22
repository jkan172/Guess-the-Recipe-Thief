package nz.ac.auckland.se206;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.apiproxy.tts.TextToSpeechRequest.Voice;
import nz.ac.auckland.se206.controllers.GuessController;
import nz.ac.auckland.se206.controllers.RoomController;
import nz.ac.auckland.se206.states.GameOver;
import nz.ac.auckland.se206.states.GameStarted;
import nz.ac.auckland.se206.states.GameState;
import nz.ac.auckland.se206.states.Guessing;

/**
 * Context class for managing the state of the game. Handles transitions between different game
 * states and maintains game data such as the professions and rectangle IDs.
 */
public class GameStateContext {

  private final String rectIdToGuess;
  private final Person personToGuess;
  private final Map<String, Person> rectanglesToProfession;
  private final GameStarted gameStartedState;
  private final Guessing guessingState;
  private final GameOver gameOverState;
  private GameState gameState;
  private RoomController roomController;

  @SuppressWarnings("unused")
  private GuessController guessController;

  private boolean isAnyClueFound = false;

  @SuppressWarnings("unused")
  private boolean isClue1Found = false;

  @SuppressWarnings("unused")
  private boolean isClue2Found = false;

  @SuppressWarnings("unused")
  private boolean isClue3Found = false;

  private boolean isPerson1Talked = false;
  private boolean isPerson2Talked = false;
  private boolean isPerson3Talked = false;

  /**
   * Constructs a new GameStateContext and initializes the game states and professions.
   *
   * @throws ApiProxyException if there is an API proxy error Adding more words due to requirements.
   */
  public GameStateContext() {
    gameStartedState = new GameStarted(this);
    guessingState = new Guessing(this);
    gameOverState = new GameOver(this);

    gameState = gameStartedState; // Initial state

    Person person1 =
        new Person(
            "Successful Restaurateur's Employee",
            "not the thief",
            "worker at the restaurant",
            Voice.GOOGLE_EN_AU_STANDARD_B);
    Person person2 =
        new Person(
            "Struggling Cafe Owner",
            "not the thief",
            "owner of the other restaurant",
            Voice.GOOGLE_EN_AU_STANDARD_C);
    Person person3 =
        new Person(
            "Successful Restaurateur's Brother",
            "the thief",
            "Elder brother of the family",
            Voice.GOOGLE_EN_AU_STANDARD_D);

    // Map rectangle IDs to suspects
    rectanglesToProfession = new HashMap<>();
    rectanglesToProfession.put("rectPerson1", person1);
    rectanglesToProfession.put("rectPerson2", person2);
    rectanglesToProfession.put("rectPerson3", person3);

    rectIdToGuess = "rectPerson3";

    personToGuess = rectanglesToProfession.get(rectIdToGuess);
  }

  /**
   * Sets the current state of the game.
   *
   * @param state the new state to set Adding more words due to requirements for description.
   */
  public void setState(GameState state) {
    this.gameState = state;
  }

  /**
   * Gets the initial game started state.
   *
   * @return the game started state
   */
  public GameState getGameStartedState() {
    return gameStartedState;
  }

  /**
   * Gets the guessing state.
   *
   * @return the guessing state
   */
  public GameState getGuessingState() {
    return guessingState;
  }

  /**
   * Gets the game over state.
   *
   * @return the game over state
   */
  public GameState getGameOverState() {
    return gameOverState;
  }

  /**
   * Gets the profession to be guessed.
   *
   * @return the profession to guess
   */
  public Person getPersonToGuess() {
    return personToGuess;
  }

  /**
   * Gets the ID of the rectangle to be guessed.
   *
   * @return the rectangle ID to guess
   */
  public String getRectIdToGuess() {
    return rectIdToGuess;
  }

  /**
   * Gets the profession associated with a specific rectangle ID.
   *
   * @param rectangleId the rectangle ID
   * @return the profession associated with the rectangle ID
   */
  public Person getPerson(String rectangleId) {
    return rectanglesToProfession.get(rectangleId);
  }

  /**
   * Handles the event when a rectangle is clicked.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @param rectangleId the ID of the clicked rectangle
   * @throws IOException if there is an I/O error
   */
  public void handleRectangleClick(MouseEvent event, String rectangleId) throws IOException {
    gameState.handleRectangleClick(event, rectangleId);
  }

  /**
   * Handles the event when the guess button is clicked.
   *
   * @throws IOException if there is an I/O error
   */
  public void handleGuessClick() throws IOException {
    gameState.handleGuessClick();
  }

  /**
   * sets the room controller to this current instance.
   *
   * @param roomController Extra tag description due missing tag description.
   */
  public void setRoomController(RoomController roomController) {
    this.roomController = roomController;
  }

  /**
   * gets the room controller that is set to this instance.
   *
   * @return Adding more words due to requirements for description.
   */
  public RoomController getRoomController() {
    return roomController;
  }

  /**
   * gets the current game state.
   *
   * @return Adding more words due to requirements for description.
   */
  public GameState getGameState() {
    return gameState;
  }

  /**
   * Sets the guess controller to this current instance.
   *
   * @param guessController the GuessController to set
   */
  public void setGuessController(GuessController guessController) {
    this.guessController = guessController;
  }

  /**
   * Checks if all suspects have been spoken to. This method iterates through the hashmap of
   * suspects and checks if each suspect has been spoken to. If all suspects have been spoken to, it
   * returns true; otherwise, it returns false.
   *
   * @return true if all suspects have been spoken to, false otherwise
   */
  public boolean isAllSuspectsSpokenTo() {
    // Iterate through the hashmap and check if all suspects have been spoken to
    return isPerson1Talked && isPerson2Talked && isPerson3Talked;
  }

  /** Checks if the first suspect has been spoken to. */
  public void person1Talked() {
    isPerson1Talked = true;
  }

  /** Checks if the second suspect has been spoken to. */
  public void person2Talked() {
    isPerson2Talked = true;
  }

  /** Checks if the third suspect has been spoken to. */
  public void person3Talked() {
    isPerson3Talked = true;
  }

  /** Checks if any clue has been found. */
  public void clueFound() {
    isAnyClueFound = true;
  }

  /** Checks if the first clue has been found. */
  public void clue1Found() {
    isClue1Found = true;
  }

  /** Checks if the second clue has been found. */
  public void clue2Found() {
    isClue2Found = true;
  }

  /** Checks if the third clue has been found. */
  public void clue3Found() {
    isClue3Found = true;
  }

  /**
   * Checks if any clue has been found.
   *
   * @return true if any clue has been found, false otherwise
   */
  public boolean isAnyClueFound() {
    return isAnyClueFound;
  }
}
