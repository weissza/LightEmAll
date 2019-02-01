import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javalib.impworld.*;
import javalib.worldimages.*;
import tester.Tester;

class LightEmAll extends World {
  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all nodes
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree
  ArrayList<Edge> mst;
  // the width and height of the board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerX; // y
  int powerY; // x
  int radius;
  Random r;

  // amount of time in the game
  int time;

  LightEmAll(int width, int height) {

    this.width = width;
    this.height = height;
    this.powerX = 0;
    this.powerY = 0;
    this.radius = 0;
    this.time = 0;
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.r = new Random();
  }

  // with random for testing
  LightEmAll(int width, int height, Random r) {

    this.width = width;
    this.height = height;
    this.powerX = 0;
    this.powerY = 0;
    this.radius = 0;
    this.time = 0;
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.r = r;
  }

  // Returns the drawn Board
  WorldImage draw() {
    WorldImage tmpBoard = new EmptyImage();
    this.board.get(powerY).get(powerX).light(this.board.get(powerY).get(powerX), this.radius,
        this.radius, new ArrayList<>());
    for (ArrayList<GamePiece> ar : this.board) {
      WorldImage tmpLine = new EmptyImage();
      for (GamePiece g : ar) {
        WorldImage piece = g.draw();
        WorldImage star = new StarImage(35, 7, OutlineMode.OUTLINE, Color.BLUE);
        if (g.match(this.powerX, this.powerY)) {
          piece = new OverlayImage(star, piece);
        }
        tmpLine = new BesideImage(tmpLine, piece);
      }
      tmpBoard = new AboveImage(tmpBoard, tmpLine);
    }
    if (this.isValid()) {
      tmpBoard = new OverlayImage(
          new AboveImage(new TextImage("YOU WON!", 10 * this.width, Color.RED),
              new TextImage("press enter to restart", 5 * this.width, Color.RED)),
          tmpBoard);
    }
    return tmpBoard;
  }

  // EFFECT: Makes a new board of cells
  void makeBoard() {
    for (int i = 0; i < this.height; i++) { // i = y col
      ArrayList<GamePiece> tmp = new ArrayList<>();
      for (int j = 0; j < this.width; j++) { // j = x row
        tmp.add(new GamePiece(j, i, false, false, false, false));
      }
      board.add(tmp);
    }
  }

  // checks every game piece in the board is valid
  boolean isValid() {
    for (ArrayList<GamePiece> ar : board) {
      for (GamePiece g : ar) {
        if (!g.isValid() || g.litAmount == 0) {
          return false;
        }
      }
    }
    return true;
  }

  // EFFECT: scrambles the board
  void scramble() {
    for (ArrayList<GamePiece> ar : board) {
      for (GamePiece g : ar) {
        int s = r.nextInt(3);
        for (int i = 0; i < s; i++) {
          g.rotate();
        }
      }
    }
  }

  // EFFECT: Makes a hard coded board
  void makeBoardHardCode() {
    // Random r = new Random();
    for (int i = 0; i < this.height; i++) { // i = y row
      ArrayList<GamePiece> tmp = new ArrayList<>();
      for (int j = 0; j < this.width; j++) { // j = x col
        if (i == this.height / 2 && j == 0) {
          tmp.add(new GamePiece(j, i, true, true, true, false));
        }
        else if (i == this.height / 2 && j == this.width - 1) {
          tmp.add(new GamePiece(j, i, true, false, true, true));
        }
        else if (i == 0) {
          tmp.add(new GamePiece(j, i, false, false, true, false));
        }
        else if (i == this.width - 1) {
          tmp.add(new GamePiece(j, i, true, false, false, false));
        }
        else if (i == this.height / 2) {
          tmp.add(new GamePiece(j, i, true, true, true, true));
        }
        else {
          tmp.add(new GamePiece(j, i, true, false, true, false));
        }
      }
      board.add(tmp);
    }
  }

  // EFFECT: Links the board
  void initlink() {
    for (ArrayList<GamePiece> ar : board) {
      for (GamePiece g : ar) {
        // top condition
        if (g.y == 0) {
          // making sure its not a top edge
        }
        else {
          GamePiece tmp = board.get(g.y - 1).get(g.x);
          g.add(tmp, "top");
        }

        // right condition
        if (g.x == this.width - 1) {
          // making sure its not a right edge
        }
        else {
          GamePiece tmp = board.get(g.y).get(g.x + 1);
          g.add(tmp, "right");
        }

        // bottom condition
        if (g.y == this.height - 1) {
          // making sure its not a bottom edge
        }
        else {
          GamePiece tmp = board.get(g.y + 1).get(g.x);
          g.add(tmp, "bottom");
        }

        // left condition
        if (g.x == 0) {
          // making sure its not a left edge
        }
        else {
          GamePiece tmp = board.get(g.y).get(g.x - 1);
          g.add(tmp, "left");
        }
        // this.radius = this.calcEffectiveRadius();
      }
    }
  }

  // EFFECT: Draws the scene every tick
  @Override
  public WorldScene makeScene() {
    WorldImage board = this.draw();
    WorldScene ws = this.getEmptyScene();
    ws.placeImageXY(board, width * 35, height * 35);
    ws.placeImageXY(new TextImage(Integer.toString(time), 20, Color.RED), 18, 15);
    return ws;
  }

  // resets the line colors
  void reset() {
    for (ArrayList<GamePiece> ar : this.board) {
      for (GamePiece g : ar) {
        g.litAmount = 0;
      }
    }
  }

  // EFFECT: processes a click
  public void onMouseClicked(Posn pos, String buttonName) {
    if (!this.isValid()) {
      int posx = pos.x / 70;
      int posy = pos.y / 70;
      if (posy >= height) {
        return;
      }
      if (posx >= width) {
        return;
      }
      this.board.get(posy).get(posx).rotate();
      this.reset();
    }
  }

  // EFFECT: moves the location of the power station
  public void onKeyEvent(String key) {
    if (!this.isValid()) {
      if (key.equals("up")) {
        if (this.powerY - 1 >= 0) {
          if (this.board.get(powerY).get(powerX).topNeighbor.bottom
              && this.board.get(powerY).get(powerX).top) {
            this.powerY -= 1;
          }

        }
      }
      else if (key.equals("right")) {
        if (this.powerX + 1 <= this.width - 1) {

          if (this.board.get(powerY).get(powerX).rightNeighbor.left
              && this.board.get(powerY).get(powerX).right) {
            this.powerX += 1;
          }
        }
      }
      else if (key.equals("down")) {
        if (this.powerY + 1 < this.height) {
          if (this.board.get(powerY).get(powerX).bottomNeighbor.top
              && this.board.get(powerY).get(powerX).bottom) {
            this.powerY += 1;
          }
        }
      }
      else if (key.equals("left")) {
        if (this.powerX - 1 >= 0) {
          if (this.board.get(powerY).get(powerX).leftNeighbor.right
              && this.board.get(powerY).get(powerX).left) {
            this.powerX -= 1;
          }
        }
      }

    }

    else if (key.equals("enter")) {
      this.board = new ArrayList<>();
      this.makeBoard();
      this.initlink();
      this.getEdges();
      this.randomEdge();
      this.drawEdgeList(this.kruskal());
      this.radius = this.calcEffectiveRadius();
      this.scramble();
      this.powerX = 0;
      this.powerY = 0;
      this.time = 0;
    }

  }

  //EFFECT: Ticks forward the second counter
  public void onTick() {
    if (!this.isValid()) {
      time++;
    }
  }

  // EFFECT: set this's radius to the calculated radius
  int calcEffectiveRadius() {
    GamePiece start = this.board.get(this.powerY).get(powerX);

    GamePiece deepestFromStart = this.farthestPiece(start);

    GamePiece deepestFromDeepest = this.farthestPiece(deepestFromStart);

    int diameter = this.findWidth(deepestFromStart, deepestFromDeepest);
    if (diameter < 0) {
      diameter = 0;
    }
    int radius = (diameter / 2) + 1;
    return radius;
  }

  //returns the farthest game piece from the given piece on this board 
  GamePiece farthestPiece(GamePiece from) {
    ArrayList<GamePiece> queue = new ArrayList<GamePiece>();
    ArrayList<GamePiece> seen = new ArrayList<GamePiece>();
    queue.add(from);
    GamePiece farthest = from;

    while (!queue.isEmpty()) {
      GamePiece next = queue.remove(0);
      if (queue.isEmpty() && next.neighborList().size() == 1
          && seen.contains(next.neighborList().get(0))) {
        farthest = next;
      }
      else if (seen.contains(next)) {
        // do nothing if next has already been visited
      }
      else {
        seen.add(next);
        for (GamePiece g : next.neighborList()) {
          if (!seen.contains(g)) {
            queue.add(g);
          }
        }
      }
    }
    return farthest;
  }

  //returns the width of this boar 
  int findWidth(GamePiece from, GamePiece to) {
    ArrayList<GamePiece> work = new ArrayList<>();
    ArrayList<Integer> depthQueue = new ArrayList<>();
    ArrayList<GamePiece> alreadySeen = new ArrayList<GamePiece>();

    int diameter = -1;
    work.add(from);
    depthQueue.add(0);
    while (!work.isEmpty() && !depthQueue.isEmpty()) {
      GamePiece next = work.remove(0);
      int depth = depthQueue.remove(0);
      if (next.equals(to)) {
        diameter = depth;
      }
      else if (alreadySeen.contains(next)) {
        // do nothing: we've already seen this one
      }
      else {
        for (GamePiece g : next.neighborList()) {
          if (!alreadySeen.contains(g)) {
            work.add(g);
            depthQueue.add(depth + 1);
          }
        }
        alreadySeen.add(next);
      }
    }
    return diameter + 1;
  }

  // EFFECT: makes the board with no connections
  void makeBoardNew() {
    for (int i = 0; i < this.height; i++) { // i = y col
      ArrayList<GamePiece> tmp = new ArrayList<>();
      for (int j = 0; j < this.width; j++) { // j = x row
        tmp.add(new GamePiece(j, i, false, false, false, false));
      }
      board.add(tmp);
    }
  }

  // EFFECT: makes the list of all the nodes
  void getEdges() {
    ArrayList<Edge> tmp = new ArrayList<>();
    ArrayList<GamePiece> gtmp = new ArrayList<>();
    for (ArrayList<GamePiece> ar : board) {
      for (GamePiece g : ar) {
        gtmp.add(g);
        for (Edge e : g.edgeList()) {
          tmp.add(e);
        }
      }
    }
    this.mst = tmp;
    this.nodes = gtmp;
  }

  // EFFECT: Randomly assigns weights
  void randomEdge() {
    for (Edge e : this.mst) {
      e.weight = r.nextInt();
    }
  }

  //Returns a heap sorted list of edges
  ArrayList<Edge> heapsort(ArrayList<Edge> list) {
    makeValidHeap(list, list.size() - 1);
    return headHelp(list);
  }

  // remove head and re-heap
  ArrayList<Edge> headHelp(ArrayList<Edge> list) {
    for (int i = list.size() - 1; i > 0; i--) {
      //swaps the first and last index
      this.swap(list, 0, i);
      //re-makes the heap, but stopping one place sooner
      makeValidHeap(list, i - 1);
    }
    this.mst = list;
    return list;
  }

  // makes a valid heap from a given list index
  void makeValidHeap(ArrayList<Edge> list, int start) {
    for (int i = start; i > 0; i--) {
      int j = i;
      while (j != 0) {
        //checking if the parents weight is less than the childs weight
        if (list.get((j - 1) / 2).weight < list.get(j).weight) {
          this.swap(list, j, (j - 1) / 2);
        }
        //going to the parent and re-checking 
        j = (j - 1) / 2;
      }
    }
  }
  //Upheaps every index to build a heap -- last part of lecture 29
  //this makes more sense to me than the upheaping and downheaping

  //EFFECT: Swaps two items in an array
  void swap(ArrayList<Edge> arr, int index1, int index2) {
    Edge oldValueAtIndex2 = arr.get(index2);
    arr.set(index2, arr.get(index1));
    arr.set(index1, oldValueAtIndex2);
  }

  // returns a list of edges in a MST pattern
  ArrayList<Edge> kruskal() {
    HashMap<GamePiece, GamePiece> representatives = new HashMap<>();
    ArrayList<Edge> edgesInTree = new ArrayList<>();
    ArrayList<Edge> worklist = this.heapsort(this.mst);

    for (GamePiece g : this.nodes) {
      representatives.put(g, g);
    }

    while (worklist.size() > 0) {
      Edge cheapest = worklist.get(0);
      worklist.remove(0);

      GamePiece from = cheapest.fromNode;
      GamePiece to = cheapest.toNode;
      if (find(representatives, from) == find(representatives, to)) {
        // if itll create a loop
      }
      else {
        edgesInTree.add(cheapest);
        union(representatives, find(representatives, from), find(representatives, to));
      }
    }
    return edgesInTree;
  }

  // returns the base node
  GamePiece find(HashMap<GamePiece, GamePiece> map, GamePiece e) {
    if (map.get(e) == e) {
      return e;
    }
    else {
      return find(map, map.get(e));
    }
  }

  // EFFECT: changes the from and to nodes
  void union(HashMap<GamePiece, GamePiece> map, GamePiece from, GamePiece to) {
    GamePiece newFrom = find(map, from);
    GamePiece newTo = find(map, to);
    map.put(newFrom, newTo);
  }

  //Draws a list of edges
  void drawEdgeList(ArrayList<Edge> mstEdges) {
    for (Edge e : mstEdges) {
      e.fromNode.linkEdge(e.toNode);
    }
  }

  // EFFECT: Sets up and runs big bang
  void play() {
    this.board = new ArrayList<>();
    this.makeBoard();
    this.initlink();
    this.getEdges();
    this.randomEdge();
    this.drawEdgeList(this.kruskal());
    this.radius = this.calcEffectiveRadius();
    this.scramble();
    this.bigBang(width * 70, height * 70, 1);
  }

}

class GamePiece {
  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int x; // y
  int y; // x
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean top;
  boolean right;
  boolean bottom;
  boolean left;
  int litAmount;
  GamePiece topNeighbor;
  GamePiece rightNeighbor;
  GamePiece bottomNeighbor;
  GamePiece leftNeighbor;
  // whether the power station is on this piece
  boolean powerStation;

  GamePiece(int row, int col, boolean top, boolean right, boolean bottom, boolean left) {
    this.x = row; // y
    this.y = col; // x
    this.top = top;
    this.right = right;
    this.bottom = bottom;
    this.left = left;
    this.litAmount = 0;
    this.topNeighbor = null;
    this.rightNeighbor = null;
    this.bottomNeighbor = null;
    this.leftNeighbor = null;
  }

  // EFFECT: Rotates this cell
  void rotate() {
    boolean tmp = this.top;
    this.top = this.left;
    this.left = this.bottom;
    this.bottom = this.right;
    this.right = tmp;
    if (!this.isValid()) {
      this.litAmount = 0;
    }
  }

  //EFFECT: links all the edges together
  void linkEdge(GamePiece e) {
    if (e == this.topNeighbor) {
      this.top = true;
      e.bottom = true;
    }
    if (e == this.rightNeighbor) {
      this.right = true;
      e.left = true;
    }
    if (e == this.bottomNeighbor) {
      this.bottom = true;
      e.top = true;
    }
    if (e == this.leftNeighbor) {
      this.left = true;
      e.right = true;
    }
  }

  // Draws this cell
  WorldImage draw() {
    Color line = Color.LIGHT_GRAY;

    if (litAmount == 1) {
      line = Color.YELLOW;
    }
    else if (litAmount == 2) {
      line = Color.YELLOW.darker();
    }
    else if (litAmount == 3) {
      line = Color.ORANGE.darker();
    }
    else if (litAmount == 4) {
      line = Color.ORANGE.darker().darker();
    }

    WorldImage rect = new RectangleImage(40, 10, "Solid", line).movePinhole(-15, 0);
    WorldImage back = new RectangleImage(70, 70, "Solid", Color.black);
    if (this.top) {
      back = new OverlayImage(new RotateImage(rect, -90), back);
    }
    if (this.right) {
      back = new OverlayImage(rect, back);
    }
    if (this.bottom) {
      back = new OverlayImage(new RotateImage(rect, 90), back);
    }
    if (this.left) {
      back = new OverlayImage(new RotateImage(rect, 180), back);
    }
    return new FrameImage(back, Color.WHITE);
  }

  // EFFECT: adds a game piece to this cells neighbor list
  void add(GamePiece g, String s) {
    if (s.equals("top")) {
      this.topNeighbor = g;
    }
    if (s.equals("right")) {
      this.rightNeighbor = g;
    }
    if (s.equals("bottom")) {
      this.bottomNeighbor = g;
    }
    if (s.equals("left")) {
      this.leftNeighbor = g;
    }
  }

  // returns if the given coordinates match this
  boolean match(int x, int y) {
    return x == this.x && y == this.y;
  }

  // checks if this piece is connected in a valid way
  boolean isValid() {
    // checking if the top is valid
    if (this.top) {
      if (this.topNeighbor == null) {
        return false;
      }
      if (!this.topNeighbor.bottom) {
        return false;
      }
    }
    // checking if the right is valid
    if (this.right) {
      if (this.rightNeighbor == null) {
        return false;
      }
      if (!this.rightNeighbor.left) {
        return false;
      }
    }
    // checking the bottom is valid
    if (this.bottom) {
      if (this.bottomNeighbor == null) {
        return false;
      }
      if (!this.bottomNeighbor.top) {
        return false;
      }
    }
    // checking if the left is connected
    if (this.left) {
      if (this.leftNeighbor == null) {
        return false;
      }
      if (!this.leftNeighbor.right) {
        return false;
      }
    }
    return true;
  }

  // returns an ArrayList of all of this pieces neighbors
  ArrayList<GamePiece> neighborList() {
    ArrayList<GamePiece> tmp = new ArrayList<>();

    if (this.topNeighbor != null) {
      if (this.top) {
        tmp.add(this.topNeighbor);
      }
    }
    if (this.rightNeighbor != null) {
      if (this.right) {
        tmp.add(this.rightNeighbor);
      }
    }
    if (this.bottomNeighbor != null) {
      if (this.bottom) {
        tmp.add(this.bottomNeighbor);
      }
    }
    if (this.leftNeighbor != null) {
      if (this.left) {
        tmp.add(this.leftNeighbor);
      }
    }
    return tmp;
  }

  //Returns this cells list of all possible edges
  ArrayList<Edge> edgeList() {
    ArrayList<Edge> tmp = new ArrayList<>();

    if (this.topNeighbor != null) {
      tmp.add(new Edge(this, this.topNeighbor, 0));
    }
    if (this.rightNeighbor != null) {
      tmp.add(new Edge(this, this.rightNeighbor, 0));

    }
    if (this.bottomNeighbor != null) {
      tmp.add(new Edge(this, this.bottomNeighbor, 0));

    }
    if (this.leftNeighbor != null) {
      tmp.add(new Edge(this, this.leftNeighbor, 0));
    }
    return tmp;
  }

  //EFFECT: lights this square and recurs
  void light(GamePiece from, int radius, int radTemp, ArrayList<GamePiece> seen) {
    if (seen.contains(this)) {
      return;
    }
    seen.add(this);

    if (radTemp > 0) {
      if (radius >= radTemp && radTemp > (radius / 4 + radius / 2)) {
        this.litAmount = 1;
      }
      if ((radius / 4 + radius / 2) >= radTemp && radTemp > radius / 2) {
        this.litAmount = 2;
      }
      if (radius / 2 >= radTemp && radTemp > (radius / 4)) {
        this.litAmount = 3;
      }
      if ((radius / 4) >= radTemp && radTemp > 0) {
        this.litAmount = 4;
      }
    }

    else {
      this.litAmount = 0;
    }
    for (GamePiece g : this.neighborList()) {
      if (g.neighborList().contains(this)) {
        if (from != g) {
          g.light(this, radius, radTemp - 1, seen);
        }
      }
    }
  }
}

class Edge {
  GamePiece fromNode;
  GamePiece toNode;
  int weight;

  Edge(GamePiece fromNode, GamePiece toNode, int weight) {
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.weight = weight;
  }
}

class ExamplesLight {

  void testLinkEdge(Tester t) {
    GamePiece c1 = new GamePiece(0, 0, false, false, false, false);
    GamePiece c2 = new GamePiece(0, 1, false, false, false, false);
    GamePiece c3 = new GamePiece(0, 2, false, false, false, false);
    c1.add(c2, "left");
    t.checkExpect(c1.left, false);
    t.checkExpect(c2.right, false);
    c1.linkEdge(c2);
    t.checkExpect(c1.left, true);
    t.checkExpect(c2.right, true);

    c2.add(c3, "bottom");
    t.checkExpect(c2.bottom, false);
    t.checkExpect(c3.top, false);
    c2.linkEdge(c3);
    t.checkExpect(c2.bottom, true);
    t.checkExpect(c3.top, true);
  }

  void testDrawEdgeList(Tester t) {
    LightEmAll l1 = new LightEmAll(2, 2, new Random(1));
    GamePiece c1 = new GamePiece(0, 0, false, false, false, false);
    GamePiece c2 = new GamePiece(0, 0, false, false, false, false);
    GamePiece c3 = new GamePiece(0, 0, false, false, false, false);
    GamePiece c4 = new GamePiece(0, 0, false, false, false, false);
    c1.add(c2, "left");
    Edge e1 = new Edge(c1, c2, 0);
    ArrayList<Edge> el1 = new ArrayList<>();
    el1.add(e1);
    t.checkExpect(c1.left, false);
    l1.drawEdgeList(el1);
    t.checkExpect(c1.left, true);

    c1 = new GamePiece(0, 0, false, false, false, false);
    c2 = new GamePiece(0, 0, false, false, false, false);
    c3 = new GamePiece(0, 0, false, false, false, false);
    c4 = new GamePiece(0, 0, false, false, false, false);

    c1.add(c2, "left");
    c2.add(c3, "left");
    e1 = new Edge(c1, c2, 0);
    Edge e2 = new Edge(c2, c3, 0);
    el1 = new ArrayList<>();
    el1.add(e1);
    el1.add(e2);
    t.checkExpect(c1.left, false);
    t.checkExpect(c2.left, false);
    l1.drawEdgeList(el1);
    t.checkExpect(c1.left, true);
    t.checkExpect(c2.left, true);
  }

  void testKruskal(Tester t) {
    LightEmAll l1 = new LightEmAll(2, 2, new Random(1));
    l1.makeBoard();
    l1.initlink();
    l1.getEdges();
    Edge e1 = new Edge(l1.nodes.get(0), l1.nodes.get(0).bottomNeighbor, 0);
    t.checkExpect(l1.kruskal().get(0), e1);

    l1.makeBoard();
    l1.initlink();
    l1.getEdges();
    Edge e2 = new Edge(l1.nodes.get(1), l1.nodes.get(1).bottomNeighbor, 0);
    t.checkExpect(l1.kruskal().get(1), e2);

    l1.makeBoard();
    l1.initlink();
    l1.getEdges();
    Edge e3 = new Edge(l1.nodes.get(1), l1.nodes.get(1).bottomNeighbor, 0);
    t.checkExpect(l1.kruskal().get(1), e3);
  }

  void testRandom(Tester t) {
    LightEmAll l1 = new LightEmAll(3, 3, new Random(1));
    l1.makeBoard();
    l1.initlink();
    l1.getEdges();
    t.checkExpect(l1.mst.get(0).weight, 0);
    t.checkExpect(l1.mst.get(2).weight, 0);
    t.checkExpect(l1.mst.get(7).weight, 0);
    l1.randomEdge();
    t.checkExpect(l1.mst.get(0).weight, -1155869325);
    t.checkExpect(l1.mst.get(2).weight, 1761283695);
    t.checkExpect(l1.mst.get(7).weight, -1465154083);
  }

  void testFindAndUnion(Tester t) {
    LightEmAll l1 = new LightEmAll(3, 3);
    l1.getEdges();
    GamePiece c1 = new GamePiece(0, 0, true, true, true, true);
    GamePiece c2 = new GamePiece(0, 1, true, true, true, true);
    GamePiece c3 = new GamePiece(0, 2, true, true, true, true);
    GamePiece c4 = new GamePiece(0, 3, true, true, true, true);
    GamePiece c5 = new GamePiece(0, 4, true, true, true, true);

    HashMap<GamePiece, GamePiece> map = new HashMap<>();
    map.put(c1, c1);
    map.put(c2, c2);
    map.put(c3, c2);
    map.put(c4, c3);
    map.put(c5, c5);
    t.checkExpect(l1.find(map, c1), c1);
    t.checkExpect(l1.find(map, c2), c2);
    t.checkExpect(l1.find(map, c4), c2);
    l1.union(map, c3, c1);
    t.checkExpect(map.get(c3), c2);
    l1.union(map, c5, c1);
    t.checkExpect(map.get(c5), c1);
  }

  void testMakeBoard(Tester t) {
    LightEmAll l1 = new LightEmAll(8, 8, new Random(1));
    l1.makeBoardHardCode();
    l1.play();
  }

  void testSwap(Tester t) {
    LightEmAll l1 = new LightEmAll(3, 3);
    l1.makeBoard();
    l1.initlink();
    l1.getEdges();

    Edge e1 = l1.mst.get(0);
    Edge e2 = l1.mst.get(1);

    t.checkExpect(l1.mst.get(0), e1);
    t.checkExpect(l1.mst.get(1), e2);

    l1.swap(l1.mst, 0, 1);

    t.checkExpect(l1.mst.get(0), e2);
    t.checkExpect(l1.mst.get(1), e1);

    Edge e3 = l1.mst.get(5);
    Edge e4 = l1.mst.get(8);

    t.checkExpect(l1.mst.get(5), e3);
    t.checkExpect(l1.mst.get(8), e4);

    l1.swap(l1.mst, 5, 8);

    t.checkExpect(l1.mst.get(5), e4);
    t.checkExpect(l1.mst.get(8), e3);
  }

  void testRadius(Tester t) {
    LightEmAll l1 = new LightEmAll(5, 5, new Random(1));
    l1.makeBoardHardCode();
    l1.initlink();
    t.checkExpect(l1.calcEffectiveRadius(), 5);

    LightEmAll l2 = new LightEmAll(3, 3, new Random(1));
    l2.makeBoardHardCode();
    l2.initlink();
    t.checkExpect(l2.calcEffectiveRadius(), 3);

    LightEmAll l3 = new LightEmAll(10, 7, new Random(1));
    l3.makeBoardHardCode();
    l3.initlink();
    t.checkExpect(l3.calcEffectiveRadius(), 9);

  }

  void testHeap(Tester t) {
    LightEmAll l1 = new LightEmAll(5, 5, new Random(1));
    l1.makeBoard();
    l1.initlink();
    l1.getEdges();
    l1.randomEdge();

    LightEmAll l2 = new LightEmAll(10, 10, new Random(1));
    l2.makeBoard();
    l2.initlink();
    l2.getEdges();
    l2.randomEdge();

    l2.heapsort(l2.mst);

    boolean test = true;
    boolean test2 = true;

    for (int i = 1; i < l2.mst.size(); i++) {
      if (l2.mst.get(i - 1).weight > l2.mst.get(i).weight) {
        test = false;
      }
    }

    for (int i = 1; i < l2.mst.size(); i++) {
      if (l2.mst.get(i - 1).weight < l2.mst.get(i).weight) {
        test2 = false;
      }
    }

    t.checkExpect(test, true);
    t.checkExpect(test2, false);

    t.checkExpect(l1.mst.get(0).weight, -1155869325);
    t.checkExpect(l1.mst.get(1).weight, 431529176);
    t.checkExpect(l1.mst.get(2).weight, 1761283695); // random order
    t.checkExpect(l1.mst.get(5).weight, 155629808);
    t.checkExpect(l1.mst.get(35).weight, -1023599386);
    t.checkExpect(l1.mst.get(38).weight, 600276151);
    t.checkExpect(l1.mst.get(39).weight, -518561627);

    l1.heapsort(l1.mst);

    t.checkExpect(l1.mst.get(0).weight, -2119636700);
    t.checkExpect(l1.mst.get(1).weight, -2048118856);
    t.checkExpect(l1.mst.get(2).weight, -1978864692);
    t.checkExpect(l1.mst.get(3).weight, -1973979577); // increasing in weight
    t.checkExpect(l1.mst.get(35).weight, 5577367);
    t.checkExpect(l1.mst.get(38).weight, 26273138);
    t.checkExpect(l1.mst.get(39).weight, 45889196);
    t.checkExpect(l1.mst.get(40).weight, 100579776);
    t.checkExpect(l1.mst.get(41).weight, 155629808);
    t.checkExpect(l1.mst.get(45).weight, 498074875);
  }

  void testOnKey(Tester t) {
    LightEmAll l1 = new LightEmAll(5, 5, new Random(2));
    l1.makeBoard();
    l1.initlink();
    l1.getEdges();
    l1.randomEdge();
    l1.drawEdgeList(l1.kruskal());
    l1.radius = l1.calcEffectiveRadius();
    l1.scramble();

    t.checkExpect(l1.powerX, 0);
    t.checkExpect(l1.powerY, 0);

    t.checkExpect(l1.board.get(l1.powerY).get(l1.powerX).bottomNeighbor.top
        && l1.board.get(l1.powerY).get(l1.powerX).bottom, true);

    l1.onKeyEvent("down");

    t.checkExpect(l1.powerX, 0);
    t.checkExpect(l1.powerY, 1);

    t.checkExpect(l1.powerX - 1 >= 0, false);

    l1.onKeyEvent("left");

    t.checkExpect(l1.powerX, 0);
    t.checkExpect(l1.powerY, 1);

    t.checkExpect(l1.board.get(l1.powerY).get(l1.powerX).rightNeighbor.left
        && l1.board.get(l1.powerY).get(l1.powerX).right, true);

    l1.onKeyEvent("right");

    t.checkExpect(l1.powerX, 1);
    t.checkExpect(l1.powerY, 1);

    t.checkExpect(l1.board.get(l1.powerY).get(l1.powerX).topNeighbor.bottom
        && l1.board.get(l1.powerY).get(l1.powerX).top, false);

    l1.onKeyEvent("up");

    t.checkExpect(l1.powerX, 1);
    t.checkExpect(l1.powerY, 1);
  }

  void testDraw(Tester t) {
    LightEmAll l3 = new LightEmAll(1, 1, new Random(1));
    LightEmAll l4 = new LightEmAll(2, 1, new Random(1));
    l3.makeBoard();
    l3.initlink();
    l3.getEdges();
    l3.randomEdge();
    l4.makeBoard();
    l4.initlink();
    l4.getEdges();
    l4.randomEdge();
    l4.drawEdgeList(l4.kruskal());
    l4.radius = l4.calcEffectiveRadius();
    l4.scramble();

    WorldImage piece1 = l3.board.get(0).get(0).draw();
    t.checkExpect(l3.draw(), new AboveImage(new EmptyImage(), new BesideImage(new EmptyImage(),
        new OverlayImage(new StarImage(35, 7, OutlineMode.OUTLINE, Color.BLUE), piece1))));
    WorldImage piece3 = l4.board.get(0).get(1).draw();
    t.checkExpect(l4.draw(),
        new AboveImage(new EmptyImage(),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new OverlayImage(new StarImage(35, 7, OutlineMode.OUTLINE, Color.BLUE),
                        new FrameImage(new OverlayImage(
                            new RotateImage(new RectangleImage(40, 10, "Solid", Color.YELLOW)
                                .movePinhole(-15, 0), 90),
                            new RectangleImage(70, 70, "Solid", Color.black)), Color.WHITE))),
                piece3)));

    l4.board.get(0).get(0).right = true;
    l4.board.get(0).get(0).bottom = false;
    l4.board.get(0).get(0).left = false;
    l4.board.get(0).get(0).top = false;
    l4.board.get(0).get(1).bottom = false;
    l4.board.get(0).get(1).left = true;
    l4.board.get(0).get(1).top = false;
    l4.board.get(0).get(1).right = false;

    l4.board.get(0).get(0).litAmount = 1;
    l4.board.get(0).get(1).litAmount = 1;

    t.checkExpect(l4.isValid(), true);

  }

  void testDrawGamePiece(Tester t) {
    LightEmAll l4 = new LightEmAll(2, 1, new Random(1));
    l4.makeBoard();
    l4.initlink();
    l4.getEdges();
    l4.randomEdge();
    l4.drawEdgeList(l4.kruskal());
    l4.radius = l4.calcEffectiveRadius();
    l4.scramble();

    t.checkExpect(l4.board.get(0).get(0).draw(),
        new FrameImage(new OverlayImage(
            new RotateImage(
                new RectangleImage(40, 10, "Solid", Color.LIGHT_GRAY).movePinhole(-15, 0), 90),
            new RectangleImage(70, 70, "Solid", Color.black)), Color.WHITE));
    t.checkExpect(l4.board.get(0).get(1).draw(),
        new FrameImage(new OverlayImage(
            new RotateImage(
                new RectangleImage(40, 10, "Solid", Color.LIGHT_GRAY).movePinhole(-15, 0), 180),
            new RectangleImage(70, 70, "Solid", Color.black)), Color.WHITE));

  }

  void testFarthest(Tester t) {
    LightEmAll l1 = new LightEmAll(3, 3);
    l1.makeBoardHardCode();
    l1.initlink();
    t.checkExpect(l1.farthestPiece(l1.board.get(0).get(0)), l1.board.get(2).get(2));

    LightEmAll l2 = new LightEmAll(5, 5);
    l2.makeBoardHardCode();
    l2.initlink();
    t.checkExpect(l2.farthestPiece(l2.board.get(0).get(0)), l2.board.get(4).get(4));
  }

  void testDepth(Tester t) {
    LightEmAll l1 = new LightEmAll(3, 3);
    l1.makeBoardHardCode();
    l1.initlink();
    t.checkExpect(l1.findWidth(l1.board.get(0).get(0), l1.board.get(1).get(0)), 2);
    t.checkExpect(l1.findWidth(l1.board.get(0).get(0), l1.board.get(0).get(1)), 4);
    t.checkExpect(l1.findWidth(l1.board.get(0).get(0), l1.board.get(2).get(2)), 5);
  }

  void testRotate(Tester t) {
    GamePiece c1 = new GamePiece(0, 0, true, false, false, true);
    c1.rotate();
    t.checkExpect(c1, new GamePiece(0, 0, true, true, false, false));
    c1.rotate();
    t.checkExpect(c1, new GamePiece(0, 0, false, true, true, false));
    c1.rotate();
    t.checkExpect(c1, new GamePiece(0, 0, false, false, true, true));
    c1.rotate();
    t.checkExpect(c1, new GamePiece(0, 0, true, false, false, true));
  }

  void testGamePieceDraw(Tester t) {
    GamePiece c1 = new GamePiece(0, 0, false, false, false, false);
    GamePiece c2 = new GamePiece(0, 0, false, true, false, false);
    t.checkExpect(c1.draw(),
        new FrameImage(new RectangleImage(70, 70, "Solid", Color.black), Color.WHITE));
    t.checkExpect(c2.draw(),
        new FrameImage(new OverlayImage(
            new RectangleImage(40, 10, "Solid", Color.LIGHT_GRAY).movePinhole(-15, 0),
            new RectangleImage(70, 70, "Solid", Color.black)), Color.WHITE));
  }

  void testAdd(Tester t) {
    GamePiece c1 = new GamePiece(0, 0, true, true, true, true);
    GamePiece c2 = new GamePiece(0, 1, true, true, true, true);
    GamePiece c3 = new GamePiece(1, 1, true, true, true, true);
    GamePiece c4 = new GamePiece(4, 3, true, true, true, true);
    GamePiece c5 = new GamePiece(0, 0, true, true, true, true);
    GamePiece c6 = new GamePiece(0, 1, true, true, true, true);

    c2.add(c1, "top");
    t.checkExpect(c2.topNeighbor, c1);
    c2.add(c3, "bottom");
    t.checkExpect(c2.bottomNeighbor, c3);
    c3.add(c4, "left");
    t.checkExpect(c3.leftNeighbor, c4);
    c5.add(c6, "right");
    t.checkExpect(c5.rightNeighbor, c6);
  }

  void testMatch(Tester t) {
    GamePiece c1 = new GamePiece(0, 0, true, true, true, true);
    GamePiece c4 = new GamePiece(4, 3, true, true, true, true);
    GamePiece c6 = new GamePiece(0, 1, true, true, true, true);

    t.checkExpect(c1.match(0, 0), true);
    t.checkExpect(c1.match(1, 1), false);
    t.checkExpect(c4.match(4, 3), true);
    t.checkExpect(c4.match(3, 4), false);
    t.checkExpect(c6.match(0, 1), true);
    t.checkExpect(c6.match(1, 0), false);
  }

  void testIsValidGamePiece(Tester t) {
    GamePiece c1 = new GamePiece(0, 0, false, true, false, false);
    GamePiece c2 = new GamePiece(1, 0, false, false, false, true);
    ArrayList<GamePiece> g1 = new ArrayList<>();
    g1.add(c1);
    g1.add(c2);
    ArrayList<ArrayList<GamePiece>> board = new ArrayList<>();
    board.add(g1);
    LightEmAll l1 = new LightEmAll(2, 1);
    l1.board = board;
    l1.initlink();
    t.checkExpect(l1.board.get(0).get(0).isValid(), true);
    t.checkExpect(l1.board.get(0).get(1).isValid(), true);
    c1.rotate();
    t.checkExpect(l1.board.get(0).get(0).isValid(), false);
    t.checkExpect(l1.board.get(0).get(1).isValid(), false);
  }

  void testScrambleAndTestValid(Tester t) {
    LightEmAll l2 = new LightEmAll(3, 3, new Random(1));
    l2.makeBoardHardCode();
    l2.initlink();
    t.checkExpect(l2.isValid(), false);
    l2.scramble();
    t.checkExpect(l2.isValid(), false);
    l2.board = new ArrayList<>();
    l2.makeBoardHardCode();
    t.checkExpect(l2.board.get(0).get(1).left, false);
    t.checkExpect(l2.board.get(0).get(2).bottom, true);
    t.checkExpect(l2.board.get(0).get(0).bottom, true);
    l2.scramble();
    t.checkExpect(l2.board.get(0).get(2).left, true);
    t.checkExpect(l2.board.get(0).get(2).bottom, false);
  }

  void testInitLink(Tester t) {
    LightEmAll l1 = new LightEmAll(3, 3);
    l1.makeBoardHardCode();
    t.checkExpect(l1.board.get(0).get(0).bottomNeighbor, null);
    t.checkExpect(l1.board.get(1).get(0).topNeighbor, null);
    l1.initlink();
    t.checkExpect(l1.board.get(0).get(0).bottomNeighbor, l1.board.get(1).get(0));
    t.checkExpect(l1.board.get(1).get(0).topNeighbor, l1.board.get(0).get(0));
    t.checkExpect(l1.board.get(0).get(0).rightNeighbor, l1.board.get(0).get(1));
    t.checkExpect(l1.board.get(0).get(1).leftNeighbor, l1.board.get(0).get(0));
  }

  void testMakeBoardHardCode(Tester t) {
    LightEmAll l1 = new LightEmAll(9, 9, new Random(1));
    LightEmAll l2 = new LightEmAll(3, 3, new Random(1));
    l1.makeBoardHardCode();
    l2.makeBoardHardCode();
    t.checkExpect(l2.board.get(1).get(1), new GamePiece(1, 1, true, true, true, true));
    t.checkExpect(l2.board.get(2).get(2), new GamePiece(2, 2, true, false, false, false));
    t.checkExpect(l1.board.get(0).get(0), new GamePiece(0, 0, false, false, true, false));
  }

  void testMakeScene(Tester t) {
    LightEmAll l2 = new LightEmAll(3, 3, new Random(1));
    l2.makeBoardNew();
    // l2.makeBoardF(l2.board);
    l2.initlink();
    WorldScene ws = l2.getEmptyScene();
    ws.placeImageXY(l2.draw(), 3 * 35, 3 * 35);
    t.checkExpect(l2.makeScene(), ws);
  }

  void testOnMouse(Tester t) {
    LightEmAll l2 = new LightEmAll(3, 3, new Random(1));
    LightEmAll l3 = new LightEmAll(3, 3, new Random(1));
    l3.makeBoardHardCode();
    l3.initlink();
    l2.makeBoardHardCode();
    l2.initlink();
    l3.board.get(1).get(1).rotate();
    l2.onMouseClicked(new Posn(70, 70), "RightButton");
    t.checkExpect(l2, l3);

    LightEmAll l4 = new LightEmAll(10, 10, new Random(1));
    LightEmAll l5 = new LightEmAll(10, 10, new Random(1));
    l4.makeBoardHardCode();
    l4.initlink();
    l5.makeBoardHardCode();
    l5.initlink();
    l4.board.get(1).get(1).rotate();
    l5.onMouseClicked(new Posn(70, 70), "RightButton");
    t.checkExpect(l5, l4);
  }

}