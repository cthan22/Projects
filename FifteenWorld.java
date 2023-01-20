import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.*;

// Represents an individual tile
class Tile {
  // The number on the tile.  Use 0 to represent the hole
  int value;

  // Tile constructor
  Tile(int value) {
    this.value = value;
  }

  // Draws this tile onto the background at the specified logical coordinates
  WorldImage draw(int size) {
    String strVal = Integer.toString(this.value);
    // if the tile value is 0, draw it as an empty tile
    if (this.value == 0) {
      strVal = "";
    }

    return new OverlayImage(
        new RectangleImage(size, size, OutlineMode.OUTLINE, Color.WHITE),
        new OverlayImage(
            new TextImage(strVal, 30, Color.BLACK),
            new RectangleImage(size, size, OutlineMode.SOLID, Color.LIGHT_GRAY)));
  }

  // compares the value of this tile to a given value
  boolean sameValue(int value) {
    return this.value == value;
  }
}

// represents the world state
class FifteenGame extends World {
  // represents the rows of tiles
  ArrayList<ArrayList<Tile>> tiles = this.makeTiles();
  ArrayList<String> history = new ArrayList<>();

  FifteenGame() { }

  FifteenGame(ArrayList<ArrayList<Tile>> tiles, ArrayList<String> history) {
    this.tiles = tiles;
    this.history = history;
  }

  // makes tiles
  ArrayList<ArrayList<Tile>> makeTiles() {
    ArrayList<ArrayList<Tile>> tiles = new ArrayList<ArrayList<Tile>>();
    ArrayList<Integer> nums = makeTileNums();

    Random rand = new Random();

    for (int i = 0; i < 4; i++) {
      ArrayList<Tile> row = new ArrayList<Tile>();
      for (int j = 0; j < 4; j++) {
        // remove a random number from nums
        row.add(new Tile(nums.remove(rand.nextInt(nums.size()))));
      }
      tiles.add(row);
    }
    return tiles;
  }

  // generates a list of tile numbers
  ArrayList<Integer> makeTileNums() {
    ArrayList<Integer> nums = new ArrayList<Integer>();
    // add numbers 0-15 to nums (0 represents the empty tile)
    for (int i = 0; i < 16; i++) {
      nums.add(i);
    }
    return nums;
  }

  // draws the game
  public WorldScene makeScene() {
    WorldScene scene = this.getEmptyScene();

    // tile size (in pixels)
    int size = 100;

    for (int i = 0; i < this.tiles.size(); i++) {
      for (int j = 0; j < this.tiles.size(); j++) {
        WorldImage tile = this.tiles.get(i).get(j).draw(size);
        scene.placeImageXY(tile, j * 100 + (size / 2), i * 100 + (size / 2));
      }
    }
    return scene;
  }

  // handles keystrokes (moves the hole)
  public void onKeyEvent(String key) {
    if (key.equals("u")) {
      // only undo the move if there is a move to undo
      if (history.size() > 0) {
        String lastMove = history.remove(history.size() - 1);
        String moveToSend = "";
        if (lastMove.equals("left")) {
          moveToSend = "right";
        }
        else if (lastMove.equals("right")) {
          moveToSend = "left";
        }
        else if (lastMove.equals("up")) {
          moveToSend = "down";
        }
        else if (lastMove.equals("down")) {
          moveToSend = "up";
        }
        this.onKeyEventHelper(moveToSend, false);
      }
    }
    else {
      this.onKeyEventHelper(key, true);
    }
  }

  // helps onKeyEvent()
  public void onKeyEventHelper(String key, Boolean shouldSave) {
    for (ArrayList<Tile> at : this.tiles) {
      for (Tile t : at) {
        boolean moveFailed = true;
        // find the hole
        if (t.value == 0) {
          int holeIndex = at.indexOf(t);
          if (key.equals("left")) {
            // if hole is not at the left edge
            if (holeIndex != 0) {
              // swap the hole with the tile to the left
              Tile temp = at.get(holeIndex - 1);
              at.set(holeIndex - 1, t);
              at.set(holeIndex, temp);
              moveFailed = false;
            }
          } else if (key.equals("right")) {
            // if hole is not at the right edge
            if (holeIndex != this.tiles.size() - 1) {
              // swap the hole with the tile to the right
              Tile temp = at.get(holeIndex + 1);
              at.set(holeIndex + 1, t);
              at.set(holeIndex, temp);
              moveFailed = false;
            }
          } else if (key.equals("up")) {
            int rowIndex = this.tiles.indexOf(at);
            // if hole is not at the top edge
            if (rowIndex != 0) {
              // swap the hole with the tile above
              ArrayList<Tile> tempList = this.tiles.get(rowIndex - 1);
              Tile tempTile = tempList.get(holeIndex);
              tempList.set(holeIndex, t);
              at.set(holeIndex, tempTile);
              moveFailed = false;
            }
          } else if (key.equals("down")) {
            int rowIndex = this.tiles.indexOf(at);
            // if hole is not at the bottom edge
            if (rowIndex != this.tiles.size() - 1) {
              // swap the hole with the tile below
              ArrayList<Tile> tempList = this.tiles.get(rowIndex + 1);
              Tile tempTile = tempList.get(holeIndex);
              tempList.set(holeIndex, t);
              at.set(holeIndex, tempTile);
              moveFailed = false;
            }
          }
          if (shouldSave && !moveFailed) {
            history.add(key);
          }

          if (this.winGame()) {
            this.endOfWorld("You win!");
          }

          return;
        }
      }
    }
  }

  // ends the game
  public WorldEnd worldEnds() {
    if (this.winGame()) {
      WorldScene endScene = new WorldScene(400, 400);
      endScene.placeImageXY(
          new TextImage("You Win!", 50, Color.RED), 200, 200);
      return new WorldEnd(true, endScene);
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  // has the game been won?
  public boolean winGame() {
    ArrayList<Integer> nums = makeTileNums();
    // move 0 from front to back of list
    nums.remove(0);
    nums.add(0);

    int counter = 0;
    for (int i = 0; i < this.tiles.size(); i++) {
      for (int j = 0; j < this.tiles.size(); j++) {
        boolean match = this.tiles.get(i).get(j).sameValue(nums.get(counter));
        if (!match) {
          return false;
        }
        counter++;
      }
    }
    return true;
  }

  // renders the last scene
  public WorldScene lastScene(String s) {
    WorldScene endScene = new WorldScene(400, 400);
    endScene.placeImageXY(
        new TextImage(s, 50, Color.RED), 200, 200);
    return endScene;
  }
}

// examples and tests
class ExampleFifteenGame {

  Tile t0 = new Tile(0);
  Tile t1 = new Tile(1);
  Tile t2 = new Tile(2);
  Tile t3 = new Tile(3);
  Tile t4 = new Tile(4);
  Tile t5 = new Tile(5);
  Tile t6 = new Tile(6);
  Tile t7 = new Tile(7);
  Tile t8 = new Tile(8);
  Tile t9 = new Tile(9);
  Tile t10 = new Tile(10);
  Tile t11 = new Tile(11);
  Tile t12 = new Tile(12);
  Tile t13 = new Tile(13);
  Tile t14 = new Tile(14);
  Tile t15 = new Tile(15);

  ArrayList<Tile> at1 = new ArrayList<Tile>(Arrays.asList(this.t1, this.t2, this.t3));
  ArrayList<Tile> at2 = new ArrayList<Tile>(Arrays.asList(this.t4, this.t0, this.t5));
  ArrayList<Tile> at3 = new ArrayList<Tile>(Arrays.asList(this.t6, this.t7, this.t8));

  ArrayList<Tile> at4 = new ArrayList<Tile>(Arrays.asList(this.t0, this.t4, this.t5));
  ArrayList<Tile> at5 = new ArrayList<Tile>(Arrays.asList(this.t4, this.t5, this.t0));

  ArrayList<Tile> at6 = new ArrayList<Tile>(Arrays.asList(this.t1, this.t0, this.t3));
  ArrayList<Tile> at7 = new ArrayList<Tile>(Arrays.asList(this.t4, this.t2, this.t5));

  ArrayList<Tile> at8 = new ArrayList<Tile>(Arrays.asList(this.t4, this.t7, this.t5));
  ArrayList<Tile> at9 = new ArrayList<Tile>(Arrays.asList(this.t6, this.t0, this.t8));

  ArrayList<Tile> atOne = new ArrayList<Tile>(Arrays.asList(this.t1));


  ArrayList<ArrayList<Tile>> aat = new ArrayList<ArrayList<Tile>>(
      Arrays.asList(this.at1, this.at2, this.at3));
  ArrayList<ArrayList<Tile>> aatLeft = new ArrayList<ArrayList<Tile>>(
      Arrays.asList(this.at1, this.at4, this.at3));
  ArrayList<ArrayList<Tile>> aatRight = new ArrayList<ArrayList<Tile>>(
      Arrays.asList(this.at1, this.at5, this.at3));
  ArrayList<ArrayList<Tile>> aatUp = new ArrayList<ArrayList<Tile>>(
      Arrays.asList(this.at6, this.at7, this.at3));
  ArrayList<ArrayList<Tile>> aatDown = new ArrayList<ArrayList<Tile>>(
      Arrays.asList(this.at1, this.at8, this.at9));
  ArrayList<ArrayList<Tile>> aatOne = new ArrayList<ArrayList<Tile>>(
      Arrays.asList(this.atOne));


  FifteenGame game1 = new FifteenGame(this.aat, new ArrayList<String>());
  FifteenGame gameLeft = new FifteenGame(this.aatLeft, new ArrayList<String>());
  FifteenGame gameRight = new FifteenGame(this.aatRight, new ArrayList<String>());
  FifteenGame gameUp = new FifteenGame(this.aatUp, new ArrayList<String>());
  FifteenGame gameDown = new FifteenGame(this.aatDown, new ArrayList<String>());
  FifteenGame gameOne = new FifteenGame(this.aatOne, new ArrayList<String>());


  ArrayList<Tile> atWin0 = new ArrayList<Tile>(Arrays.asList(this.t1, this.t2, this.t3, this.t4));
  ArrayList<Tile> atWin1 = new ArrayList<Tile>(Arrays.asList(this.t5, this.t6, this.t7, this.t8));
  ArrayList<Tile> atWin2 = new ArrayList<Tile>(Arrays.asList(this.t9, this.t10, this.t11, this.t12));
  ArrayList<Tile> atAlmostWin3 = new ArrayList<Tile>(Arrays.asList(this.t13, this.t14, this.t0, this.t15));

  ArrayList<ArrayList<Tile>> aatAlmostWin = new ArrayList<ArrayList<Tile>>(Arrays.asList(this.atWin0, this.atWin1, this.atWin2, this.atAlmostWin3));

  FifteenGame almostGameWin = new FifteenGame(this.aatAlmostWin, new ArrayList<String>());

  void initData() {
    this.t0 = new Tile(0);
    this.t1 = new Tile(1);
    this.t2 = new Tile(2);
    this.t3 = new Tile(3);
    this.t4 = new Tile(4);
    this.t5 = new Tile(5);
    this.t6 = new Tile(6);
    this.t7 = new Tile(7);
    this.t8 = new Tile(8);

    this.at1 = new ArrayList<Tile>(Arrays.asList(this.t1, this.t2, this.t3));
    this.at2 = new ArrayList<Tile>(Arrays.asList(this.t4, this.t0, this.t5));
    this.at3 = new ArrayList<Tile>(Arrays.asList(this.t6, this.t7, this.t8));

    this.at4 = new ArrayList<Tile>(Arrays.asList(this.t0, this.t4, this.t5));
    this.at5 = new ArrayList<Tile>(Arrays.asList(this.t4, this.t5, this.t0));

    this.at6 = new ArrayList<Tile>(Arrays.asList(this.t1, this.t0, this.t3));
    this.at7 = new ArrayList<Tile>(Arrays.asList(this.t4, this.t2, this.t5));

    this.at8 = new ArrayList<Tile>(Arrays.asList(this.t4, this.t7, this.t5));
    this.at9 = new ArrayList<Tile>(Arrays.asList(this.t6, this.t0, this.t8));


    this.aat = new ArrayList<ArrayList<Tile>>(Arrays.asList(this.at1, this.at2, this.at3));
    this.aatLeft = new ArrayList<ArrayList<Tile>>(Arrays.asList(this.at1, this.at4, this.at3));
    this.aatRight = new ArrayList<ArrayList<Tile>>(Arrays.asList(this.at1, this.at5, this.at3));
    this.aatUp = new ArrayList<ArrayList<Tile>>(Arrays.asList(this.at6, this.at7, this.at3));
    this.aatDown = new ArrayList<ArrayList<Tile>>(Arrays.asList(this.at1, this.at8, this.at9));

    this.game1 = new FifteenGame(this.aat, new ArrayList<String>());
    this.gameLeft = new FifteenGame(this.aatLeft, new ArrayList<String>(Arrays.asList("left")));
    this.gameRight = new FifteenGame(this.aatRight, new ArrayList<String>(Arrays.asList("right")));
    this.gameUp = new FifteenGame(this.aatUp, new ArrayList<String>(Arrays.asList("up")));
    this.gameDown = new FifteenGame(this.aatDown, new ArrayList<String>(Arrays.asList("down")));

    this.atWin0 = new ArrayList<Tile>(Arrays.asList(this.t1, this.t2, this.t3, this.t4));
    this.atWin1 = new ArrayList<Tile>(Arrays.asList(this.t5, this.t6, this.t7, this.t8));
    this.atWin2 = new ArrayList<Tile>(Arrays.asList(this.t9, this.t10, this.t11, this.t12));
    this.atAlmostWin3 = new ArrayList<Tile>(Arrays.asList(this.t13, this.t14, this.t0, this.t15));

    this.aatAlmostWin = new ArrayList<ArrayList<Tile>>(Arrays.asList(this.atWin0, this.atWin1, this.atWin2, this.atAlmostWin3));

    this.almostGameWin = new FifteenGame(this.aatAlmostWin, new ArrayList<String>());
  }

  // test big bang
  void testGame(Tester t) {
    FifteenGame g = new FifteenGame();
    g.bigBang(400, 400);
  }

  // tests for draw
  void testDraw(Tester t) {
    t.checkExpect(new Tile(1).draw(100), new OverlayImage(
        new RectangleImage(100, 100, OutlineMode.OUTLINE, Color.WHITE),
        new OverlayImage(
            new TextImage("1", 30, Color.BLACK),
            new RectangleImage(100, 100, OutlineMode.SOLID, Color.LIGHT_GRAY))));
    t.checkExpect(new Tile(5).draw(100), new OverlayImage(
        new RectangleImage(100, 100, OutlineMode.OUTLINE, Color.WHITE),
        new OverlayImage(
            new TextImage("5", 30, Color.BLACK),
            new RectangleImage(100, 100, OutlineMode.SOLID, Color.LIGHT_GRAY))));
  }

  // tests for makeTiles
  void testMakeTiles(Tester t) {
    FifteenGame g = new FifteenGame();
    ArrayList<Integer> nums = new ArrayList<Integer>();

    // add numbers 0-15 to nums (0 represents the empty tile)
    for (int i = 0; i < 16; i++) {
      nums.add(i);
    }

    for (ArrayList<Tile> at : g.makeTiles()) {
      for (Tile tile : at) {
        nums.remove(nums.indexOf(tile.value));
      }
    }

    // if nums is empty, then all tiles were created
    t.checkExpect(nums.size(), 0);
  }

  // tests for makeTileNums
  void testMakeTileNums(Tester t) {
    FifteenGame g = new FifteenGame();
    ArrayList<Integer> nums = new ArrayList<Integer>();
    // add numbers 0-15 to nums (0 represents the empty tile)
    for (int i = 0; i < 16; i++) {
      nums.add(i);
    }

    t.checkExpect(g.makeTileNums(), nums);
  }

  // tests for makeScene
  void testMakeScene(Tester t) {
    WorldScene testScene = gameOne.getEmptyScene();
    int size = 100;

    WorldImage tile = new Tile(1).draw(size);
    testScene.placeImageXY(tile, 100 + (size / 2), 100 + (size / 2));

    t.checkExpect(gameOne.makeScene(), testScene);
  }

  // tests for onKeyEvent
  void testOnKeyEvent(Tester t) {
    this.initData();
    t.checkExpect(this.game1, this.game1);
    this.game1.onKeyEvent("left");
    t.checkExpect(this.game1, this.gameLeft);
    // tests a failed 'left'
    FifteenGame tempLeft = this.gameLeft;
    this.game1.onKeyEvent("left");
    t.checkExpect(this.game1, tempLeft);

    this.initData();
    this.game1.onKeyEvent("right");
    t.checkExpect(this.game1, this.gameRight);
    // tests a failed 'right'
    FifteenGame tempRight = this.gameRight;
    this.game1.onKeyEvent("right");
    t.checkExpect(this.game1, tempRight);

    this.initData();
    this.game1.onKeyEvent("up");
    t.checkExpect(this.game1, this.gameUp);
    // tests a failed 'up'
    FifteenGame tempUp = this.gameUp;
    this.game1.onKeyEvent("up");
    t.checkExpect(this.game1, tempUp);

    this.initData();
    this.game1.onKeyEvent("down");
    t.checkExpect(this.game1, this.gameDown);
    // tests a failed 'down'
    FifteenGame tempDown = this.gameDown;
    this.game1.onKeyEvent("down");
    t.checkExpect(this.game1, tempDown);

    this.initData();
    this.game1.onKeyEvent("down");
    this.game1.onKeyEvent("u");
    t.checkExpect(this.game1, this.game1);

    this.initData();
    this.game1.onKeyEvent("c");
    t.checkExpect(this.game1, this.game1);
  }

  // tests for onKeyEventHelper
  void testOnKeyEventHelper(Tester t) {
    this.initData();
    this.game1.onKeyEventHelper("left", true);
    t.checkExpect(this.game1, this.gameLeft);

    this.initData();
    this.game1.onKeyEventHelper("right", true);
    t.checkExpect(this.game1, this.gameRight);

    this.initData();
    this.game1.onKeyEventHelper("up", true);
    t.checkExpect(this.game1, this.gameUp);

    this.initData();
    this.game1.onKeyEventHelper("down", true);
    t.checkExpect(this.game1, this.gameDown);

    this.gameDown.onKeyEventHelper("u", false);
    t.checkExpect(this.gameDown, this.game1);

    this.initData();
    FifteenGame temp = this.game1;
    temp.onKeyEventHelper("c", true);
    t.checkExpect(temp, this.game1);
  }

  // tests for lastScene
  void testLastScene(Tester t) {
    this.initData();
    WorldScene scene = this.game1.lastScene("You win!");
    WorldScene testScene = new WorldScene(400, 400);
    testScene.placeImageXY(
        new TextImage("You win!", 50, Color.RED), 200, 200);
    t.checkExpect(scene, testScene);
  }

  // tests for winGame
  void testWinGame(Tester t) {
    this.initData();
    t.checkExpect(this.almostGameWin.winGame(), false);
    this.almostGameWin.onKeyEvent("right");
    t.checkExpect(this.almostGameWin.winGame(), true);
  }

  // tests for sameValue
  void testSameValue(Tester t) {
    t.checkExpect(this.t0.sameValue(0), true);
    t.checkExpect(this.t0.sameValue(1), false);
  }
}
