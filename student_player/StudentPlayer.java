package student_player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.math.*;

import boardgame.Move;

import pentago_swap.PentagoPlayer;
import student_player.MyTools.TrackMove;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoBoardState.Quadrant;
import pentago_swap.PentagoMove;


public class StudentPlayer extends PentagoPlayer {

	public StudentPlayer() {
		super("xxx");
	}



	public Move chooseMove(PentagoBoardState boardState) {

		int turn = boardState.getTurnNumber();
		int player = boardState.getTurnPlayer();
		int noob = boardState.getOpponent();

		// First move strategy: put the piece in the middle of one of the quadrants
//		if (turn == 0) {
//			return MyTools.gridCenter(boardState, player);
//		}
		// Generates all available moves
		ArrayList<PentagoMove> randMoves = boardState.getAllLegalMoves();
		// It is important to shuffle the available moves since they are not randomly
		// placed in the array, and thus might cause some bias to the top quadrant
		Collections.shuffle(randMoves);
		// To save time, can check if there is a possible win, if there is, it returns
		// it.
		PentagoMove possibleWin = guaranteedWin(boardState, player, randMoves);
		if (possibleWin != null) {
			return possibleWin;
		}
		// Removes all moves that will lead to the victory of the opponent, and so, it
		// holds the remaining moves.
		ArrayList<PentagoMove> moves = noNoobWin(boardState, noob, randMoves);
		Collections.shuffle(moves);
		// If there are no moves other than moves that lead to a win for the opponent,
		// use all of the available moves
		if (moves == null || moves.size() == 0) {
			moves = randMoves;
		}
		Move possibleDraw = draw(boardState, player, randMoves);
		// If there is a possibility to draw, do so, better to draw then to lose.
		if (possibleDraw != null) {
			return possibleDraw;
		}
		TrackMove x;
		// Before this turns, computing minimax up to depth three exceeds the time limit
		if (turn < 11) {
			x = minimaxPolicy(boardState, player, noob, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, moves);
		}
		// Once there are less moves to consider, the minimax depth can be increased
		// while avoiding timeouts
		else {
			x = minimaxPolicy(boardState, player, noob, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, moves);
		}
		System.out.println(x.aValue);
		return x.aMove;
	}

	// Determines if the player can make a move that results in a guaranteed win
	public PentagoMove guaranteedWin(PentagoBoardState bS, int player, ArrayList<PentagoMove> moves) {
		for (PentagoMove m : moves) {
			PentagoBoardState clonedBS = (PentagoBoardState) bS.clone();
			clonedBS.processMove(m);
			if (clonedBS.getWinner() == player) {
				return m;
			}
		}
		return null;
	}

	// Determines if the player can make a move that results in a draw
	public PentagoMove draw(PentagoBoardState bS, int player, ArrayList<PentagoMove> moves) {
		for (PentagoMove m : moves) {
			PentagoBoardState clonedBS = (PentagoBoardState) bS.clone();
			clonedBS.processMove(m);
			if (clonedBS.getWinner() == Integer.MAX_VALUE) {
				return m;
			}
		}
		return null;
	}

	// Return the moves that do not lead to a win for player
	public ArrayList<PentagoMove> noNoobWin(PentagoBoardState bS, int player, ArrayList<PentagoMove> moves) {
		ArrayList<PentagoMove> avoid = new ArrayList<PentagoMove>();
		for (int i = 0; i < moves.size(); i++) {
			PentagoBoardState clonedBS = (PentagoBoardState) bS.clone();
			clonedBS.processMove(moves.get(i));
			if (!clonedBS.gameOver()) {
				ArrayList<PentagoMove> noobMoves = clonedBS.getAllLegalMoves();
				// maybe remove the draw
				if (guaranteedWin(clonedBS, player, noobMoves) == null) {
					avoid.add(moves.get(i));
				}
			}
		}
		return avoid;
	}

	// Sums two array lists
	public ArrayList<Integer> listSum(ArrayList<Integer> a, ArrayList<Integer> b) {
		ArrayList<Integer> sum = new ArrayList<Integer>(a.size());

		for (int i = 0; i < a.size(); i++) {
			sum.add(a.get(i) + b.get(i));
		}
		return sum;
	}

	// Returns a move depending on the minimax policy
	public TrackMove minimaxPolicy(PentagoBoardState bS, int player, int noob, int a, int b, int depth,
			ArrayList<PentagoMove> moves) {
		// Run while you can still make moves
		while (!bS.gameOver()) {
			if (moves.size() == 0) {
				return new TrackMove((PentagoMove) bS.getRandomMove(), 0);
			}
			// When the target depth is reached or when there is only one move left,
			// calculate the value of the move
			if (moves.size() == 1 || depth >= 3) {
				int eval = eval(bS, player, noob);
				return new TrackMove(moves.get(0), eval);
			}
			int turn = bS.getTurnPlayer();

			PentagoMove rand = moves.get(0);
			// Depending on the player of the turn, creates a new move
			TrackMove best = turn == player ? new TrackMove(rand, Integer.MIN_VALUE)
					: new TrackMove(rand, Integer.MAX_VALUE);

			for (PentagoMove m : moves) {
				PentagoBoardState clonedBS = (PentagoBoardState) bS.clone();
				clonedBS.processMove(m);
				ArrayList<PentagoMove> randMoves = clonedBS.getAllLegalMoves();
				Collections.shuffle(randMoves);
				int recurVal;
				// If the player can win in a turn, return the move with a corresponding value
				if (turn == player) {
					if (clonedBS.getWinner() == player && turn == player) {
						return new TrackMove(m, 11010);
//				} else if (clonedBS.getWinner() == Integer.MAX_VALUE
//						&& guaranteedWin(clonedBS, noob, clonedBS.getAllLegalMoves()) != null) {
//					return new TrackMove(m, 6000);
//				} else if (clonedBS.getWinner() == Integer.MAX_VALUE) {
//					return new TrackMove(m, 0);
					}
					// If the opponent can win in a turn, return the move with a corresponding value
				} else if (turn == noob) {
					if (clonedBS.getWinner() == noob && turn == noob) {
						return new TrackMove(m, -11000);
					}
//				if (clonedBS.getWinner() == player && draw(clonedBS, noob, clonedBS.getAllLegalMoves()) != null) {
//					return new TrackMove(m, -8000);
//				}
				}
				// Recursion to go deeper
				TrackMove recur = minimaxPolicy(clonedBS, player, noob, a, b, depth + 1, randMoves);
				// To compare the best move so far, and the one obtained from the recursion
				recurVal = recur.aValue;
				int bestVal = best.aValue;
				Boolean isItMe = (turn == player);

				// Depeneding on the alpha beta pruning policy, replace the best move so far, or
				// don't
				if (isItMe) {
					a = abprunePolicy(a, recurVal, isItMe);
					if (recurVal > bestVal) {
						best = new TrackMove(m, recurVal);
						bestVal = best.aValue;
					}
				} else {
					b = abprunePolicy(b, recurVal, isItMe);
					if (recurVal < bestVal) {
						best = new TrackMove(m, recurVal);
						bestVal = best.aValue;
					}
				}
				if (a >= b) {
					break;
				}
			}

			return best;
		}
		// if the game is over, just return a move with a zero weight to avoid bias
		return new TrackMove(null, 0);
	}

	// Policy to prune alphas and betas
	public int abprunePolicy(int x, int curr, boolean val) {
		if (val) {
			if (x < curr) {
				return curr;
			}
		} else {
			if (x > curr) {
				return curr;
			}
		}
		return x;
	}

	// Evaluation function
	public int eval(PentagoBoardState bS, int player, int noob) {
		int eval = 0;
		// Determines the colours of both players
		// simlar to provided code
		Piece colr = player == 0 ? Piece.WHITE : Piece.BLACK;
		Piece noobColr = colr == Piece.WHITE ? Piece.BLACK : Piece.WHITE;
		// Keeps track of pairs of three's on rows / cols / diags
		int pairs3 = 0;
		int noobPairs3 = 0;
		// Keeps track of how many pieces on a row / col
		ArrayList<Integer> colVals = new ArrayList<Integer>(3);
		ArrayList<Integer> rowVals = new ArrayList<Integer>(3);
		// Used to add more bias to the middle
		ArrayList<Integer> midBias = new ArrayList<Integer>(6);
		for (int i = 0; i < 3; i++) {
			colVals.add(0);
			rowVals.add(0);

		}
		for (int i = 0; i < 6; i++) {
			midBias.add(0);
		}
//		  quadToInt = new HashMap<>(4);
//        quadToInt.put(Quadrant.TL, 0);
//        quadToInt.put(Quadrant.TR, 1);
//        quadToInt.put(Quadrant.BL, 2);
//        quadToInt.put(Quadrant.BR, 3);
		// keeps track of the total number of pieces in all diagonals
		int totalDiag1 = 0;
		int totalDiag2 = 0;
		// Loops over the four quadrants
		for (int i = 0; i < 4; i++) {
			// Keeps track of moves in a quadrant
			int pairs = 0;
			int noobPairs = 0;
			int diag1 = 0;
			int diag2 = 0;
			int weirdDiag = 0;
			int weirdDiagNoob = 0;
			for (int j = 0; j < 3; j++) {
				int row = 0;
				int col = 0;
				// Positions for diagonals in each quadrant
				int xPosD1;
				int xPosD2;
				xPosD1 = xPosD2 = (i % 2 == 0 ? 0 : 3) + j;
				int yPosD1 = ((i == 2 || i == 3) == true ? 3 : 0) + j;
				int yPosD2 = ((i == 2 || i == 3) == true ? 3 : 0) - j + 2;
				for (int l = 0; l < 3; l++) {
					int xPosR;
					int xPosC;
					int yPosR;
					int yPosC;
					// Positions of pieces in each quadrant / row / column
					if (i == 0) {
						xPosR = l;
						yPosR = j;
						xPosC = j;
						yPosC = l;
					}
					if (i == 1) {
						xPosR = l + 3;
						yPosR = j;
						xPosC = j;
						yPosR = l + 3;
					}
					if (i == 2) {
						xPosR = l;
						yPosR = j + 3;
						xPosC = j + 3;
						yPosC = l;
					} else {
						xPosR = l + 3;
						yPosR = j + 3;
						xPosC = j + 3;
						yPosC = l + 3;
					}
					Piece rowP = bS.getPieceAt(xPosR, yPosR);
					Piece colP = bS.getPieceAt(xPosC, yPosC);
					// count how many of the player's pieces versus how manny opponent's pieces per
					// row /col
					if (rowP == colr) {
						row++;
					} else if (rowP == noobColr) {
						row--;
					}
					if (colP == colr) {
						col++;
					} else if (colP == noobColr) {
						col--;
					}

				}
				// Weird diagonals represents pieces in positions such as (0,1) and (1,2), they
				// are not part of the actual diagonal per say, but together they form a two
				// piece diagonal
				if (bS.getPieceAt((i % 2 == 0 ? 0 : 3) + 1, (i == 2 || i == 3) == true ? 3 : 0) == colr
						|| bS.getPieceAt((i % 2 == 0 ? 0 : 3) + 1, (i == 2 || i == 3) == true ? 5 : 2) == colr) {
					if (bS.getPieceAt((i % 2 == 0 ? 0 : 3), (i == 2 || i == 3) == true ? 4 : 1) == noobColr && bS
							.getPieceAt((i % 2 == 0 ? 0 : 3) + 2, (i == 2 || i == 3) == true ? 4 : 1) == noobColr) {
						weirdDiag++;
					}
					if (bS.getPieceAt((i % 2 == 0 ? 0 : 3), (i == 2 || i == 3) == true ? 4 : 1) == colr
							|| bS.getPieceAt((i % 2 == 0 ? 0 : 3) + 2, (i == 2 || i == 3) == true ? 4 : 1) == colr) {
						weirdDiag++;
					}
				}
				if (bS.getPieceAt((i % 2 == 0 ? 0 : 3) + 1, (i == 2 || i == 3) == true ? 3 : 0) == noobColr
						|| bS.getPieceAt((i % 2 == 0 ? 0 : 3) + 1, (i == 2 || i == 3) == true ? 5 : 2) == noobColr) {
					if (bS.getPieceAt((i % 2 == 0 ? 0 : 3), (i == 2 || i == 3) == true ? 4 : 1) == noobColr && bS
							.getPieceAt((i % 2 == 0 ? 0 : 3) + 2, (i == 2 || i == 3) == true ? 4 : 1) == noobColr) {
						weirdDiagNoob++;
					}

					if (bS.getPieceAt((i % 2 == 0 ? 0 : 3), (i == 2 || i == 3) == true ? 4 : 1) == noobColr || bS
							.getPieceAt((i % 2 == 0 ? 0 : 3) + 2, (i == 2 || i == 3) == true ? 4 : 1) == noobColr) {
						weirdDiagNoob++;
					}
				}
				//count pieces in diagonals
				if (bS.getPieceAt(xPosD1, yPosD1) == colr) {
					diag1++;
					eval -= 10;
				}
				if (bS.getPieceAt(xPosD1, yPosD1) == noobColr) {
					diag1--;
					if (j % 2 == 0) {
						eval += 10;
					}
				}
				if (bS.getPieceAt(xPosD2, yPosD2) == colr) {
					diag2++;
					eval -= 1.5;
				}
				if (bS.getPieceAt(xPosD2, yPosD2) == noobColr) {
					diag2--;
					if (j % 2 == 0) {
						eval += 1.5;
					}
				}
				// Early Game
				if (bS.getTurnNumber() < 6) {
					//Adding/subtracting from the evaluation depending on the number of pieces
					if (row < 1) {
						eval += -11.5;
					}
					if (col < 1) {
						eval += -11.5;
					}
					if (row == 2) {
						eval += 11;
						pairs++;
					}
					if (col == 2) {
						eval += 11;
						pairs++;
					}
					if (row == -2) {
						eval += -11;
						noobPairs++;
					}
					if (col == -2) {
						eval += -11;
						noobPairs++;
					}
					if (diag1 == 2) {
						eval += 8;
						pairs++;
					}
					if (diag2 == 2) {
						eval += 8;
						pairs++;
					}
					if (diag1 == -2) {
						eval += -11;
						noobPairs++;
					}
					if (diag2 == -2) {
						eval += -11;
						noobPairs++;
					}
					if (pairs - noobPairs > 2) {
						eval += 100;
					}
					if (-pairs + noobPairs > 2) {
						eval += -350;
					}

				}

				// Mid bias?
				if (row > 0) {
					int x = rowVals.get(j);
					if (bS.getPieceAt(xPosD1, 1 + (((i == 2 || i == 3) == true ? 3 : 0))) == colr) {
						midBias.set(j, midBias.get(j) + 1);
					}
					if (row == 3) {
						midBias.set(j, midBias.get(j) + 11);
					}
				}
				if (col > 0) {
					int x = colVals.get(j);
					if (bS.getPieceAt(1 + (i % 2 == 0 ? 0 : 3), yPosD1) == colr) {
						midBias.set(j + 3, midBias.get(j + 3) + 1);
					}
					if (col == 3) {
						midBias.set(j + 3, midBias.get(j + 3) + 11);
					}
				}
				int y = rowVals.get(j);
				int x = colVals.get(j);
				rowVals.set(j, (int) (y + row * 1.1));
				colVals.set(j, (int) (x + col * 1.1));
				// My AI performs worse against diagonals, so their values is even more lessened
				// than others
				if (diag1 == 3) {
					eval += 350;
					pairs3++;
				}
				if (diag2 == 3) {
					eval += 350;
					pairs3++;
				}
				if (diag1 == -3) {
					eval -= 2130;
					noobPairs3++;
				}
				if (diag1 == -2) {
					eval -= 30;
				}
				if (diag2 == -2) {
					eval -= 30;
				}
				if (diag2 == -3) {
					eval -= 2130;
					noobPairs3++;
				}
				if (row == 3) {
					eval += 350;
					pairs3++;
				}
				if (row == -3) {
					eval -= 2100;
					noobPairs3++;
				}
				if (col == 3) {
					eval += 350;
					pairs++;
				}
				if (col == -3) {
					eval -= 2100;
					noobPairs3++;
				}
				if (weirdDiagNoob <= -2) {
					eval -= 125;
				}
				if (weirdDiag <= 2) {
					eval += 25;
				}
				totalDiag1 += diag1;
				totalDiag2 += diag2;
			}
		}
		// Value of triples
//		if (pairs3 - noobPairs3 > 2) {
//			eval += 50;
//		}
//		if (noobPairs3 - pairs3 == 1) {
//			eval += -30;
//		} else if (noobPairs3 - pairs3 > 2) {
//			eval += -150;
//		}
		// Bias against diagonals
		if (totalDiag1 == -4) {
			eval -= 40;
		}
		if (totalDiag1 == -5) {
			eval -= 70;
		}
		if (totalDiag2 == -4) {
			eval -= 40;
		}
		if (totalDiag2 == -5) {
			eval -= 70;
		}
		for (Integer i : midBias) {
			if (i > 14) {
				eval += 1050;
			}
			if (i == 14) {
				eval += 250;
			}
		}
		for (Integer i : colVals) {
			if (i == -4) {
				eval -= 20;
			}
			if (i == -5) {
				eval -= 120;
			}
			eval += 0.3 * (Math.pow(i, 3) - 2.2 * Math.pow(i, 2));
		}
		for (Integer i : rowVals) {
			if (i == -4) {
				eval -= 20;
			}
			if (i == -5) {
				eval -= 120;
			}
			eval += 0.3 * (Math.pow(i, 3) - 2.2 * Math.pow(i, 2));
		}

		return eval;
	}

}