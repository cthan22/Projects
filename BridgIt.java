import javalib.impworld.*;
import java.awt.Color;
import java.util.*;
import javalib.worldimages.*;
import tester.Tester;

// class to represent a cell
class Cell {
  Color color;
  int size;
  Cell left;
  Cell right;
  Cell above;
  Cell below;

  Cell(Color color, int size) {
    this.color = color;
    this.size = size;
  }

  // Draws this cell onto the background
  WorldImage draw() {
    return new RectangleImage(this.size, this.size, OutlineMode.SOLID, this.color);
  }

  // is there a path from this cell to the given one?
  boolean hasPathTo(Cell dest, ArrayList<Cell> seen, Color color) {
    ArrayList<Cell> neighbors = new ArrayList<Cell>(Arrays.asList(
        this.left, this.right, this.above, this.below));
    for (Cell n : neighbors) {
      if (n != null && n.color == color) {
        if (!seen.contains(n)) {
          seen.add(n);
          // can get there in just one step || can get there on a path through n
          if (n == dest || n.hasPathTo(dest, seen, color)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}

// represents the state of BridgIt
class BridgItWorld extends World {
  int size;
  int cellSize = 50; // cell size (in pixels)
  ArrayList<ArrayList<Cell>> board;
  boolean turn = false; // false = player 1, true = player 2

  BridgItWorld(int size) {
    Utils u = new Utils();
    this.size = u.checkBoardSize(size, "Board size must be greater than or equal to 3 and odd");
    this.board = this.initBoard();
    this.linkCells();
  }

  // constructor for testing
  BridgItWorld(int size, ArrayList<ArrayList<Cell>> board) {
    Utils u = new Utils();
    this.size = u.checkBoardSize(size, "Board size must be greater than or equal to 3 and odd");
    this.board = board;
  }

  // creates the board
  ArrayList<ArrayList<Cell>> initBoard() {
    board = new ArrayList<ArrayList<Cell>>();
    Color color = null;
    for (int i = 0; i < this.size; i++) {
      ArrayList<Cell> row = new ArrayList<Cell>();
      for (int j = 0; j < this.size; j++) {
        if (i % 2 == 0) {
          if (j % 2 == 0) {
            color = Color.WHITE;
          }
          else {
            color = Color.MAGENTA;
          }
        }
        else {
          if (j % 2 == 0) {
            color = Color.PINK;
          }
          else {
            color = Color.WHITE;
          }
        }
        row.add(new Cell(color, this.cellSize));
      }
      board.add(row);
    }
    // initialize neighbors
    return board;
  }

  // links the cells to their neighbors
  // EFFECT: links the cells to their neighbors
  void linkCells() {
    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        Cell cell = this.board.get(i).get(j);

        // if cell is not at the top edge, link it to the cell above it
        if (i != 0) {
          cell.above = this.board.get(i - 1).get(j);
        }
        // if cell is not at the left edge, link it to the cell to the left of it
        if (j != 0) {
          cell.left = this.board.get(i).get(j - 1);
        }
        // if cell is not at the bottom edge, link it to the cell below it
        if (i != this.size - 1) {
          cell.below = this.board.get(i + 1).get(j);
        }
        // if cell is not at the right edge, link it to the cell to the right of it
        if (j != this.size - 1) {
          cell.right = this.board.get(i).get(j + 1);
        }
      }
    }
  }

  // draws the game
  public WorldScene makeScene() {
    WorldScene scene = this.getEmptyScene();

    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        WorldImage cell = this.board.get(i).get(j).draw();
        scene.placeImageXY(cell,
            j * this.cellSize + (this.cellSize / 2),
            i * this.cellSize + (this.cellSize / 2));
      }
    }
    return scene;
  }

  // handle mouse clicks
  public void onMouseClicked(Posn pos, String buttonName) {
    // rounds down to the near number
    // value represent a list index
    int x = pos.x / this.cellSize;
    int y = pos.y / this.cellSize;
    Cell cell = this.board.get(y).get(x);
    if (cell.color == Color.WHITE && buttonName.equals("LeftButton")) {
      if (cell.left == null || cell.right == null || cell.above == null || cell.below == null) {
        cell.color = Color.WHITE;
        this.turn = !this.turn;
      }
      else if (this.turn) {
        cell.color = Color.MAGENTA;
      }
      else {
        cell.color = Color.PINK;
      }

      if (this.checkWin()) {
        this.endOfWorld("Player " + (this.turn ? "2" : "1") + " wins!");
      }

      // change the turn
      this.turn = !this.turn;
    }
  }

  // handle key presses
  public void onKeyEvent(String key) {
    // if "r" is pressed, reset the game
    if (key.equals("r")) {
      this.initBoard();
      this.linkCells();
    }
  }

  // checks for a winning board
  public boolean checkWin() {
    // check for pink win
    // start at odd indices because they are pink (top to bottom)
    // increment by 2 because we only need to check every other row
    for (int i = 1; i < this.size; i += 2) {
      Cell leftCell = this.board.get(i).get(0);
      for (int j = 1; j < this.size; j += 2) {
        Cell rightCell = this.board.get(j).get(this.size - 1);
        if (leftCell.hasPathTo(rightCell, new ArrayList<Cell>(), leftCell.color)) {
          return true;
        }
      }
    }
    // check for magenta win
    for (int i = 1; i < this.size; i += 2) {
      Cell topCell = this.board.get(0).get(i);
      for (int j = 1; j < this.size; j += 2) {
        Cell bottomCell = this.board.get(this.size - 1).get(j);
        if (topCell.hasPathTo(bottomCell, new ArrayList<Cell>(), topCell.color)) {
          return true;
        }
      }
    }
    return false;
  }

  // overwrite lastScene
  public WorldScene lastScene(String s) {
    WorldScene scene = this.makeScene();
    WorldImage text = new TextImage(s, 5 * this.size, Color.BLACK);
    scene.placeImageXY(text, this.size * this.cellSize / 2, this.size * this.cellSize / 2);
    return scene;
  }
}

// class to enforce restrictions
class Utils {
  // checks if the given board size is valid (greater than or equal to 3 and odd)
  int checkBoardSize(int size, String message) {
    if (size >= 3 && size % 2 == 1) {
      return size;
    }
    else {
      throw new IllegalArgumentException(message);
    }
  }
}

// tests and examples
class ExamplesGame {
  // cell size (in pixels)
  int cellSize = 50;

  int size = 3;
  BridgItWorld bwTest = new BridgItWorld(this.size);


  Cell c0 = new Cell(Color.WHITE, this.cellSize);
  Cell c1 = new Cell(Color.MAGENTA, this.cellSize);
  Cell c2 = new Cell(Color.WHITE, this.cellSize);
  Cell c3 = new Cell(Color.PINK, this.cellSize);
  Cell c4 = new Cell(Color.WHITE, this.cellSize);
  Cell c5 = new Cell(Color.PINK, this.cellSize);
  Cell c6 = new Cell(Color.WHITE, this.cellSize);
  Cell c7 = new Cell(Color.MAGENTA, this.cellSize);
  Cell c8 = new Cell(Color.WHITE, this.cellSize);

  ArrayList<Cell> row0 = new ArrayList<Cell>(Arrays.asList(this.c0, this.c1, this.c2));
  ArrayList<Cell> row1 = new ArrayList<Cell>(Arrays.asList(this.c3, this.c4, this.c5));
  ArrayList<Cell> row2 = new ArrayList<Cell>(Arrays.asList(this.c6, this.c7, this.c8));

  ArrayList<ArrayList<Cell>> testBoard = new ArrayList<ArrayList<Cell>>(Arrays.asList(this.row0,
      this.row1,
      this.row2));

  BridgItWorld bwTest2 = new BridgItWorld(this.size, this.testBoard);

  // initializes data
  void initData() {
    this.bwTest = new BridgItWorld(this.size);

    this.c0 = new Cell(Color.WHITE, this.cellSize);
    this.c1 = new Cell(Color.MAGENTA, this.cellSize);
    this.c2 = new Cell(Color.WHITE, this.cellSize);
    this.c3 = new Cell(Color.PINK, this.cellSize);
    this.c4 = new Cell(Color.WHITE, this.cellSize);
    this.c5 = new Cell(Color.PINK, this.cellSize);
    this.c6 = new Cell(Color.WHITE, this.cellSize);
    this.c7 = new Cell(Color.MAGENTA, this.cellSize);
    this.c8 = new Cell(Color.WHITE, this.cellSize);

    this.row0 = new ArrayList<Cell>(Arrays.asList(this.c0, this.c1, this.c2));
    this.row1 = new ArrayList<Cell>(Arrays.asList(this.c3, this.c4, this.c5));
    this.row2 = new ArrayList<Cell>(Arrays.asList(this.c6, this.c7, this.c8));

    this.testBoard = new ArrayList<ArrayList<Cell>>(Arrays.asList(this.row0, this.row1, this.row2));
    this.bwTest2 = new BridgItWorld(this.size, this.testBoard);
  }

  // test bigBang
  void testGame(Tester t) {
    int size = 11;
    BridgItWorld bw = new BridgItWorld(size);
    bw.bigBang(size * this.cellSize, size * this.cellSize);
  }

  // tests for draw
  void testDraw(Tester t) {
    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        t.checkExpect(this.testBoard.get(i).get(j).draw(),
            new RectangleImage(this.cellSize,
                this.cellSize,
                OutlineMode.SOLID,
                this.testBoard.get(i).get(j).color));
      }
    }
  }

  // tests for initBoard
  void testInitBoard(Tester t) {
    this.initData();
    ArrayList<ArrayList<Cell>> board = this.bwTest.initBoard();
    t.checkExpect(board, this.testBoard);
  }

  // tests for makeScene
  void testMakeScene(Tester t) {
    this.initData();
    WorldScene scene = this.bwTest.getEmptyScene();

    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        WorldImage cell = this.testBoard.get(i).get(j).draw();
        scene.placeImageXY(cell,
            j * this.cellSize + (this.cellSize / 2),
            i * this.cellSize + (this.cellSize / 2));
      }
    }
    t.checkExpect(this.bwTest.makeScene(), scene);
  }

  // tests for linkCells
  void testLinkCells(Tester t) {
    this.initData();
    this.bwTest.initBoard();
    t.checkExpect(this.bwTest.board, this.testBoard);
    t.checkExpect(this.c0.left, null);
    t.checkExpect(this.c4.left, null);
    t.checkExpect(this.c4.right, null);
    t.checkExpect(this.c4.above, null);
    t.checkExpect(this.c4.below, null);

    this.bwTest.linkCells();

    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        Cell cell = this.testBoard.get(i).get(j);

        if (i != 0) {
          cell.above = this.testBoard.get(i - 1).get(j);
        }
        if (j != 0) {
          cell.left = this.testBoard.get(i).get(j - 1);
        }
        if (i != this.size - 1) {
          cell.below = this.testBoard.get(i + 1).get(j);
        }
        if (j != this.size - 1) {
          cell.right = this.testBoard.get(i).get(j + 1);
        }
      }
    }

    t.checkExpect(this.bwTest.board, this.testBoard);
    t.checkExpect(this.c0.left, null);
    t.checkExpect(this.c4.left, this.c3);
    t.checkExpect(this.c4.right, this.c5);
    t.checkExpect(this.c4.above, this.c1);
    t.checkExpect(this.c4.below, this.c7);
  }

  // tests for BridgItWorldConstructor
  void testBridgItWorldConstructor(Tester t) {
    String message = "Board size must be greater than or equal to 3 and odd";

    BridgItWorld bw = new BridgItWorld(3); // valid constructor example
    t.checkConstructorException(new IllegalArgumentException(message), "BridgItWorld", 1);
    t.checkConstructorException(new IllegalArgumentException(message), "BridgItWorld", 4);

    Utils u = new Utils();
    t.checkExpect(u.checkBoardSize(11, message), 11);
    t.checkException(new IllegalArgumentException(message), u, "checkBoardSize", 1, message);
    t.checkException(new IllegalArgumentException(message), u, "checkBoardSize", 4, message);
  }

  // tests for onMouseClicked
  void testOnMouseClicked(Tester t) {
    this.initData();
    this.bwTest2.linkCells();

    // tests for clicking on a border white cell
    t.checkExpect(this.c0.color, Color.WHITE);
    this.bwTest2.onMouseClicked(new Posn(0, 0), "LeftButton");
    t.checkExpect(this.c0.color, Color.WHITE);

    // tests for clicking on a white cell
    t.checkExpect(this.c4.color, Color.WHITE);
    this.bwTest2.onMouseClicked(new Posn(75, 75), "LeftButton");
    t.checkExpect(this.c4.color, Color.PINK);

    // test for clicking on a non-white cell
    this.bwTest2.onMouseClicked(new Posn(75, 75), "LeftButton");
    t.checkExpect(this.c4.color, Color.PINK);

    // test for click not being left button
    this.bwTest2.onMouseClicked(new Posn(75, 75), "Caroline");
    t.checkExpect(this.c4.color, Color.PINK);
  }

  // tests for onKeyEvent
  void testOnKeyEvent(Tester t) {
    this.initData();
    this.bwTest.initBoard();
    this.bwTest.linkCells();

    this.bwTest2.linkCells();

    // change one cell
    this.bwTest.onMouseClicked(new Posn(25, 25), "LeftButton");
    t.checkExpect(this.bwTest.board.equals(this.bwTest2.board), false);

    this.bwTest.onKeyEvent("r");
    t.checkExpect(this.bwTest.board, this.bwTest2.board);

    this.bwTest.onKeyEvent("n");
    t.checkExpect(this.bwTest.board, this.bwTest2.board);
  }

  // tests for checkWin
  void testCheckWin(Tester t) {
    this.initData();
    this.bwTest2.initBoard();
    this.bwTest2.linkCells();

    // tests a board that is not won
    t.checkExpect(this.bwTest2.checkWin(), false);

    // click correct squares for pink to win
    this.bwTest2.onMouseClicked(new Posn(75, 75), "LeftButton");
    t.checkExpect(this.bwTest2.checkWin(), true);

    this.initData();
    this.bwTest2.initBoard();
    this.bwTest2.linkCells();
    // click correct squares for magenta to win
    this.bwTest2.onMouseClicked(new Posn(0, 0), "LeftButton");
    this.bwTest2.onMouseClicked(new Posn(75, 75), "LeftButton");
    t.checkExpect(this.bwTest2.checkWin(), true);
  }

  // tests for hasPathTo
  void testHasPathTo(Tester t) {
    this.initData();
    this.bwTest2.initBoard();
    this.bwTest2.linkCells();
    Cell c3 = this.bwTest2.board.get(1).get(0);
    Cell c5 = this.bwTest2.board.get(1).get(this.size - 1);
    Cell c7 = this.bwTest2.board.get(this.size - 1).get(1);
    t.checkExpect(c3.hasPathTo(c7, new ArrayList<Cell>(), Color.PINK), false);
    t.checkExpect(c3.hasPathTo(c5, new ArrayList<Cell>(), Color.PINK), false);
    this.bwTest2.onMouseClicked(new Posn(75, 75), "LeftButton");
    t.checkExpect(c3.hasPathTo(c7, new ArrayList<Cell>(), Color.PINK), false);
    t.checkExpect(c3.hasPathTo(c5, new ArrayList<Cell>(), Color.PINK), true);
    t.checkExpect(c3.hasPathTo(c5, new ArrayList<Cell>(), Color.MAGENTA), false);
  }

  // tests for lastScene
  void testLastScene(Tester t) {
    this.initData();
    this.bwTest2.initBoard();
    WorldScene lastScene = this.bwTest2.lastScene("Player 1 wins!");
    WorldImage text = new TextImage("Player 1 wins!", 5 * this.size, Color.BLACK);
    WorldScene testScene = this.bwTest2.makeScene();
    testScene.placeImageXY(text, this.size * this.cellSize / 2, this.size * this.cellSize / 2);
    t.checkExpect(lastScene, testScene);
  }
}