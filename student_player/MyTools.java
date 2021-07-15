package student_player;

import java.util.ArrayList;

import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

public class MyTools {
	public static double getSomething() {
		return Math.random();
	}
	//inner class that assigns a value to a move
	public static class TrackMove {
		public PentagoMove aMove;
		public Integer aValue;

		public TrackMove(PentagoMove pMove, Integer pValue) {
			assert pMove != null && pValue != null;
			aMove = pMove;
			aValue = pValue;
		}
	}
	// strategy to place a piece in the center of one of the four quadrants
	public static PentagoMove gridCenter(PentagoBoardState bS, int player) {
		if (bS.getPieceAt(1, 1) == PentagoBoardState.Piece.EMPTY) {
			return new PentagoMove(1, 1, PentagoBoardState.Quadrant.TL, PentagoBoardState.Quadrant.BR, player);
		} else if (bS.getPieceAt(1, 4) == PentagoBoardState.Piece.EMPTY) {
			return new PentagoMove(1, 4, PentagoBoardState.Quadrant.TR, PentagoBoardState.Quadrant.BL, player);
		} else if (bS.getPieceAt(4, 1) == PentagoBoardState.Piece.EMPTY) {
			return new PentagoMove(4, 1, PentagoBoardState.Quadrant.TR, PentagoBoardState.Quadrant.BL, player);
		} else {
			return new PentagoMove(4, 4, PentagoBoardState.Quadrant.TL, PentagoBoardState.Quadrant.BR, player);
		}
	}
}
