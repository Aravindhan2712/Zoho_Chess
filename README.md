# Zoho_Chess
•	In a ChessBoard the rows are numbers and colums are alphabets
•	So the positions of pieces are denoted by their index as follows:( The king side pawn the pawn in front of the king is denoted by e2 therefore the positions the pawns can move to are e3 and e4 (i.e) the squares in from of the pawn
•	Other than entering positions three additional functions can be used .They are
o	Print
o	Exit 
o	Help
•	Print : The command to activate the operation is print which prints the current state of the board
•	Exit: The command to activate the operation is exit which exits the current operation
•	Help: The command to activate the operation is (the position of the piece to be moved |space|the position the piece should be moved to followed by –help. Therefore the command looks like this e2 e4 --help . 
•	Please makesure to follow the syntax as they are case sensitive
•	The input and outputs will be like:
Enter the position of the piece you want to move:
e2
		Enter the position to which the piece is to be moved:
			e4
		Now the board will update accordingly
		And the program is executed until one player is CheckMated
•	All the moves will be noted in a file one of the player is executed
•	Here are a few moves for a quick CheckMate
o	1st move = f2 -> f3 (White player or 1st Player’s move)
o	2nd move = e7 -> e5 (Black player or 2nd Player’s move)
o	3rd move = g2->g4(White player or 1st Player’s move)
o	4th move =  d8->h4 e5 (Black player or 2nd Player’s move)
o	CheckMate
o	This move is known as fool’ mate shortest mate
