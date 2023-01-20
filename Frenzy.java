import tester.*;
import java.util.Random;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;

// class to represent a fish in the game
class Fish {
  Random rand = new Random();
  int size; // sizes range from 1-4
  Color color;
  int x = rand.nextInt(800);
  int y = rand.nextInt(600);
  int dir = rand.nextInt(2); // 0 = left, 0 = right

  Fish(int size, Color color, int x, int y, int dir) {
    this.size = size;
    this.color = color;
    this.x = x;
    this.y = y;
    this.dir = dir;
  }

  // draws a fish
  public WorldImage draw() {
    return new RectangleImage(this.size * 25, this.size * 25, "solid", this.color);
  }

  // moves a fish
  public Fish move(String key) {
    if (this.color == Color.RED) { // only the user fish is red
      if (key.equals("up")) {
        return new Fish(this.size, this.color, this.x, this.moveHelperY(this.y - 20), this.dir);
      }
      else if (key.equals("left")) {
        return new Fish(this.size, this.color, this.moveHelperX(this.x - 20), this.y, this.dir);
      }
      else if (key.equals("down")) {
        return new Fish(this.size, this.color, this.x, this.moveHelperY(this.y + 20), this.dir);
      }
      else if (key.equals("right")) {
        return new Fish(this.size, this.color, this.moveHelperX(this.x + 20), this.y, this.dir);
      }
      else {
        return this;
      }
    }
    else {
      if (dir == 0) {
        return new Fish(this.size, this.color, this.moveHelperX(this.x - 20), this.y, this.dir);
      }
      else {
        return new Fish(this.size, this.color, this.moveHelperX(this.x + 20), this.y, this.dir);
      }
    }
  }

  // controls the fish when it goes off screen left or right
  int moveHelperX(int newX) {
    int halfPixelSize = (this.size * 25) / 2;
    // handles right side
    if (newX - halfPixelSize > 800) {
      return 0 - halfPixelSize;
    }
    // handles the left side
    else if (newX < 0 - halfPixelSize) {
      return 800 + halfPixelSize;
    }
    else {
      return newX;
    }
  }

  // controls the fish when it goes off screen top or bottom
  int moveHelperY(int newY) {
    int halfPixelSize = (this.size * 25) / 2;
    // handles bottom
    if (newY + halfPixelSize > 600) {
      return 600 - halfPixelSize;
    }
    // handles top
    else if (newY - halfPixelSize < 0) {
      return 0 + halfPixelSize;
    }
    else {
      return newY;
    }
  }

  // checks if this fish is colliding with the userFish
  public boolean isNotColliding(Fish userFish) {
    if (userFish.size == this.size) {
      return true;
    }
    else {
      return this.isNotCollidingHelper(userFish);
    }
  }

  // helper for isNotColliding
  public boolean isNotCollidingHelper(Fish userFish) {
    int halfPixelSize = (this.size * 25) / 2;
    int userFishHalfPixelSize = (userFish.size * 25) / 2;
    boolean areColliding = !(this.x - halfPixelSize > userFish.x + userFishHalfPixelSize
            || this.x + halfPixelSize < userFish.x - userFishHalfPixelSize
            || this.y - halfPixelSize > userFish.y + userFishHalfPixelSize
            || this.y + halfPixelSize < userFish.y - userFishHalfPixelSize);

    if (areColliding) {
      return (userFish.size <= this.size);
    }
    else {
      return true;
    }
  }

  // checks if the userFish is the largest possible size (which ends game)
  public boolean winGame() {
    return this.size == 4;
  }

  // has the user fish been eaten?
  public boolean isUserEaten(Fish userFish) {
    int halfPixelSize = (this.size * 25) / 2;
    int userFishHalfPixelSize = (userFish.size * 25) / 2;

    boolean areColliding = !(this.x - halfPixelSize > userFish.x + userFishHalfPixelSize
            || this.x + halfPixelSize < userFish.x - userFishHalfPixelSize
            || this.y - halfPixelSize > userFish.y + userFishHalfPixelSize
            || this.y + halfPixelSize < userFish.y - userFishHalfPixelSize);

    return areColliding && (userFish.size < this.size);
  }

  // decides whether to grow the user fish
  public boolean shouldGrow(Fish userFish) {
    int halfPixelSize = (this.size * 25) / 2;
    int userFishHalfPixelSize = (userFish.size * 25) / 2;

    boolean areColliding = !(this.x - halfPixelSize > userFish.x + userFishHalfPixelSize
            || this.x + halfPixelSize < userFish.x - userFishHalfPixelSize
            || this.y - halfPixelSize > userFish.y + userFishHalfPixelSize
            || this.y + halfPixelSize < userFish.y - userFishHalfPixelSize);

    return areColliding && (userFish.size > this.size);
  }

  // grows the user fish
  public Fish growFish(boolean shouldGrow) {
    if (shouldGrow) {
      return new Fish(this.size + 1, this.color, this.x, this.y, this.dir);
    }
    else {
      return this;
    }
  }
}

interface IPred<T> {
  // asks a question about the given object
  boolean apply(T t);
}

interface IFunc<T, U> {
  // asks a question about the given object
  U apply(T t);
}

interface IFunc2<T, U, V> {
  // combines t and u and produces a v
  V apply(T t, U u);
}

// function object to draw a fish
class DrawFish implements IFunc2<Fish, WorldScene, WorldScene> {
  // places an image of a fish
  public WorldScene apply(Fish f, WorldScene u) {
    return u.placeImageXY(f.draw(), f.x, f.y);
  }
}

// function object to move a fish
class MoveFish implements IFunc<Fish, Fish> {
  String key;

  MoveFish(String key) {
    this.key = key;
  }

  // moves an image of a fish
  public Fish apply(Fish f) {
    return f.move(key);
  }
}

// function object that checks if a fish has eaten another
class FishCollision implements IPred<Fish> {
  Fish userFish;

  FishCollision(Fish userFish) {
    this.userFish = userFish;
  }

  // this = currentFish
  // fish = userFish

  // determines if a fish has been eaten
  public boolean apply(Fish f) {
    return f.isNotColliding(this.userFish);
  }
}

// function object to determine if you lost the game
class LoseGame implements IPred<Fish> {
  Fish userFish;

  LoseGame(Fish userFish) {
    this.userFish = userFish;
  }

  // determines if the user fish has been eaten
  public boolean apply(Fish f) {
    return f.isUserEaten(this.userFish);
  }
}

// function object that checks if the user fish should be grown
class GrowFish implements IPred<Fish> {
  Fish userFish;

  GrowFish(Fish userFish) {
    this.userFish = userFish;
  }
  // this = currentFish
  // fish = userFish

  // determines if a fish has been eaten
  public boolean apply(Fish f) {
    return f.shouldGrow(this.userFish);
  }
}

interface IList<T> {
  // filter this list by the given predicate
  IList<T> filter(IPred<T> pred);

  // maps a function onto each member of the list, producing a list of the results
  <U> IList<U> map(IFunc<T, U> fun);

  // combines the items in this list using the given function
  <U> U foldr(IFunc2<T, U, U> fun, U base);

  // does everything in this list pass the given predicate
  boolean andmap(IPred<T> pred);

  //does at least one thing in this list pass the given predicate
  boolean ormap(IPred<T> pred);

}

class MtList<T> implements IList<T> {
  // filter this list by the given predicate
  public IList<T> filter(IPred<T> pred) {
    return this;
  }

  // maps a function onto each member of the list, producing a list of the results
  public <U> IList<U> map(IFunc<T, U> fun) {
    return new MtList<U>();
  }

  // combines the items in this list using the given function
  public <U> U foldr(IFunc2<T, U, U> fun, U base) {
    return base;
  }

  // does everything in this list pass the given predicate
  public boolean andmap(IPred<T> pred) {
    return true;
  }

  // does at least one thing in this list pass the given predicate
  public boolean ormap(IPred<T> pred) {
    return false;
  }
}

class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  // filter this list by the given predicate
  public IList<T> filter(IPred<T> pred) {
    if (pred.apply(this.first)) {
      return new ConsList<T>(this.first, this.rest.filter(pred));
    }
    else {
      return this.rest.filter(pred);
    }
  }

  // maps a function onto each member of the list, producing a list of the results
  public <U> IList<U> map(IFunc<T, U> fun) {
    return new ConsList<U>(fun.apply(this.first), this.rest.map(fun));
  }

  // combines the items in this list using the given function
  public <U> U foldr(IFunc2<T, U, U> fun, U base) {
    return fun.apply(this.first, this.rest.foldr(fun, base));
  }

  // does everything in this list pass the given predicate
  public boolean andmap(IPred<T> pred) {
    return pred.apply(this.first) && this.rest.andmap(pred);
  }

  // does at least one T in this list pass the given predicate
  public boolean ormap(IPred<T> pred) {
    return pred.apply(this.first) || this.rest.ormap(pred);
  }
}

// represents the world state
class FishWorld extends World {
  // whatever changes during world program

  IList<Fish> fish;
  Fish userFish;

  FishWorld(IList<Fish> fish, Fish userFish) {
    this.fish = fish;
    this.userFish = userFish;
  }

  // draws the fish on the background
  public WorldScene makeScene() {
    return new ConsList<Fish>(this.userFish,
        this.fish).foldr(new DrawFish(), new WorldScene(800, 600));
  }

  // handles key input and is given the key that has been pressed
  public World onKeyEvent(String key) {
    return new FishWorld(this.fish.map(new MoveFish(key)), this.userFish.move(key));
  }

  // moves the background fish
  public World onTick() {
    // moves background fish
    IList<Fish> tempBackground = this.fish.map(new MoveFish("g"));

    boolean shouldGrowFish = tempBackground.ormap(new GrowFish(this.userFish));

    IList<Fish> updatedBackground = tempBackground.filter(new FishCollision(this.userFish));

    Fish updatedUserFish = this.userFish.growFish(shouldGrowFish);

    return new FishWorld(updatedBackground, updatedUserFish);
  }

  // ends the game
  public WorldEnd worldEnds() {
    if (this.userFish.winGame()) {
      return new WorldEnd(true, this.makeScene().placeImageXY(
          new TextImage("You Win!", 50, Color.RED), 400, 300));
    }
    else if (this.fish.ormap(new LoseGame(this.userFish))) {
      return new WorldEnd(true, this.makeScene().placeImageXY(
          new TextImage("You Lose!", 50, Color.RED), 400, 300));
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }
}

// examples
class ExamplesGame {
  Random rand  = new Random();
  Random rand2 = new Random(123);

  Fish testUserFish = new Fish(2, Color.RED, rand2.nextInt(800), 
      rand2.nextInt(600), rand2.nextInt(2));
  Fish testSize1Fish = new Fish(1, Color.GREEN, rand2.nextInt(800), 
      rand2.nextInt(600), rand2.nextInt(2));
  Fish testSize1FishNotRandom = new Fish(1, Color.GREEN, 10, 50, 1);
  Fish testSize2Fish = new Fish(2, Color.BLUE, rand2.nextInt(800), 
      rand2.nextInt(600), rand2.nextInt(2));
  Fish testSize3Fish = new Fish(3, Color.MAGENTA, rand2.nextInt(800), 
      rand2.nextInt(600), rand2.nextInt(2));
  Fish testSize4Fish = new Fish(4, Color.BLACK, rand2.nextInt(800), 
      rand2.nextInt(600), rand2.nextInt(2));
  

  Fish userFish = new Fish(2, Color.RED, rand.nextInt(800), 
      rand.nextInt(600), rand.nextInt(2));
  Fish size1Fish = new Fish(1, Color.GREEN, rand.nextInt(800), 
      rand.nextInt(600), rand.nextInt(2));
  Fish size2Fish = new Fish(2, Color.BLUE, rand.nextInt(800), 
      rand.nextInt(600), rand.nextInt(2));
  Fish size3Fish = new Fish(3, Color.MAGENTA, rand.nextInt(800), 
      rand.nextInt(600), rand.nextInt(2));
  Fish size4Fish = new Fish(4, Color.BLACK, rand.nextInt(800), 
      rand.nextInt(600), rand.nextInt(2)); // is not initially drawn

  IList<Fish> mtFish = new MtList<Fish>();
  IList<Fish> oneFish = new ConsList<Fish>(this.size1Fish, this.mtFish);
  IList<Fish> twoFish = new ConsList<Fish>(this.size2Fish, this.twoFish);
  IList<Fish> threeFish = new ConsList<Fish>(this.size3Fish, this.threeFish);

  IList<Fish> testOneFish = new ConsList<Fish>(this.testSize1Fish, this.mtFish);
  IList<Fish> testOneFishNotRandom = new ConsList<Fish>(this.testSize1FishNotRandom, this.mtFish);
  IList<Fish> testTwoFish = new ConsList<Fish>(this.testSize2Fish, this.testOneFish);
  IList<Fish> testThreeFish = new ConsList<Fish>(this.testSize3Fish, this.testThreeFish);

  // size1 fish
  IList<Fish> size1Fish1 = new ConsList<Fish>(new Fish(1, Color.GREEN, 
      rand.nextInt(800), rand.nextInt(600), rand.nextInt(2)), this.mtFish);
  IList<Fish> size1Fish2 = new ConsList<Fish>(new Fish(1, Color.GREEN, 
      rand.nextInt(800), rand.nextInt(600), rand.nextInt(2)), this.size1Fish1);
  IList<Fish> size1Fish3 = new ConsList<Fish>(new Fish(1, Color.GREEN, 
      rand.nextInt(800), rand.nextInt(600), rand.nextInt(2)), this.size1Fish2);
  IList<Fish> size1Fish4 = new ConsList<Fish>(new Fish(1, Color.GREEN, 
      rand.nextInt(800), rand.nextInt(600), rand.nextInt(2)), this.size1Fish3);
  IList<Fish> size1Fish5 = new ConsList<Fish>(new Fish(1, Color.GREEN, 
      rand.nextInt(800), rand.nextInt(600), rand.nextInt(2)), this.size1Fish4);
  IList<Fish> size1Fish6 = new ConsList<Fish>(new Fish(1, Color.GREEN, 
      rand.nextInt(800), rand.nextInt(600), rand.nextInt(2)), this.size1Fish5);

  // size2 fish
  IList<Fish> size2Fish1 = new ConsList<Fish>(new Fish(2, Color.BLUE, 
      rand2.nextInt(800), rand2.nextInt(600), rand2.nextInt(2)), this.size1Fish6);
  IList<Fish> size2Fish2 = new ConsList<Fish>(new Fish(2, Color.BLUE, 
      rand2.nextInt(800), rand2.nextInt(600), rand2.nextInt(2)), this.size2Fish1);
  IList<Fish> size2Fish3 = new ConsList<Fish>(new Fish(2, Color.BLUE, 
      rand2.nextInt(800), rand2.nextInt(600), rand2.nextInt(2)), this.size2Fish2);
  IList<Fish> size2Fish4 = new ConsList<Fish>(new Fish(2, Color.BLUE, 
      rand2.nextInt(800), rand2.nextInt(600), rand2.nextInt(2)), this.size2Fish3);

  // size3 fish
  IList<Fish> size3Fish1 = new ConsList<Fish>(new Fish(3, Color.MAGENTA, 
      rand.nextInt(800), rand.nextInt(600), rand.nextInt(2)), this.size2Fish4);
  IList<Fish> gameFish = new ConsList<Fish>(new Fish(3, Color.MAGENTA, 
      rand.nextInt(800), rand.nextInt(600), rand.nextInt(2)), this.size3Fish1);

  WorldScene ws0 = new WorldScene(800, 600)
          .placeImageXY(this.userFish.draw(), rand.nextInt(800), rand.nextInt(600));
  WorldScene ws0Test = new WorldScene(800, 600)
          .placeImageXY(this.testUserFish.draw(), this.testUserFish.x, this.testUserFish.y);
  WorldScene ws1 = new WorldScene(500, 500);

  DrawFish df = new DrawFish();

  FishWorld fw1Test = new FishWorld(this.testOneFish, this.testUserFish);
  FishWorld fw2Test = new FishWorld(this.testTwoFish, this.testUserFish);
  FishWorld fw4Test = new FishWorld(this.testThreeFish, this.testUserFish);

  FishWorld fw0 = new FishWorld(this.mtFish, this.userFish);
  FishWorld fw0Test = new FishWorld(this.mtFish, this.testUserFish);
  FishWorld fw1 = new FishWorld(this.oneFish, this.userFish);
  FishWorld fw3 = new FishWorld(this.threeFish, this.userFish);

  // examples for testing for growth
  Fish testGrowFish1 = new Fish(1, Color.GREEN, 400, 300, 0);
  Fish testGrowFish2 = new Fish(2, Color.RED, 400, 300, 0);
  Fish testGrowFish3 = new Fish(3, Color.BLUE, 100, 300, 0);
  Fish testGrowFish4 = new Fish(2, Color.GREEN, 400, 300, 0);
  Fish testGrowFish5 = new Fish(3, Color.RED, 400, 300, 0);

  IList<Fish> growFishList1 = new ConsList<Fish>(this.testGrowFish1, this.mtFish);
  IList<Fish> growFishList2 = new ConsList<Fish>(this.testGrowFish2, this.growFishList1);
  IList<Fish> growFishList3 =  new ConsList<Fish>(this.testGrowFish2, this.mtFish);
  IList<Fish> growFishList4 = new ConsList<Fish>(this.testGrowFish3, this.mtFish);

  FishWorld fwGrow1 = new FishWorld(this.growFishList1, this.testGrowFish2);
  FishWorld fwGrow2 = new FishWorld(this.mtFish, this.testGrowFish5);
  FishWorld fwGrow3 = new FishWorld(this.growFishList4, this.testGrowFish1);
  FishWorld fwGrow3Moved = new FishWorld(
      this.growFishList4.map(new MoveFish("g")), this.testGrowFish1);

  FishWorld fwEndGame1 = new FishWorld(this.gameFish, this.size4Fish);
  FishWorld fwEndGame2 = new FishWorld(this.growFishList3, this.testGrowFish1);

  // test big bang
  boolean testBigBang(Tester t) {
    FishWorld world = new FishWorld(this.gameFish, this.userFish);
    int worldWidth = 800;
    int worldHeight = 600;
    double tickRate = .1;
    return world.bigBang(worldWidth, worldHeight, tickRate);
  }

  // tests for draw()
  boolean testDraw(Tester t) {
    return t.checkExpect(this.userFish.draw(),
        new RectangleImage(50, 50, "solid", Color.RED))
            && t.checkExpect(this.size1Fish.draw(),
                new RectangleImage(25, 25, "solid", Color.GREEN))
            && t.checkExpect(this.size2Fish.draw(),
                new RectangleImage(50, 50, "solid", Color.BLUE))
            && t.checkExpect(this.size3Fish.draw(),
                new RectangleImage(75, 75, "solid", Color.MAGENTA))
            && t.checkExpect(this.size4Fish.draw(),
                new RectangleImage(100, 100, "solid", Color.BLACK));
  }

  // tests for apply()
  void testApply(Tester t) {
    t.checkExpect(this.df.apply(this.userFish, this.ws0),
            this.ws0.placeImageXY(this.userFish.draw(), this.userFish.x, this.userFish.y));
    t.checkExpect(this.df.apply(this.size1Fish, this.ws1),
            this.ws1.placeImageXY(this.size1Fish.draw(), this.size1Fish.x, this.size1Fish.y));
  }

  // tests for makeScene()
  void testMakeScene(Tester t) {
    t.checkExpect(this.fw0Test.makeScene(), this.ws0Test);
    t.checkExpect(this.fwGrow1.makeScene(),
          new WorldScene(800, 600).placeImageXY(this.testGrowFish1.draw(), 
              this.testGrowFish1.x, this.testGrowFish1.y)
                                  .placeImageXY(this.testGrowFish2.draw(), 
                                      this.testGrowFish2.x, this.testGrowFish2.y));
  }

  // tests for move()
  void testMove(Tester t) {
    Random rand = new Random(123);
    int randX = rand.nextInt(800);
    int randY = rand.nextInt(600);
    int randDir = rand.nextInt(2);

    t.checkExpect(this.testUserFish.move("up"),
        new Fish(2, Color.RED, randX, randY - 20, randDir));
    t.checkExpect(this.testUserFish.move("left"),
        new Fish(2, Color.RED, randX - 20, randY, randDir));
    t.checkExpect(this.testUserFish.move("down"),
        new Fish(2, Color.RED, randX, randY + 20, randDir));
    t.checkExpect(this.testUserFish.move("right"),
        new Fish(2, Color.RED, randX + 20, randY, randDir));
    t.checkExpect(this.testUserFish.move("c"), this.testUserFish);
  }

  // tests for onKeyEvent()
  void testOnKeyEvent(Tester t) {
    Random rand = new Random(123);
    int randX = rand.nextInt(800);
    int randY = rand.nextInt(600);
    int randDir = rand.nextInt(2);
    t.checkExpect(this.fw0Test.onKeyEvent("up"),
            new FishWorld(this.mtFish, new Fish(2, Color.RED, randX, randY - 20, randDir)));
    t.checkExpect(this.fw0Test.onKeyEvent("left"),
            new FishWorld(this.mtFish, new Fish(2, Color.RED, randX - 20, randY, randDir)));
    t.checkExpect(this.fw0Test.onKeyEvent("down"),
            new FishWorld(this.mtFish, new Fish(2, Color.RED, randX, randY + 20, randDir)));
    t.checkExpect(this.fw0Test.onKeyEvent("right"),
            new FishWorld(this.mtFish, new Fish(2, Color.RED, randX + 20, randY, randDir)));
  }

  // tests for moveHelperX()
  void testMoveHelperX(Tester t) {
    int halfPixelSize = (this.userFish.size * 25) / 2;
    t.checkExpect(this.userFish.moveHelperX(0 - halfPixelSize - 20), 800 + halfPixelSize);
    t.checkExpect(this.userFish.moveHelperX(400 - halfPixelSize + 20), 400 - halfPixelSize + 20);
    t.checkExpect(this.userFish.moveHelperX(400 - halfPixelSize - 20), 400 - halfPixelSize - 20);
    t.checkExpect(this.userFish.moveHelperX(800 + halfPixelSize + 20), 0 - halfPixelSize);
  }

  // tests for moveHelperY()
  void testMoveHelperY(Tester t) {
    int halfPixelSize = (this.userFish.size * 25) / 2;
    t.checkExpect(this.userFish.moveHelperY(0 + halfPixelSize - 20), 0 + halfPixelSize);
    t.checkExpect(this.userFish.moveHelperY(400 - 20), 400 - 20);
    t.checkExpect(this.userFish.moveHelperY(600 - halfPixelSize + 20), 600 - halfPixelSize);
    t.checkExpect(this.userFish.moveHelperY(400 + 20), 400 + 20);
  }

  // tests for onTick()
  void testOnTick(Tester t) {
    t.checkExpect(this.fw0.onTick(), this.fw0);
    t.checkExpect(this.fwGrow1.onTick(), this.fwGrow2);
  }

  // tests for isNotColliding
  void testIsNotColliding(Tester t) {
    t.checkExpect(this.testUserFish.isNotColliding(this.size2Fish), true);
    t.checkExpect(this.testGrowFish1.isNotColliding(this.testGrowFish1), true);
    t.checkExpect(this.testGrowFish1.isNotColliding(this.testGrowFish2), false);
  }

  // tests for isNotCollidingHelper
  void testIsNotCollidingHelper(Tester t) {
    t.checkExpect(this.testUserFish.isNotCollidingHelper(this.testUserFish), true);
    t.checkExpect(this.testGrowFish1.isNotCollidingHelper(this.testGrowFish2), false);
    t.checkExpect(this.testGrowFish2.isNotCollidingHelper(this.testGrowFish1), true);

  }

  // tests for winGame()
  void testWinGame(Tester t) {
    t.checkExpect(this.userFish.winGame(), false);
    t.checkExpect(this.size4Fish.winGame(), true);
  }

  // isUserEaten()
  void testIsUserEaten(Tester t) {
    t.checkExpect(this.testGrowFish1.isUserEaten(this.testGrowFish2), false);
    t.checkExpect(this.testGrowFish2.isUserEaten(this.testGrowFish1), true);
  }

  // tests for worldEnds
  void testWorldEnds(Tester t) {
    t.checkExpect(this.fw0.worldEnds(), new WorldEnd(false, this.fw0.makeScene()));
    t.checkExpect(this.fwEndGame1.worldEnds(),
        new WorldEnd(true, this.fwEndGame1.makeScene().placeImageXY(
            new TextImage("You Win!", 50, Color.RED), 400, 300)));
    t.checkExpect(this.fwEndGame2.worldEnds(),
        new WorldEnd(true, this.fwEndGame2.makeScene().placeImageXY(
            new TextImage("You Lose!", 50, Color.RED), 400, 300)));
  }

  // tests for shouldGrow
  void testShouldGrow(Tester t) {
    t.checkExpect(this.testGrowFish1.shouldGrow(this.testGrowFish2), true);
    t.checkExpect(this.testGrowFish1.shouldGrow(this.testGrowFish3), false);
    t.checkExpect(this.testGrowFish2.shouldGrow(this.testGrowFish3), false);
    t.checkExpect(this.testGrowFish2.shouldGrow(this.testGrowFish2), false);
  }

  // tests for growFish
  void testGrowFish(Tester t) {
    t.checkExpect(this.testGrowFish1.growFish(true), this.testGrowFish4);
    t.checkExpect(this.testGrowFish1.growFish(false), this.testGrowFish1);
  }
}