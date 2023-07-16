package chess.zoho.com;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;



public class ChessGame {
    private static String[][] chessboard;
    private static StringBuilder moveHistory;
    private static String enPassantTarget; 
    
    
    public static void main(String[] args) {
        chessboard = new String[][]{
                {"b_R", "b_N", "b_B", "b_Q", "b_K", "b_B", "b_N", "b_R"},
                {"b_P", "b_P", "b_P", "b_P", "b_P", "b_P", "b_P", "b_P"},
                {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
                {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
                {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
                {"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "},
                {"w_P", "w_P", "w_P", "w_P", "w_P", "w_P", "w_P", "w_P"},
                {"w_R", "w_N", "w_B", "w_Q", "w_K", "w_B", "w_N", "w_R"}
        };

        moveHistory = new StringBuilder();

        playChessGame();
    }

    private static void playChessGame() {
        Scanner scanner = new Scanner(System.in);
        boolean isPlayer1Turn = true;

        while (true) {
        	//isCheckmate(isPlayer1Turn);
            printChessboard();
            
            String currentPlayer = isPlayer1Turn ? "Player 1" : "Player 2";
            System.out.println(currentPlayer + ", Enter the position of the piece you want to move (Example : e2 denotes King's pawn in the white side):");
            String input = scanner.nextLine();
            boolean helpEnabled = false;

            if (input.equals("exit")) {
                System.out.println("Exiting the game.");
                break;
            }

            if (input.equals("Print")) {
                printChessboard();
                continue;
            }

            if (input.contains("--help")) {
                handleHelpRequest(input, helpEnabled);
                continue;
            }

            if (!isValidPosition(input)) {
                System.out.println("Invalid position. Please try again.");
                continue;
            }

            int[] currentPos = convertPositionToIndexes(input);

            if (!isOwnPiece(currentPos, isPlayer1Turn)) {
                System.out.println("Invalid piece selection. Please choose your own piece.");
                continue;
            }

            String piece = chessboard[currentPos[0]][currentPos[1]];
            String possibleMoves = getPossibleMoves(piece, currentPos, isPlayer1Turn,helpEnabled);
            if(possibleMoves.isEmpty()) {
            	System.out.println("Invalid piece selection. The piece cannot be moved.");
            	continue;
            }
            
            System.out.println("Current piece: " + piece);
            System.out.println("Possible moves: " + possibleMoves);

            System.out.println(currentPlayer + " Enter the position you want to move the piece to:");
            String newPosition = scanner.nextLine();

            if (!isValidPosition(newPosition)) {
                System.out.println("Invalid position. Please try again.");
                continue;
            }

            int[] newPos = convertPositionToIndexes(newPosition);

            if ((!isMoveValid(piece, currentPos, newPos, isPlayer1Turn) )) {
                System.out.println("Invalid move. Please try again.");
                continue;
            }

            String capturedPiece = movePiece(currentPos, newPos,chessboard);
            String move = currentPlayer + ": " + piece + " at " + input + " moved to " + newPosition;
           
            if (!capturedPiece.isEmpty() && !capturedPiece.equals("   ")) {
                move += " (captured " + capturedPiece + ")";
                System.out.println(move);
            }
            moveHistory.append(move).append("\n");
            
            
            isPlayer1Turn = !isPlayer1Turn;
            if(isCheckMate(isPlayer1Turn)) {
            	moveHistory.append("Check Mate");
            	System.out.println("GameOver Winner: "+(isPlayer1Turn ? "P2" : "P1"));
            	saveMoveHistoryToFile();
            	break;
            }
        }
    }
    
    private static boolean isCheckMate(boolean isPlayer1Turn) {
        char color = isPlayer1Turn ? 'w' :'b';
        
        for(int i = 0 ; i<8 ; i++) {
            for(int j = 0 ; j < 8; j++) {
                if(chessboard[i][j] != null && !chessboard[i][j].isEmpty()
                        && chessboard[i][j].charAt(0)==color) {
                    int index[] = {i, j};
                    String moves = getPossibleMoves(chessboard[i][j], index, isPlayer1Turn, false);
                    String possibleMoves[] = moves.trim().split(" ");
                    
                    for(String possibleMove : possibleMoves) {
                    	if(possibleMove == null || possibleMove.isEmpty()) {
                            continue;
                            }
                        int[] possibleMoveIndex = convertPositionToIndexes(possibleMove);
                        
                        String[][] clonedChessBoard = cloneChessBoard();
                        movePiece(index, possibleMoveIndex, chessboard);
                        
                        int [] kingPosition = findKingPosition(color);
                        String kingSquare = getSquareName(kingPosition[0], kingPosition[1]);
                        
                        if(!isKingInCheck(chessboard, color+"", isPlayer1Turn, kingSquare)) {
                        	 reupdateOriginalChessBoard(clonedChessBoard);
                        	return false;
                        }
                        
                        reupdateOriginalChessBoard(clonedChessBoard);
                    }
                    
                }
            }
        }
        return true;
    }    
    private static boolean isValidPosition(String position) {
        return position.matches("[a-h][1-8]");
    }

    private static int[] convertPositionToIndexes(String position) {
    	
    	int column = position.charAt(0)-97;
		int  row = 8 - Integer.parseInt(position.charAt(1)+"");
		int index[] = {row , column};
		return index;
    }
    private static String convertIndexToPosition(int index[]) {
    	int column = index[1];
		int  row = index[0];
		String alphabet = (char)(row+97)+"";
		String number = (char)(column+1)+"";
		
		return alphabet+number;
    }

    private static boolean isOwnPiece(int[] position, boolean isPlayer1Turn) {
       if(isPlayer1Turn) {
    	   //Comeback
    	   if((chessboard[position[0]][position[1]]).startsWith("w")){
    		   return true;
    	   }
    	   else {return false;}
       }
       else {
    	   if((chessboard[position[0]][position[1]]).startsWith("b")){
    		   return true;
    	   }
    	   else {return false;}
    	   
       }
	
    }

    private static String getPossibleMoves(String piece, int[] currentPosition, boolean isPlayer1Turn, boolean helpEnabled) {
        int row = currentPosition[0];
        int col = currentPosition[1];
        char color = isPlayer1Turn ? 'w' : 'b';

        switch (piece.charAt(2)) {
            case 'P':
                return getPawnMoves(row, col, color , helpEnabled);
            case 'R':
                return getRookMoves(row, col, color , helpEnabled);
            case 'N':
                return getKnightMoves(row, col, color , helpEnabled);
            case 'B':
                return getBishopMoves(row, col, color , helpEnabled);
            case 'Q':
                return getQueenMoves(row, col, color , helpEnabled);
            case 'K':
                return getKingMoves(row, col, color , helpEnabled);
            default:
                return "";
        }
    }

    private static String getPawnMoves(int row, int col, char color, boolean helpEnabled) {
        ArrayList<String> moves = new ArrayList<>();
        int direction = (color == 'w') ? -1 : 1;
        String allMoves = new String();
        // Move one square forward
        if (isValidSquare(row + direction, col)) {
            moves.add(getSquareName(row + direction, col));

            // Move two squares forward (only on the initial move)
            if ((row == 6 && color == 'w') || (row == 1 && color == 'b')) {
//            	boolean occ =  !isOccupied(row + direction, col);
//            	boolean occ2 =  !isOccupied(row + (2*direction), col);
//            	System.out.println(occ+ " "+occ2);
                if (isValidSquare(row + (2 * direction), col) && !isOccupied(row + direction, col) && !isOccupied(row + (2 * direction), col)) {
                    moves.add(getSquareName(row + (2 * direction), col));
                }
            }
        }

        // Capture diagonally to the left
        if(helpEnabled) {
        	if (isValidSquare(row + direction, col - 1)) {
                moves.add(getSquareName(row + direction, col - 1));
            }
        }
        else{
        	if(isValidSquare(row + direction, col - 1) && isOccupiedByOpponent(row + direction, col - 1, color)) {
        		moves.add(getSquareName(row + direction, col - 1));
        	}
        }
        // Capture diagonally to the right
        if(helpEnabled) {
        	if (isValidSquare(row + direction, col + 1)) {
                moves.add(getSquareName(row + direction, col + 1));
            }
        }
        else{
        	if(isValidSquare(row + direction, col + 1) && isOccupiedByOpponent(row + direction, col + 1, color)) {
        		moves.add(getSquareName(row + direction, col + 1));
        	}
        }

        // En passant capture
        if (isValidSquare(row, col - 1) && isEnPassantValid(row, col - 1, color)) {
            moves.add(getSquareName(row + direction, col - 1));
        }
        if (isValidSquare(row, col + 1) && isEnPassantValid(row, col + 1, color)) {
            moves.add(getSquareName(row + direction, col + 1));
        }
        for(int i = 0 ; i<moves.size(); i++) 
        {
        	allMoves+=moves.get(i)+" ";
        }
        return allMoves;
    }

    private static String getRookMoves(int row, int col, char color, boolean helpEnabled) {
        
        ArrayList<String> moves = new ArrayList<>();
        String allMoves = new String();
        // Move vertically up
        for (int i = row - 1; i >= 0; i--) {
            if (isValidSquare(i, col)) {
                if (isOccupiedByOpponent(i, col, color)) {
                    moves.add(getSquareName(i, col));
                    break;
                } else if (!isOccupied(i, col)) {
                    moves.add(getSquareName(i, col));
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        // Move vertically down
        for (int i = row + 1; i < 8; i++) {
            if (isValidSquare(i, col)) {
                if (isOccupiedByOpponent(i, col, color)) {
                    moves.add(getSquareName(i, col));
                    break;
                } else if (!isOccupied(i, col)) {
                    moves.add(getSquareName(i, col));
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        // Move horizontally to the left
        for (int j = col - 1; j >= 0; j--) {
            if (isValidSquare(row, j)) {
                if (isOccupiedByOpponent(row, j, color)) {
                    moves.add(getSquareName(row, j));
                    break;
                } else if (!isOccupied(row, j)) {
                    moves.add(getSquareName(row, j));
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        // Move horizontally to the right
        for (int j = col + 1; j < 8; j++) {
            if (isValidSquare(row, j)) {
                if (isOccupiedByOpponent(row, j, color)) {
                    moves.add(getSquareName(row, j));
                    break;
                } else if (!isOccupied(row, j)) {
                    moves.add(getSquareName(row, j));
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        
		for(int i = 0 ; i<moves.size(); i++) 
        {
        	allMoves+=moves.get(i)+" ";
        }
        return allMoves;
    }
    

    private static String getKnightMoves(int row, int col, char color, boolean helpEnabled) {
        int[][] knightMoves = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };
        String allMoves = new String();
        ArrayList<String> moves = new ArrayList<>();

        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];

            if (isValidSquare(newRow, newCol)) {
                if (isOccupiedByOpponent(newRow, newCol, color)) {
                    moves.add(getSquareName(newRow, newCol));
                } else if (!isOccupied(newRow, newCol)) {
                    moves.add(getSquareName(newRow, newCol));
                }
            }
        }

       
		
		for(int i = 0 ; i<moves.size(); i++) 
        {
        	allMoves+=moves.get(i)+" ";
        }
        return allMoves;
    }
    

    private static String getBishopMoves(int row, int col, char color, boolean helpEnabled) {
        ArrayList<String> moves = new ArrayList<>();
        String allMoves = new String();
        // Move diagonally up-left
        int i = row - 1;
        int j = col - 1;
        while (isValidSquare(i, j)) {
            if (isOccupiedByOpponent(i, j, color)) {
                moves.add(getSquareName(i, j));
                break;
            } else if (!isOccupied(i, j)) {
                moves.add(getSquareName(i, j));
            } else {
                break;
            }
            i--;
            j--;
        }

        // Move diagonally up-right
        i = row - 1;
        j = col + 1;
        while (isValidSquare(i, j)) {
            if (isOccupiedByOpponent(i, j, color)) {
                moves.add(getSquareName(i, j));
                break;
            } else if (!isOccupied(i, j)) {
                moves.add(getSquareName(i, j));
            } else {
                break;
            }
            i--;
            j++;
        }

        // Move diagonally down-left
        i = row + 1;
        j = col - 1;
        while (isValidSquare(i, j)) {
            if (isOccupiedByOpponent(i, j, color)) {
                moves.add(getSquareName(i, j));
                break;
            } else if (!isOccupied(i, j)) {
                moves.add(getSquareName(i, j));
            } else {
                break;
            }
            i++;
            j--;
        }

        // Move diagonally down-right
        i = row + 1;
        j = col + 1;
        while (isValidSquare(i, j)) {
            if (isOccupiedByOpponent(i, j, color)) {
                moves.add(getSquareName(i, j));
                break;
            } else if (!isOccupied(i, j)) {
                moves.add(getSquareName(i, j));
            } else {
                break;
            }
            i++;
            j++;
        }

        for(int k = 0 ; k<moves.size() ; k++) {
       	 allMoves += moves.get(k)+" ";
       }
		return allMoves;
    }

    private static String getQueenMoves(int row, int col, char color, boolean helpEnabled) {
        ArrayList<String> moves = new ArrayList<>();
        String allMoves = new String();
        // Get the bishop moves
         ArrayList<String> crossMoves =new ArrayList<>();
         ArrayList<String> straightMoves =new ArrayList<>();
        // Get the rook moves
         String rookMoves[] = getRookMoves(row, col, color,helpEnabled).split(" "); 
         String bishopMoves[] = getBishopMoves(row, col, color, helpEnabled).split(" "); 
        moves.add(getRookMoves(row, col, color, helpEnabled));
        for(int i = 0 ; i< bishopMoves.length ; i++) {
        	crossMoves.add(bishopMoves[i]);
        }
        for(int j = 0 ; j<rookMoves.length;j++) {
        	straightMoves.add(rookMoves[j]);
        }
       // System.out.println(crossMoves.get(0)=="");
        
        	moves.addAll(crossMoves);
        	moves.addAll(straightMoves);
        
        if (!bishopMoves[0].equals("") || !rookMoves[0].equals("")) 
        {
        	for (int i = 0; i < moves.size(); i++) {
        	            allMoves += moves.get(i) + " ";
        	        }
        } 
        else {
        	  allMoves = "";    
        }
        
		return allMoves;
    }

    private static String getKingMoves(int row, int col, char color, boolean helpEnabled) {
        int[][] kingMoves = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };

        StringBuilder moves = new StringBuilder();

        for (int[] move : kingMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];

            if (isValidSquare(newRow, newCol)) {
                if (isOccupiedByOpponent(newRow, newCol, color)) {
                    moves.append(getSquareName(newRow, newCol));
                } else if (!isOccupied(newRow, newCol)) {
                    moves.append(getSquareName(newRow, newCol));
                }
            }
        }

        // Castling
        if (canCastleKingSide(row, col, color)) {
            moves.append(getSquareName(row, col + 2));
        }
        if (canCastleQueenSide(row, col, color)) {
            moves.append(getSquareName(row, col - 2));
        }

        return moves.toString();
    }

    private static boolean canCastleKingSide(int row, int col, char color) {
        if ((color == 'w' && row != 7) || (color == 'b' && row != 0)) {
            return false;
        }

        if (isCheck(color)) {
            return false;
        }

        if (isOccupied(row, col + 1) || isOccupied(row, col + 2)) {
            return false;
        }

        if (isUnderAttack(row, col, getOpponentColor(color)) || isUnderAttack(row, col + 1, getOpponentColor(color)) || isUnderAttack(row, col + 2, getOpponentColor(color))) {
            return false;
        }

        return true;
    }

    private static boolean canCastleQueenSide(int row, int col, char color) {
        if ((color == 'w' && row != 7) || (color == 'b' && row != 0)) {
            return false;
        }

        if (isCheck(color)) {
            return false;
        }

        if (isOccupied(row, col - 1) || isOccupied(row, col - 2) || isOccupied(row, col - 3)) {
            return false;
        }

        if (isUnderAttack(row, col, getOpponentColor(color)) || isUnderAttack(row, col - 1, getOpponentColor(color)) || isUnderAttack(row, col - 2, getOpponentColor(color))) {
            return false;
        }

        return true;
    }

    private static boolean isCheck(char color) {
        int[] kingPosition = findKingPosition(color);
        char opponentColor = (color == 'w') ? 'b' : 'w';

        // Check if the king is under attack by opponent's pieces
        return isUnderAttack(kingPosition[0], kingPosition[1], opponentColor);
    }

    private static boolean isUnderAttack(int row, int col, char opponentColor) {
        // Check if the specified square is under attack by the opponent's pieces

        // Check for knight attacks
        int[][] knightMoves = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };
        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValidSquare(newRow, newCol) && chessboard[newRow][newCol].equals(opponentColor + "_N")) {
                return true;
            }
        }

        // Check for bishop/queen attacks (diagonal)
        int[][] diagonalMoves = {
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };
        for (int[] move : diagonalMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            while (isValidSquare(newRow, newCol)) {
                String piece = chessboard[newRow][newCol];
                if (piece.equals(opponentColor + "_B") || piece.equals(opponentColor + "_Q")) {
                    return true;
                } else if (!piece.isEmpty()) {
                    break;
                }
                newRow += move[0];
                newCol += move[1];
            }
        }

        // Check for rook/queen attacks (vertical and horizontal)
        int[][] verticalHorizontalMoves = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1}
        };
        for (int[] move : verticalHorizontalMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            while (isValidSquare(newRow, newCol)) {
                String piece = chessboard[newRow][newCol];
                if (piece.equals(opponentColor + "_R") || piece.equals(opponentColor + "_Q")) {
                    return true;
                } else if (!piece.isEmpty()) {
                    break;
                }
                newRow += move[0];
                newCol += move[1];
            }
        }

        // Check for pawn attacks
        int pawnDirection = (opponentColor == 'w') ? -1 : 1;
        int[][] pawnMoves = {
                {pawnDirection, -1}, {pawnDirection, 1}
        };
        for (int[] move : pawnMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValidSquare(newRow, newCol) && chessboard[newRow][newCol].equals(opponentColor + "_P")) {
                return true;
            }
        }

        // Check for king attacks
        int[][] kingMoves = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };
        for (int[] move : kingMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (isValidSquare(newRow, newCol) && chessboard[newRow][newCol].equals(opponentColor + "_K")) {
                return true;
            }
        }

        return false;
    }

    private static boolean isEnPassantValid(int row, int col, char color) {
        if (enPassantTarget != null && enPassantTarget.equals(getSquareName(row, col))) {
            // Check if the en passant target is valid for the specified square and color
            int[] enPassantSquare = convertPositionToIndexes(enPassantTarget);
            int enPassantRow = enPassantSquare[0];
            int enPassantCol = enPassantSquare[1];
            int direction = (color == 'w') ? -1 : 1;

            if (row == enPassantRow + direction && Math.abs(col - enPassantCol) == 1) {
                String pawnColor = (color == 'w') ? "W" : "B";
                String opponentPawn = getOpponentColor(color) + "_P";
                String adjacentPawn = (col > enPassantCol) ? chessboard[enPassantRow][enPassantCol + 1] : chessboard[enPassantRow][enPassantCol - 1];

                return adjacentPawn.equals(pawnColor + "_P") && chessboard[row][col].isEmpty() && chessboard[enPassantRow][enPassantCol].equals(opponentPawn);
            }
        }

        return false;
    }
    public static boolean isKingInCheck(String[][] chessboard, String colorOfTheKing , boolean player1turn, String kingPosition ) {
		char opponentColor = 'w';
    	if(colorOfTheKing.equals("w")) {
    		opponentColor = 'b';
    	}
    	
    	for(int i = 0 ; i<8 ; i++) {
    		for(int j = 0 ; j < 8; j++) {
    			if(chessboard[i][j] != null && !chessboard[i][j].isEmpty() && chessboard[i][j].charAt(0)==opponentColor){
    				int index[] = {i,j};
    				String moves = getPossibleMoves(chessboard[i][j], index, !player1turn, false);
    				if(moves.contains(kingPosition)) {
    					return true;
    				}
    				
    			}
    		}
    	}
    	
    	
    	return false;
    	
		
	}
    private static int[] findKingPosition(char color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String piece = chessboard[row][col];
                if (piece.equals(color + "_K")) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }


    private static boolean isValidSquare(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    private static boolean isOccupied(int row, int col) {
        if(chessboard[row][col].isEmpty() || chessboard[row][col].equals("   ")) {
        	return false;
        }
        else {
        	return true;
        }
    }

    private static boolean isOccupiedByAlly(int row, int col, char color) {
        String piece = chessboard[row][col];
        return piece.length() > 0 && piece.charAt(0) == color;
    }

    private static boolean isOccupiedByOpponent(int row, int col, char color) {
        String piece = chessboard[row][col];
        if(!piece.isEmpty()) {
        if((piece.charAt(0)!=' ') && (piece.charAt(0)!=color)) {
        	return true;
        }
        else {
        	return false;
        }
        }
        else {
        	return false;
        }
    }

    private static String getSquareName(int row, int col) {
        char colName = (char) (col+97);
        int rowName = 8 - row;
        return colName + "" + rowName;
    }

    private static char getOpponentColor(char color) {
        return (color == 'w') ? 'b' : 'w';
    }


    private static String[][] cloneChessBoard(){
    	 String[][] clonedChessBoard = new String[8][8];
    	 for (int row = 0; row < 8; row++) {
    		 for (int col = 0; col < 8; col++) {
    			 clonedChessBoard[row][col] = chessboard[row][col];

    		 }

    	 }
		return clonedChessBoard;
 
    }
    private static void reupdateOriginalChessBoard(String[][] clonedChessBoard) {
    	for (int row = 0; row < 8; row++) {
   		 for (int col = 0; col < 8; col++) {
   			 chessboard[row][col] = clonedChessBoard[row][col];
   		 }
    	}
    }

    private static boolean isMoveValid(String piece, int[] currentPosition, int[] newPosition, boolean isPlayer1Turn) {
        
    	String[][] clonedChessBoard = cloneChessBoard();

        movePiece(currentPosition, newPosition, chessboard);
        char color = (isPlayer1Turn) ? 'w' : 'b';
        int [] kingPosition = findKingPosition(color);
        String kingSquare = getSquareName(kingPosition[0], kingPosition[1]);
        if(isKingInCheck(chessboard, color+"", isPlayer1Turn, kingSquare)) {
        	reupdateOriginalChessBoard(clonedChessBoard);
        	return false;
        }

        reupdateOriginalChessBoard(clonedChessBoard);
    	
        if (isPiecePinned(currentPosition[0], currentPosition[1], color)) {
            System.out.println("Cannot move a pinned piece.");
            return false;
        }

        String possibleMoves = getPossibleMoves(piece, currentPosition, isPlayer1Turn, false);
        String[] validMoves = possibleMoves.split(" ");

        String destination = getSquareName(newPosition[0], newPosition[1]);
        for (int i = 0 ; i < validMoves.length ; i++) {
            if(i < validMoves.length) {
            	if (validMoves[i].equals(destination)) {
            		return true;
            	}
            	
            }
           
        }
	
        return false;
    }
    

    private static boolean isPiecePinned(int row, int col, char color) {
        // Check if the specified piece at (row, col) is pinned to the king

        // Check for bishop/queen pins (diagonal)
        int[][] diagonalMoves = {
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };
        for (int[] move : diagonalMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            while (isValidSquare(newRow, newCol)) {
                String piece = chessboard[newRow][newCol];
                if (piece.isEmpty()) {
                    newRow += move[0];
                    newCol += move[1];
                    continue;
                } else if (piece.charAt(0) == color) {
                    break;
                } else {
                    // Found an opponent's piece
                    if (piece.charAt(2) == 'B' || piece.charAt(2) == 'Q') {
                        // The piece is pinned if it is not the king and cannot move away from the king
                        if (piece.charAt(2) != 'K' && !canMoveAwayFromKing(newRow, newCol, row, col, color)) {
                            return true;
                        }
                    }
                    break;
                }
            }
        }

        // Check for rook/queen pins (vertical and horizontal)
        int[][] verticalHorizontalMoves = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1}
        };
        for (int[] move : verticalHorizontalMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            while (isValidSquare(newRow, newCol)) {
                String piece = chessboard[newRow][newCol];
                if (piece.isEmpty()) {
                    newRow += move[0];
                    newCol += move[1];
                    continue;
                } else if (piece.charAt(0) == color) {
                    break;
                } else {
                    // Found an opponent's piece
                    if (piece.charAt(2) == 'R' || piece.charAt(2) == 'Q') {
                        // The piece is pinned if it is not the king and cannot move away from the king
                        if (piece.charAt(2) != 'K' && !canMoveAwayFromKing(newRow, newCol, row, col, color)) {
                            return true;
                        }
                    }
                    break;
                }
            }
        }

        return false;
    }

    private static boolean canMoveAwayFromKing(int row, int col, int kingRow, int kingCol, char color) {
        // Check if the specified piece at (row, col) can move away from the king at (kingRow, kingCol)

        // Check if the piece can move to any square that is not under attack by the opponent
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isValidSquare(i, j) && (i != kingRow || j != kingCol)) {
                    String targetPiece = chessboard[i][j];
                    if (targetPiece.isEmpty() || targetPiece.charAt(0) != color) {
                        if (!isUnderAttack(i, j, getOpponentColor(color))) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }


    private static String movePiece(int[] currentPos, int[] newPos , String[][] chessboard) {
        String piece = chessboard[currentPos[0]][currentPos[1]];
        String capturedPiece = chessboard[newPos[0]][newPos[1]];

        chessboard[newPos[0]][newPos[1]] = piece;
        chessboard[currentPos[0]][currentPos[1]] = "";

        return capturedPiece.isEmpty() ? null : capturedPiece;
    }

    private static void handleHelpRequest(String input , boolean helpEnabled ) {
        String position = input.substring(0, 2);
        String helpPosition = input.substring(3, 5);
        helpEnabled = true;
        if (!isValidPosition(position) || !isValidPosition(helpPosition)) {
            System.out.println("Invalid position. Please try again.");
            return;
        }

        int[] pos = convertPositionToIndexes(position);
        String piece = chessboard[pos[0]][pos[1]];
        String color = piece.charAt(0)+"";

        int[] helpPos = convertPositionToIndexes(helpPosition);
        String helpPiece = chessboard[helpPos[0]][helpPos[1]];


        System.out.println(isCapturable(piece, color, helpPosition));
    }
    
    public static HashMap<String, String[]> allPossibleMoves(String color , String position) {

    	HashMap<String, String[]> blackMoves = new HashMap<>();
    	HashMap<String, String[]> whiteMoves = new HashMap<>();
    	String black = new String();
    	String white = new String();
    
    	for(int i = 0 ; i<8 ; i++) {
    		for(int j=0 ; j<8 ; j++) {
    			if(!chessboard[i][j].isEmpty()) {
    				if(color=="w") {
    					if(chessboard[i][j].charAt(0)=='b') {
    						int index[] = {i,j};
	    					 black = getPossibleMoves(chessboard[i][j], index, false,true);
	    					 String allBlackMoves[] = black.split(" ");
	    			    	 
	    					 if (blackMoves.containsKey(chessboard[i][j].charAt(2) + "")) {
	    		                    // Get the existing array for the key
	    		                    String[] existingArray = blackMoves.get(chessboard[i][j].charAt(2) + "");

	    		                    // Concatenate allBlackMoves with the existing array using Arrays.copyOf()
	    		                    int totalLength = existingArray.length + allBlackMoves.length;
	    		                    String[] combinedArray = Arrays.copyOf(existingArray, totalLength);
	    		                    System.arraycopy(allBlackMoves, 0, combinedArray, existingArray.length, allBlackMoves.length);

	    		                    // Put the updated array back into blackMoves
	    		                    blackMoves.put(chessboard[i][j].charAt(2) + "", combinedArray);
	    		                } else {
	    		                    // If the key doesn't exist, simply put allBlackMoves as the value
	    		                    blackMoves.put(chessboard[i][j].charAt(2) + "", allBlackMoves);
	    		                }
	    		            }
	    		        }
	    				 
    			
    				else {
    					if(chessboard[i][j].charAt(0)=='w') {
    						int index[] = {i,j};
	    					 white = getPossibleMoves(chessboard[i][j], index, true,true);
	    					 String allWhiteMoves[] = white.split(" ");
	    					 if (whiteMoves.containsKey(chessboard[i][j].charAt(2) + "")) {
	    		                    // Get the existing array for the key
	    		                    String[] existingArray = whiteMoves.get(chessboard[i][j].charAt(2) + "");

	    		                    // Concatenate allBlackMoves with the existing array using Arrays.copyOf()
	    		                    int totalLength = existingArray.length + allWhiteMoves.length;
	    		                    String[] combinedArray = Arrays.copyOf(existingArray, totalLength);
	    		                    System.arraycopy(allWhiteMoves, 0, combinedArray, existingArray.length, allWhiteMoves.length);

	    		                    // Put the updated array back into blackMoves
	    		                    whiteMoves.put(chessboard[i][j].charAt(2) + "", combinedArray);
	    		                } else {
	    		                    // If the key doesn't exist, simply put allBlackMoves as the value
	    		                    whiteMoves.put(chessboard[i][j].charAt(2) + "", allWhiteMoves);
	    		                }
    					}				
	    				}
	    			}
    			}
    		}
    		
    	
    	if(color == "w") {
    		return blackMoves;
    	}
    	else {
    		return whiteMoves;
    	}
		
	}
    
    public static String isCapturable(String piece ,String color , String position) {
		
    	HashMap<String, String[]> moves = allPossibleMoves(color, position);
    	
    	for (Entry<String, String[]> entry : moves.entrySet() ) {
    		String[] move = entry.getValue();
    		for(int i = 0 ; i<move.length ; i++) {
    			if(move[i].equals(position)) {
    				return "The "+piece+" can be captured by "+keyToString(entry.getKey()) ;
    			}
    		}
    	}
    	return "Safe place.";
		
	}
    public static String keyToString(String key) {
    	
    	switch (key) {
		case "K": {
			return "KING";
		}
		case "P": {
			return "PAWN";
		}
		case "N": {
			return "KNIGHT";
		}
		case "Q": {
			return "QUEEN";
		}
		case "R": {
			return "ROOK";
		}
		case "B": {
			return "BISHOP";
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + key);
		}
		
		
	}

    private static boolean isCapturePossible(String piece, int[] currentPos, int[] helpPos) {
        switch (piece.charAt(2)) {
            case 'P': {
                int direction = (piece.charAt(0) == 'w') ? -1 : 1;
                int dx = helpPos[1] - currentPos[1];
                int dy = helpPos[0] - currentPos[0];

                // Check if the help position is one step diagonally forward
                if (dx == -1 || dx == 1) {
                    return dy == direction;
                }

                // Check if the help position is an en passant capture
                String enPassantTargetPos = enPassantTarget;
                if (enPassantTargetPos != null) {
                    int[] enPassantTargetIndex = convertPositionToIndexes(enPassantTargetPos);
                    if (helpPos[0] == enPassantTargetIndex[0] && helpPos[1] == enPassantTargetIndex[1]) {
                        return true;
                    }
                }

                return false;
            }
            case 'R': {
                // Check if the help position is in the same row or column as the rook
                return currentPos[0] == helpPos[0] || currentPos[1] == helpPos[1];
            }
            case 'N': {
                // Check if the help position is a valid knight move
                int dx = Math.abs(helpPos[1] - currentPos[1]);
                int dy = Math.abs(helpPos[0] - currentPos[0]);
                return (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
            }
            case 'B': {
                // Check if the help position is on the same diagonal as the bishop
                return Math.abs(currentPos[0] - helpPos[0]) == Math.abs(currentPos[1] - helpPos[1]);
            }
            case 'Q': {
                // Check if the help position is on the same row, column, or diagonal as the queen
                return currentPos[0] == helpPos[0] || currentPos[1] == helpPos[1]
                        || Math.abs(currentPos[0] - helpPos[0]) == Math.abs(currentPos[1] - helpPos[1]);
            }
            case 'K': {
                // Check if the help position is adjacent to the king
                int dx = Math.abs(helpPos[1] - currentPos[1]);
                int dy = Math.abs(helpPos[0] - currentPos[0]);
                return (dx == 1 && dy <= 1) || (dy == 1 && dx <= 1);
            }
            default:
                return false;
        }
    }
 
    private static void printChessboard() {
        System.out.println("   a   b   c   d   e   f   g   h");
        for (int i = 0; i < chessboard.length; i++) {
            System.out.print(8 - i + " ");
            for (int j = 0; j < chessboard[i].length; j++) {
                String piece = chessboard[i][j];
                if (piece.isEmpty()) {
                    System.out.print("   ");
                } else {
                    System.out.print(piece + " ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    private static void saveMoveHistoryToFile() {
        try {
        	String path = "C:\\Users\\Aravindhan\\OneDrive\\Desktop\\move_history.txt";
        	path = path.replace("\\","/");
            FileWriter writer = new FileWriter(path);
            writer.write(moveHistory.toString());
            writer.flush();
            writer.close();
            System.out.println("Move history saved to file: move_history.txt");
        } catch (IOException e) {
            System.out.println("Error occurred while saving move history to file.");
            e.printStackTrace();
        }
    }
}
