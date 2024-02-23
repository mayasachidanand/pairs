import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import javalib.impworld.*;
import java.awt.Color;
import tester.Tester;
import javalib.impworld.WorldScene;
import javalib.worldimages.FontStyle;
import javalib.worldimages.FromFileImage;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.OverlayImage;
import javalib.worldimages.OverlayOffsetImage;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.TextImage;
import javalib.worldimages.WorldImage;

/*
 * User Guide 
To play Barbie Concentration start by running the game. 
The game is set to a hard level where players must match cards based on both rank and color.
The game starts at score 26 and allows for 60 tries. The user can switch the amount of tries if 60
is to difficult. The user can also restart the game by pressing the r key. 

Enhancements:
Graphics: Barbie-themed cards, heart background and Barbie based ending screens
Timer in mins and secs: resets when r is pressed 
Remaining tries: on scoreboard
Difficulty: cards must be matched by rank and color 
 * 
 */


// represents constant values for the game 
interface IConstants {
  int WIDTH = 1000;
  int HEIGHT = 600;
  ArrayList<String> SUITS = new ArrayList<String>(Arrays.asList("♣", "♦", "♥", "♠"));
  ArrayList<String> RANKS = new ArrayList<String>
  (Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"));
}

// represents the concentration game 
class Concentration extends World implements IConstants {
  ArrayList<Card> deck;
  ArrayList<Card> deck1;
  ArrayList<Card> shuffledDeck;
  ArrayList<Card> flipped;
  int score;
  int triesRemaining; 
  Random rand;
  int time;
  int flipTime; 

  Concentration() {
    this.rand = new Random();
    this.deck = new ArrayList<Card>();
    this.deck1 = new ArrayList<Card>();
    this.board();
    this.shuffledDeck = new ArrayList<Card>();
    this.shuffle();
    this.flipped = new ArrayList<Card>();
    this.score = 26;
    this.triesRemaining = 60;
    this.time = 0;
    this.flipTime = 0; 
  }
  
  // create the starting board for the game with 4 rows of 13 cards all face down
  public void board() {
    int yPos = 0;
    for (int i = 0; i < SUITS.size(); i++) {
      int xPos = 50;
      for (int j = 0; j < RANKS.size(); j++) {
        this.deck.add(new Card(RANKS.get(j), SUITS.get(i), xPos, yPos));
        this.deck1.add(new Card(RANKS.get(j), SUITS.get(i), xPos, yPos));
        xPos += 75;
      }
      yPos += 150;
    }
  }

  // shuffles the deck 
  public void shuffle() {
    int size = deck1.size();
    int yPos = -50;
    int xPos = 50;
    for (int i = 0; i < size; i++) {
      if (i % 13 == 0) {
        yPos += 125;
        xPos = 50;
      }
      else {
        xPos += 75;
      }
      int rand = this.rand.nextInt(deck1.size());
      this.shuffledDeck.add(deck1.get(rand).clone(xPos, yPos));
      deck1.remove(rand);
    }
  }

  // returns a String of the time in minutes and seconds
  public String gameTime() {
    if (this.time % 60 == 0) {
      return String.valueOf(this.time / 60) + ":00";
    }
    else {
      if (this.time % 60 > 9) {
        return String.valueOf(this.time / 60) + ":" + String.valueOf(this.time % 60);
      }
      else {
        return String.valueOf(this.time / 60) + ":0" + String.valueOf(this.time % 60);
      }
    }
  }

  // draws the game
  @Override
  public WorldScene makeScene() {
    WorldScene ws = new WorldScene(WIDTH, HEIGHT);
    ws.placeImageXY(new FromFileImage("src/background.png"), 500, 300);
    for (Card c: this.shuffledDeck) {
      c.drawCardWorld(ws);
    }
    //score board 
    ws.placeImageXY(this.scoreBoard(), 500, 550); 
    ws.placeImageXY(new TextImage("♥", 15, Color.WHITE), 325, 550); 
    //remaining tries 
    ws.placeImageXY((new TextImage("Tries Left: " + triesRemaining, 20, Color.WHITE)), 850, 550); 
    ws.placeImageXY(new TextImage("♥", 15, Color.WHITE), 675, 550);
    //time
    ws.placeImageXY((new TextImage("Time: " + this.gameTime(), 20 , Color.WHITE)), 150, 550); 
    return ws;
  }

  //creates a score board
  public WorldImage scoreBoard() {
    return new OverlayImage((new TextImage("Score: " + score, 20, Color.WHITE)),
        (new RectangleImage(950, 75, OutlineMode.SOLID, Color.pink))); 
  }

  //changes the game based on tick (1.0) 
  public void onTick() {
    this.time++;
    if (this.shuffledDeck.size() == 0) {
      this.endOfWorld("YOU CONCENTRATED!");
    }
    else if (this.triesRemaining < score) {
      this.endOfWorld("CONCENTRATE MORE!");
    }
    if (this.flipped.size() == 2 && time == flipTime + 2) {
      Card card1 = this.flipped.get(0);
      Card card2 = this.flipped.get(1);
      if (card1.sameRank(card2) && card1.sameColor(card2)) {
        score--;
        this.removeCard(this.flipped);
        this.flipped.clear();
      }
      else if (time == flipTime + 2) {
        card1.flip();
        card2.flip();
        this.flipped.clear();
      }
      triesRemaining--;
    }
  }

  //flips card  when mouse is clicked 
  public void onMouseClicked(Posn pos) {
    // for loop to find where you can click 
    for (Card c: this.shuffledDeck) {
      if (this.flipped.size() == 2) {
        return ;
      }
      flipTime = time;
      if (c.withinCard(pos)) {
        if (c.faceUp) {  //we know this is a field of field but its a primative so we can't mutate it and TA Dan says its ok 
          c.makeFaceUp();
        }
        else {
          c.makeFaceUp();
          this.flipped.add(c);
        }
      }
    }
  }

  // removes a card from the list 
  public void removeCard(ArrayList<Card> arr) {
    for (Card card: arr) {
      this.shuffledDeck.remove(card);
    }
  }

  // handles keystrokes 
  public void onKeyEvent(String k) {
    if (k.equals("r")) {
      this.shuffledDeck.clear();
      this.deck.clear();
      this.board();
      this.shuffle();
      this.time = 0;
      this.flipTime = 0; 
      this.score = 26;
      this.triesRemaining = 60; 
    }
  }

  //game ends scene
  public WorldScene lastScene(String msg) {
    WorldScene ws = new WorldScene(WIDTH, HEIGHT);
    ws.placeImageXY(new FromFileImage("src/endScene.png"), 500, 300);
    ws.placeImageXY(new TextImage(msg, 50, FontStyle.BOLD_ITALIC, Color.pink), 300, 150);
    return ws;
  }
}

// represents a card
class Card implements IConstants {
  String rank;
  String suit;
  int x; 
  int y; 
  boolean faceUp;

  Card(String rank, String suit, int x, int y, boolean faceUp) {
    this.rank = rank;
    this.suit = suit;
    this.x = x; 
    this.y = y; 
    this.faceUp = faceUp;
  }

  Card(String rank, String suit, int x, int y) {
    this(rank, suit, x, y, false);
  }

  // draws the front or back of this card onto the world scene
  public WorldImage drawCard() {
    WorldImage blank = new OverlayImage(new RectangleImage(50, 100, OutlineMode.OUTLINE, Color.pink), 
        new RectangleImage(50, 100, OutlineMode.SOLID, Color.white));
    WorldImage backOfCard = new FromFileImage("src/barbie.png");
    if (faceUp) {
      if (this.suit.equals("♦") || this.suit.equals("♥")) {
        return new OverlayOffsetImage(
            new TextImage(this.suit, 15, Color.RED),
            -10, -40,
            new OverlayOffsetImage(
                new TextImage(this.suit, 15, Color.RED),
                15, 35,
                new OverlayImage(
                    new TextImage(this.rank, 15, FontStyle.BOLD, Color.RED),
                    blank)));
      }
      else {
        return new OverlayOffsetImage(
            new TextImage(this.suit, 15, Color.BLACK),
            -10, -40,
            new OverlayOffsetImage(
                new TextImage(this.suit, 15, Color.BLACK),
                15, 35,
                new OverlayImage(
                    new TextImage(this.rank, 15, FontStyle.BOLD, Color.BLACK),
                    blank)));
      }
    }
    else {
      return backOfCard;
    }
  }

  // draws the card onto the world scene
  public WorldScene drawCardWorld(WorldScene ws) {
    ws.placeImageXY(this.drawCard(), this.x, this.y);
    return ws;
  }

  // compares if two cards have the same rank 
  public boolean sameRank(Card c) {
    return this.rank.equals(c.rank);
  }

  // compares if two cards have the same color
  public boolean sameColor(Card c) {
    return this.suit.equals("♦") && c.suit.equals("♥")
        || this.suit.equals("♥") && c.suit.equals("♦")
        || this.suit.equals("♣") && c.suit.equals("♠")
        || this.suit.equals("♠") && c.suit.equals("♣");
  }

  // make this card face up
  public void makeFaceUp() {
    this.faceUp = true;
  }

  // is the given position within the bounds of the card 
  public boolean withinCard(Posn pos) {
    return (this.x - 25) <= pos.x 
        && (this.x + 25) >= pos.x
        && (this.y - 50) <= pos.y
        && (this.y + 50) >= pos.y;
  }

  //makes the card flipped down 
  public void flip() {
    this.faceUp = false;
  }

  //makes a new card with given x and y position 
  public Card clone(int x, int y) {
    return new Card(this.rank, this.suit, x, y);
  }
}


//tests and examples for concentration game 
class ExamplesConcentration implements IConstants {

  Card c = new Card("2", "♣", 50, 100, false);
  //    Card c1 = new Card("3", "♦", false);
  //    Card c2 = new Card("2", "♣", false);
  ArrayList<Card> list0= new ArrayList<Card>();
  //    ArrayList<Card> lista = new ArrayList<Card>(Arrays.asList(c2));
  //    ArrayList<Card> listb = new ArrayList<Card>(Arrays.asList(c1));
  ArrayList<Card> list1 = new ArrayList<Card>(Arrays.asList(c));


  Card ace1 = new Card("A", "♣", 50, 100, false); 
  Card aceUp = new Card("A", "♠", 400, 500, true);
  Card ace2 = new Card("A", "♦", 50, 100, false); 
  Card ace3 = new Card("A", "♥", 900, 900, false); 
  Card ace4 = new Card("A", "♠", 20, 100, true); 
  Card two1 = new Card("2", "♣", 50, 100, false); 
  Card two2 = new Card("2", "♦", 50, 100, false); 
  Card two3 = new Card("2", "♥", 50, 100, false); 
  Card two4 = new Card("2", "♠", 50, 100, false); 
  Card three1 = new Card("3", "♣", 50, 100, true); 
  Card three2 = new Card("3", "♦", 50, 100, false); 
  Card three3 = new Card("3", "♥", 0, 100, false); 
  Card three4 = new Card("3", "♠", 50, 100, false); 
  Card four1 = new Card("4", "♣", 50, 100, false); 
  Card four2 = new Card("4", "♦", 50, 100, false); 
  Card four3 = new Card("4", "♥", 50, 100, false); 
  Card four4 = new Card("4", "♠", 50, 100, false); 
  Card five1 = new Card("5", "♣", 50, 100, false); 
  Card five2 = new Card("5", "♦", 50, 100, true); 
  Card five3 = new Card("5", "♥", 50, 100, false); 
  Card five4 = new Card("5", "♠", 50, 100, false); 
  Card six1 = new Card("6", "♣", 50, 100, false); 
  Card six2 = new Card("6", "♦", 100, 200, true); 
  Card six3 = new Card("6", "♥", 50, 100, true); 
  Card six4 = new Card("6", "♠", 50, 100, false); 
  Card seven1 = new Card("7", "♣", 50, 100, false); 
  Card seven2 = new Card("7", "♦", 50, 100, false); 
  Card seven3 = new Card("7", "♥", 50, 100, false); 
  Card seven4 = new Card("7", "♠", 200, 20, false); 
  Card eight1 = new Card("8", "♣", 50, 100, false); 
  Card eight2 = new Card("8", "♦", 50, 100, false); 
  Card eight3 = new Card("8", "♥", 50, 100, true); 
  Card eight4 = new Card("8", "♠", 50, 100, false); 
  Card nine1 = new Card("9", "♣", 50, 100, false); 
  Card nine2 = new Card("9", "♦", 50, 100, false); 
  Card nine3 = new Card("9", "♥", 50, 100, false); 
  Card nine4 = new Card("9", "♠", 50, 100, false); 
  Card ten1 = new Card("10", "♣", 50, 100, true); 
  Card ten2 = new Card("10", "♦", 50, 100, false); 
  Card ten3 = new Card("10", "♥", 0, 100, false); 
  Card ten4 = new Card("10", "♠", 50, 100, false); 
  Card j1 = new Card("J", "♣", 50, 100, false); 
  Card j2 = new Card("J", "♦", 50, 100, false); 
  Card j3 = new Card("J", "♥", 50, 100, false); 
  Card j4 = new Card("J", "♠", 50, 100, false); 
  Card q1 = new Card("Q", "♣", 50, 100, false); 
  Card q2 = new Card("Q", "♦", 50, 100, false); 
  Card q3 = new Card("Q", "♥", 400, 0, false); 
  Card q4 = new Card("Q", "♠", 50, 100, false); 
  Card k1 = new Card("K", "♣", 50, 100, false); 
  Card k2 = new Card("K", "♦", 50, 100, false); 
  Card k3 = new Card("K", "♥", 50, 100, false); 
  Card k4 = new Card("K", "♠", 50, 100, false); 
  ArrayList<Card> removeList = new ArrayList<Card>(Arrays.asList(k4, k2));

  Card a1;
  Card nine2a;
  Card k4a; 
  Card six3a; 
  WorldScene ws1;
  Concentration game;
  Concentration game2;
  Concentration game3;
  ArrayList<Card> orderedDeck;


  void initData() {
    this.a1 = new Card("A", "♣", 50, 100, false);
    this.nine2a = new Card("9", "♦", 50, 100, false);
    this.k4a = new Card("K", "♠", 50, 100, false);
    this.six3a = new Card("6", "♥", 50, 100, true); 
    this.ws1 = new WorldScene(WIDTH, HEIGHT);
    this.game = new Concentration();
    this.game2 = new Concentration();
    this.game3 = new Concentration();
    this.orderedDeck = new ArrayList<Card>();

  }
  //tests 

  //-------------------------------Card Methods------------------------------
  //tests for drawCard
  void testDrawCard(Tester t) {
    WorldImage blank = new OverlayImage(new RectangleImage(50, 100, OutlineMode.OUTLINE, Color.pink), 
        new RectangleImage(50, 100, OutlineMode.SOLID, Color.white));

    t.checkExpect(ace1.drawCard(), new FromFileImage("src/barbie.png"));
    t.checkExpect(three1.drawCard(), new OverlayOffsetImage(
        new TextImage("♣", 15, Color.BLACK),
        -10, -40,
        new OverlayOffsetImage(
            new TextImage("♣", 15, Color.BLACK),
            15, 35,
            new OverlayImage(
                new TextImage("3", 15, FontStyle.BOLD, Color.BLACK),
                blank))) );
    t.checkExpect(k3.drawCard(), new FromFileImage("src/barbie.png"));
    t.checkExpect(five2.drawCard(), new OverlayOffsetImage(
        new TextImage("♦", 15, Color.RED),
        -10, -40,
        new OverlayOffsetImage(
            new TextImage("♦", 15, Color.RED),
            15, 35,
            new OverlayImage(
                new TextImage("5", 15, FontStyle.BOLD, Color.RED),
                blank))));
    t.checkExpect(six2.drawCard(),new OverlayOffsetImage(
        new TextImage("♦", 15, Color.RED),
        -10, -40,
        new OverlayOffsetImage(
            new TextImage("♦", 15, Color.RED),
            15, 35,
            new OverlayImage(
                new TextImage("6", 15, FontStyle.BOLD, Color.RED),
                blank))));  
  }

  //tests for drawCardWorld 
  void testDrawCardWorld(Tester t) {
    WorldScene wsAce1 = new WorldScene(WIDTH, HEIGHT);
    wsAce1.placeImageXY(new FromFileImage("src/background.png"), 500, 300);
    wsAce1.placeImageXY(new FromFileImage("src/barbie.png"), 50, 100);
    WorldImage blank = new OverlayImage(new RectangleImage(50, 100, OutlineMode.OUTLINE, Color.pink), 
        new RectangleImage(50, 100, OutlineMode.SOLID, Color.white));

    WorldScene wsthree1 = new WorldScene(WIDTH, HEIGHT);
    wsthree1.placeImageXY(new FromFileImage("src/background.png"), 500, 300);
    wsthree1.placeImageXY((new OverlayOffsetImage(
        new TextImage("♣", 15, Color.BLACK),
        -10, -40,
        new OverlayOffsetImage(
            new TextImage("♣", 15, Color.BLACK),
            15, 35,
            new OverlayImage(
                new TextImage("3", 15, FontStyle.BOLD, Color.BLACK),
                blank)))), 50, 100) ;
    WorldScene wssix2 = new WorldScene(WIDTH, HEIGHT);
    wssix2.placeImageXY(new FromFileImage("src/background.png"), 500, 300);
    wssix2.placeImageXY((new OverlayOffsetImage(
        new TextImage("♦", 15, Color.RED),
        -10, -40,
        new OverlayOffsetImage(
            new TextImage("♦", 15, Color.RED),
            15, 35,
            new OverlayImage(
                new TextImage("6", 15, FontStyle.BOLD, Color.RED),
                blank)))), 100, 200) ;

    t.checkExpect(ace1.drawCardWorld(wsAce1), wsAce1);
    t.checkExpect(three1.drawCardWorld(wsthree1), wsthree1);
    t.checkExpect(six2.drawCardWorld(wssix2), wssix2);
  }

  //tests for sameRank
  void testSameRank(Tester t) {
    t.checkExpect(j1.sameRank(j1), true); 
    t.checkExpect(ace1.sameRank(ace2), true); 
    t.checkExpect(ace1.sameRank(ten2), false); 
    t.checkExpect(two3.sameRank(ace3), false); 
  }

  //tests for sameColor
  void testSameColor(Tester t) {
    t.checkExpect(j1.sameColor(j1), false);  //should be false since a deck does not have two cards with same rank and suit so we don't check that condition 
    t.checkExpect(k1.sameColor(j4), true); 
    t.checkExpect(k1.sameColor(j2), false);
    t.checkExpect(ten2.sameColor(two3), true); 
    t.checkExpect(ten1.sameColor(two3), false); 
    t.checkExpect(ace3.sameColor(two2), true);
    t.checkExpect(ace3.sameColor(two4), false);
    t.checkExpect(five4.sameColor(four1), true); 
    t.checkExpect(five2.sameColor(four1), false); 
  }


  //tests for makeFaceUp
  void testsMakeFaceUp(Tester t) {
    this.initData();

    t.checkExpect(a1.faceUp, false);
    a1.makeFaceUp();
    t.checkExpect(a1.faceUp, true);
    t.checkExpect(k4a.faceUp, false);
    k4a.makeFaceUp();
    t.checkExpect(k4a.faceUp, true);
  }


  //tests for withinCard
  void testWithinCard(Tester t) {
    t.checkExpect(ace1.withinCard(new Posn(75, 110)), true); 
    t.checkExpect(seven4.withinCard(new Posn(0, 0)), false); 
    t.checkExpect(ace1.withinCard(new Posn(0, 400)), false); 
    t.checkExpect(seven4.withinCard(new Posn(200, 20)), true); 
    t.checkExpect(ten3.withinCard(new Posn(25, 280)), false); 
    t.checkExpect(q3.withinCard(new Posn(0, 0)), false); 
  }

  //tests for flip
  void testsFlip(Tester t) {
    this.testsMakeFaceUp(t);
    t.checkExpect(a1.faceUp, true);
    a1.flip();
    t.checkExpect(a1.faceUp, false);
    t.checkExpect(k4a.faceUp, true);
    k4a.flip();
    t.checkExpect(k4a.faceUp, false);

    this.initData();
    t.checkExpect(a1.faceUp, false);
    a1.flip();
    t.checkExpect(a1.faceUp, false);
  }

  //tests for clone 
  void testClone(Tester t) {
    t.checkExpect(ace1.clone(75, 110),new Card("A", "♣", 75, 110, false));
    t.checkExpect(five3.clone(50, 100), five3);
    t.checkExpect(k3.clone(400, 0),new Card("K", "♥", 400, 0, false));
    t.checkExpect(seven4.clone(200, 20), seven4);
  }

  //---------------------------------- Big Bang----------------------------------
  //tests big bang method 
  void testGame(Tester t) {
    Concentration g = new Concentration();
    g.bigBang(WIDTH, HEIGHT, 1.0);
  }

  //tests for board method
  void testBoard(Tester t) {
    this.initData();
    t.checkExpect(this.game.deck.size(), 52);
    this.game.deck = new ArrayList<Card>();
    t.checkExpect(this.game.deck, list0);
    this.game.board();
    t.checkExpect(this.game.deck.size(), 52);

    ArrayList<Card> row1 = new ArrayList<Card>(Arrays.asList(this.game.deck.get(0), this.game.deck.get(1), this.game.deck.get(2), 
        this.game.deck.get(3), this.game.deck.get(4), this.game.deck.get(5), this.game.deck.get(6), this.game.deck.get(7), 
        this.game.deck.get(8), this.game.deck.get(9), this.game.deck.get(10), this.game.deck.get(11), this.game.deck.get(12)));

    ArrayList<Card> row2 = new ArrayList<Card>(Arrays.asList(this.game.deck.get(13), this.game.deck.get(14), this.game.deck.get(15), 
        this.game.deck.get(16), this.game.deck.get(17), this.game.deck.get(18), this.game.deck.get(19), this.game.deck.get(20), 
        this.game.deck.get(21), this.game.deck.get(22), this.game.deck.get(23), this.game.deck.get(24), this.game.deck.get(25)));

    ArrayList<Card> row3 = new ArrayList<Card>(Arrays.asList(this.game.deck.get(26), this.game.deck.get(27), this.game.deck.get(28), 
        this.game.deck.get(29), this.game.deck.get(30), this.game.deck.get(31), this.game.deck.get(32), this.game.deck.get(33), 
        this.game.deck.get(34), this.game.deck.get(35), this.game.deck.get(36), this.game.deck.get(37), this.game.deck.get(38)));

    ArrayList<Card> row4 = new ArrayList<Card>(Arrays.asList(this.game.deck.get(39), this.game.deck.get(40), this.game.deck.get(41), 
        this.game.deck.get(42), this.game.deck.get(43), this.game.deck.get(44), this.game.deck.get(45), this.game.deck.get(46), 
        this.game.deck.get(47), this.game.deck.get(48), this.game.deck.get(49), this.game.deck.get(50), this.game.deck.get(51)));

    int rankVal1 = 0;
    int x1 = 50; 
    for (Card card: row1) {
      String cardRank = RANKS.get(rankVal1);
      Card c = new Card(cardRank, "♣", x1, 0);
      t.checkExpect(card, c);
      x1 = x1 + 75;
      cardRank = RANKS.get(rankVal1++);
    }

    int rankVal2 = 0;
    int x2 = 50; 
    for (Card card: row2) {
      String cardRank = RANKS.get(rankVal2);
      Card c = new Card(cardRank, "♦", x2, 150);
      t.checkExpect(card, c);
      x2 = x2 + 75;
      cardRank = RANKS.get(rankVal2++);
    }

    int rankVal3 = 0;
    int x3 = 50; 
    for (Card card: row3) {
      String cardRank = RANKS.get(rankVal3);
      Card c = new Card(cardRank, "♥", x3, 300);
      t.checkExpect(card, c);
      x3 = x3 + 75;
      cardRank = RANKS.get(rankVal3++);
    }

    int rankVal4 = 0;
    int x4 = 50; 
    for (Card card: row4) {
      String cardRank = RANKS.get(rankVal4);
      Card c = new Card(cardRank, "♠", x4, 450);
      t.checkExpect(card, c);
      x4 = x4 + 75;
      cardRank = RANKS.get(rankVal4++);
    }
  }

  //tests for shuffle method
  void testShuffle(Tester t) {
    this.initData();
    Card ace1 = new Card("A", "♣", 50, 100, false);
    Card seven4 = new Card("7", "♠", 200, 20, false); 

    ArrayList<Card> a1 = new ArrayList<Card>(Arrays.asList(ace1, seven4)); 
    this.game.deck1 = a1;
    t.checkExpect(a1.size(), 2);
    this.game.shuffledDeck = new ArrayList<Card>();
    this.game.shuffle(); 
    t.checkExpect(this.game.shuffledDeck.size(), 2);
    t.checkExpect(this.game.shuffledDeck.containsAll(a1), true); 
  }
  
  //tests for gameTime method
  void testGameTime(Tester t) {
    this.initData();
    t.checkExpect(this.game.time, 0);
    t.checkExpect(this.game.gameTime(), "0:00");
    this.game.time = 5;
    t.checkExpect(this.game.gameTime(), "0:05");
    this.game.time = 15;
    t.checkExpect(this.game.gameTime(), "0:15");
    this.game.time = 75;
    t.checkExpect(this.game.gameTime(), "1:15");
    this.game.time = 60;
    t.checkExpect(this.game.gameTime(), "1:00");
    this.game.time = 65;
    t.checkExpect(this.game.gameTime(), "1:05");
  }

  //tests for makeScene method
  void testMakeScene(Tester t) {
    this.initData();
    Card ace1 = new Card("A", "♣", 50, 100, false);
    Card aceUp = new Card("A", "♣", 50, 100, false);
    WorldImage backOfCard = new FromFileImage("src/barbie.png");
    WorldImage blank = new OverlayImage(new RectangleImage(50, 100, OutlineMode.OUTLINE, Color.pink), 
        new RectangleImage(50, 100, OutlineMode.SOLID, Color.white)); 
    ArrayList<Card> a1 = new ArrayList<Card>(Arrays.asList(ace1, aceUp));
    this.game.deck1 = a1;
    t.checkExpect(a1.size(), 2);
    this.game.shuffledDeck = new ArrayList<Card>();
    this.game.shuffle();
    t.checkExpect(this.game.shuffledDeck.size(), 2);
    t.checkExpect(this.game.shuffledDeck.containsAll(a1), true);
    ws1.placeImageXY(new FromFileImage("src/background.png"), 500, 300);
    ws1.placeImageXY(backOfCard, 50, 75);
    ws1.placeImageXY(backOfCard, 125, 75);
    ws1.placeImageXY(this.game.scoreBoard(), 500, 550);
    ws1.placeImageXY(new TextImage("♥", 15, Color.WHITE), 325, 550);
    ws1.placeImageXY((new TextImage("Tries Left: " + this.game.triesRemaining, 20, Color.WHITE)), 850, 550);
    ws1.placeImageXY(new TextImage("♥", 15, Color.WHITE), 675, 550);
    ws1.placeImageXY((new TextImage("Time: " + this.game.gameTime(), 20, Color.WHITE)), 150, 550);
    t.checkExpect(this.game.makeScene(), ws1);
    
    ws1 = new WorldScene(WIDTH, HEIGHT);
    this.game.onMouseClicked(new Posn(125, 75));
    ws1.placeImageXY(new FromFileImage("src/background.png"), 500, 300);
    ws1.placeImageXY(backOfCard, 50, 75);
    ws1.placeImageXY(new OverlayOffsetImage(
            new TextImage("♣", 15, Color.BLACK),
            -10, -40,
            new OverlayOffsetImage(
                new TextImage("♣", 15, Color.BLACK),
                15, 35,
                new OverlayImage(
                    new TextImage("A", 15, FontStyle.BOLD, Color.BLACK),
                    blank))), 125, 75);
    ws1.placeImageXY(this.game.scoreBoard(), 500, 550);
    ws1.placeImageXY(new TextImage("♥", 15, Color.WHITE), 325, 550);
    ws1.placeImageXY((new TextImage("Tries Left: " + this.game.triesRemaining, 20, Color.WHITE)), 850, 550);
    ws1.placeImageXY(new TextImage("♥", 15, Color.WHITE), 675, 550);
    ws1.placeImageXY((new TextImage("Time: " + this.game.gameTime(), 20, Color.WHITE)), 150, 550);
    t.checkExpect(this.game.makeScene(), ws1);
    
    this.initData();
    ArrayList<Card> a2 = new ArrayList<Card>(Arrays.asList(ace1, aceUp));
    this.game.deck1 = a2;
    t.checkExpect(a2.size(), 2);
    this.game.shuffledDeck = new ArrayList<Card>();
    this.game.shuffle();
    t.checkExpect(this.game.shuffledDeck.size(), 2);
    t.checkExpect(this.game.shuffledDeck.containsAll(a2), true);
    this.game.onMouseClicked(new Posn(50, 75));
    this.game.onMouseClicked(new Posn(125, 75));
    ws1.placeImageXY(new FromFileImage("src/background.png"), 500, 300);
    ws1.placeImageXY(new OverlayOffsetImage(
        new TextImage("♣", 15, Color.BLACK),
        -10, -40,
        new OverlayOffsetImage(
            new TextImage("♣", 15, Color.BLACK),
            15, 35,
            new OverlayImage(
                new TextImage("A", 15, FontStyle.BOLD, Color.BLACK),
                blank))), 50, 75);
    ws1.placeImageXY(new OverlayOffsetImage(
            new TextImage("♣", 15, Color.BLACK),
            -10, -40,
            new OverlayOffsetImage(
                new TextImage("♣", 15, Color.BLACK),
                15, 35,
                new OverlayImage(
                    new TextImage("A", 15, FontStyle.BOLD, Color.BLACK),
                    blank))), 125, 75);
    ws1.placeImageXY(this.game.scoreBoard(), 500, 550);
    ws1.placeImageXY(new TextImage("♥", 15, Color.WHITE), 325, 550);
    ws1.placeImageXY((new TextImage("Tries Left: " + this.game.triesRemaining, 20, Color.WHITE)), 850, 550);
    ws1.placeImageXY(new TextImage("♥", 15, Color.WHITE), 675, 550);
    ws1.placeImageXY((new TextImage("Time: " + this.game.gameTime(), 20, Color.WHITE)), 150, 550);
    t.checkExpect(this.game.makeScene(), ws1);
    
    this.initData();
    this.game.deck1 = new ArrayList<Card>();
    t.checkExpect(this.game.deck1.size(), 0);
    this.game.shuffledDeck = new ArrayList<Card>();
    this.game.shuffle();
    t.checkExpect(this.game.shuffledDeck.size(), 0);
    ws1.placeImageXY(new FromFileImage("src/background.png"), 500, 300);
    ws1.placeImageXY(this.game.scoreBoard(), 500, 550);
    ws1.placeImageXY(new TextImage("♥", 15, Color.WHITE), 325, 550);
    ws1.placeImageXY((new TextImage("Tries Left: " + this.game.triesRemaining, 20, Color.WHITE)), 850, 550);
    ws1.placeImageXY(new TextImage("♥", 15, Color.WHITE), 675, 550);
    ws1.placeImageXY((new TextImage("Time: " + this.game.gameTime(), 20, Color.WHITE)), 150, 550);
    t.checkExpect(this.game.makeScene(), ws1);
  }

  //tests for scoreBoard method
  void testScoreBoard(Tester t) {
    this.initData();
    t.checkExpect(this.game.score, 26);
    t.checkExpect(game.scoreBoard(), new OverlayImage((new TextImage("Score: 26", 20, Color.WHITE)),
        (new RectangleImage(950, 75, OutlineMode.SOLID, Color.PINK))));
    this.initData();
    this.game.score = 15;
    t.checkExpect(this.game.score, 15);
    t.checkExpect(game.scoreBoard(), new OverlayImage((new TextImage("Score: 15", 20, Color.WHITE)),
        (new RectangleImage(950, 75, OutlineMode.SOLID, Color.PINK))));
  }

  //tests for onTick method
  void testOnTick(Tester t) {
    this.initData();
    Card ace1 = new Card("A", "♣", 50, 100, false);
    Card aceUp = new Card("A", "♠", 50, 100, false);
    Card twoUp = new Card("2", "♠", 50, 100, false);
    ArrayList<Card> list = new ArrayList<Card>(Arrays.asList(ace1, aceUp));
    this.game.deck1 = list;
    t.checkExpect(this.game.deck1.size(), 2);
    this.game.shuffledDeck = new ArrayList<Card>();
    this.game.shuffle();
    t.checkExpect(this.game.shuffledDeck.size(), 2);
    t.checkExpect(this.game.shuffledDeck.containsAll(list), true);
    Card ace1a = new Card("A", "♣", 50, 100, true);
    Card aceUpa = new Card("A", "♠", 50, 100, true);
    this.game.flipped = new ArrayList<Card>(Arrays.asList(ace1a, aceUpa));
    this.game.flipTime = 20;
    this.game.time = 22;
    t.checkExpect(this.game.flipped.size(), 2);
    t.checkExpect(this.game.time, this.game.flipTime + 2);
    this.game.score = 5;
    this.game.onTick();
    this.game.score--;
    this.game.flipped.clear(); 
    t.checkExpect(this.game.score, 4);
    t.checkExpect(this.game.time, 23);
    t.checkExpect(this.game.shuffledDeck.containsAll(new ArrayList<Card>(Arrays.asList(ace1a, aceUpa))), false);
    t.checkExpect(this.game.flipped.size(), 0);
    
    ArrayList<Card> list1 = new ArrayList<Card>(Arrays.asList(twoUp, aceUp));
    this.game.deck1 = list1;
    t.checkExpect(this.game.deck1.size(), 2);
    this.game.shuffledDeck = new ArrayList<Card>();
    this.game.shuffle();
    t.checkExpect(this.game.shuffledDeck.size(), 2);
    t.checkExpect(this.game.shuffledDeck.containsAll(list), true);
    Card twoUpa = new Card("2", "♣", 50, 100, true);
    Card aceUp2 = new Card("A", "♠", 50, 100, true);
    this.game.flipped = new ArrayList<Card>(Arrays.asList(twoUpa, aceUp2));
    this.game.flipTime = 20;
    this.game.time = 22;
    t.checkExpect(this.game.flipped.size(), 2);
    t.checkExpect(this.game.time, this.game.flipTime + 2);
    this.game.score = 5;
    this.game.onTick();
    this.game.flipped.clear(); 
    t.checkExpect(this.game.score, 5);
    t.checkExpect(this.game.shuffledDeck.containsAll(new ArrayList<Card>(Arrays.asList(twoUpa, aceUp2))), true);
    
    this.game.shuffledDeck = new ArrayList<Card>(); 
    this.game.score = 0;
    this.game.triesRemaining = 5;
    this.game.onTick();
    t.checkExpect(this.game.score, 0);
    t.checkExpect(this.game.triesRemaining, 5);
    ws1.placeImageXY(new FromFileImage("src/endScene.png"), 500, 300);
    ws1.placeImageXY(new TextImage("You CONCENTRATED!", 50, FontStyle.BOLD_ITALIC, Color.pink), 300, 150);
    t.checkExpect(game.lastScene("You CONCENTRATED!"), ws1);
    
    this.game.score = 10;
    this.game.triesRemaining = 8;
    this.game.onTick();
    t.checkExpect(this.game.score, 10);
    t.checkExpect(this.game.triesRemaining, 8);
    ws1.placeImageXY(new FromFileImage("src/endScene.png"), 500, 300);
    ws1.placeImageXY(new TextImage("Concentrate More!", 50, FontStyle.BOLD_ITALIC, Color.pink), 300, 150);
    t.checkExpect(game.lastScene("Concentrate More!"), ws1);
  }

  //tests for onMouseClicked method
  void testOnMouseClicked(Tester t) {
    this.initData(); 
    
    Card ace1 = new Card("A", "♣", 50, 100, false);
    Card aceUp = new Card("A", "♠", 400, 500, true);
    this.game.flipped = new ArrayList<Card>(Arrays.asList(aceUp));
    ArrayList<Card> list = new ArrayList<Card>(Arrays.asList(ace1, aceUp));
    this.game.deck1 = list;
    this.game.shuffledDeck = new ArrayList<Card>(); 
    this.game.board();
    this.game.shuffle();
    this.game.onMouseClicked(new Posn(50,100));
    t.checkExpect(this.game.flipped, new ArrayList<Card>(Arrays.asList(aceUp, new Card("A", "♣", 50, 100, true))));
    this.game.onMouseClicked(new Posn(400, 500));
    t.checkExpect(this.game.flipped, new ArrayList<Card>(Arrays.asList(aceUp, new Card("A", "♣", 50, 100, true))));
    t.checkExpect(this.aceUp.faceUp, true);
    this.game.onMouseClicked(new Posn(0,0));
    t.checkExpect(this.game.flipped, new ArrayList<Card>(Arrays.asList(aceUp, new Card("A", "♣", 50, 100, true))));
  }

  //tests for removeCard method
  void testRemoveCard(Tester t) {
    this.initData();

    this.game.shuffledDeck = new ArrayList<Card>(); 
    this.game.board();
    this.game.shuffle();
    t.checkExpect(this.game.shuffledDeck.size(), 52); 
    this.game.removeCard(removeList);
    t.checkExpect(this.game.shuffledDeck.containsAll(removeList), false); 
  }

  //tests for onKeyEvent method
  void testOnKeyEvent(Tester t) {
    this.initData();
    this.game.shuffledDeck = new ArrayList<Card>();
    this.game.board();
    this.game.shuffle();
    t.checkExpect(this.game.shuffledDeck.size(), 52);
    this.game.removeCard(removeList);
    t.checkExpect(this.game.shuffledDeck.containsAll(removeList), false);
    this.game.score = 25;
    this.game.time = 30;
    this.game.triesRemaining = 88;
    this.game.onKeyEvent("r");
    t.checkExpect(this.game.time, 0);
    t.checkExpect(this.game.flipTime, 0);
    t.checkExpect(this.game.score, 26);
    t.checkExpect(this.game.triesRemaining, 60);
    t.checkExpect(this.game.shuffledDeck.size(), 52);
    
    this.initData();
    this.game.shuffledDeck = new ArrayList<Card>();
    this.game.board();
    this.game.shuffle();
    t.checkExpect(this.game.shuffledDeck.size(), 52);
    this.game.removeCard(removeList);
    t.checkExpect(this.game.shuffledDeck.containsAll(removeList), false);
    this.game.score = 25;
    this.game.time = 67;
    this.game.triesRemaining = 80;
    this.game.onKeyEvent("a");
    t.checkExpect(this.game.time, 67);
    t.checkExpect(this.game.score, 25);
    t.checkExpect(this.game.triesRemaining, 80);
  }

  //tests for lastScene method
  void testLastScene(Tester t) {
    this.initData();
    this.game.score = 0;
    this.game.triesRemaining = 5;
    t.checkExpect(this.game.score, 0);
    t.checkExpect(this.game.triesRemaining, 5);
    ws1.placeImageXY(new FromFileImage("src/endScene.png"), 500, 300);
    ws1.placeImageXY(new TextImage("You CONCENTRATED!", 50, FontStyle.BOLD_ITALIC, Color.pink), 300, 150);
    t.checkExpect(game.lastScene("You CONCENTRATED!"), ws1);

    this.initData();
    this.game.score = 10;
    this.game.triesRemaining = 9;
    t.checkExpect(this.game.score, 10);
    t.checkExpect(this.game.triesRemaining, 9);
    ws1.placeImageXY(new FromFileImage("src/endScene.png"), 500, 300);
    ws1.placeImageXY(new TextImage("Concentrate More!", 50, FontStyle.BOLD_ITALIC, Color.pink), 300, 150);
    t.checkExpect(game.lastScene("Concentrate More!"), ws1);
  }
}