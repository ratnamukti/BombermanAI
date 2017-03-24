import java.util.Scanner;
import java.util.*;

public class TegarAI2
{
	static class Player{
		public  String x;
		public  String y;
		public  String nomorPlayer;

		public Player(String x, String y, String nomorPlayer){
			this.x = x;
			this.y = y;
			this.nomorPlayer = nomorPlayer;
		}
	}
	static class Tembok{
		public String x;
		public String y;

		public Tembok(String x, String y){
			this.x = x;
			this.y = y;
		}
		public String getX(){
			return this.x;
		}
		public String getY(){
			return this.y;
		}
	}
	public static ArrayList<Tembok> tembok = new ArrayList<Tembok>();
	public static Player player = new Player("1","1","0");
	public static String nickname = "TegarAI2";
	public static String [][] board = new String[2][2];
	public static String status = "AMAN";
	public static Tembok sementara = new Tembok("0","0");
	public static ArrayList<Tembok> bomArr = new ArrayList<Tembok>();
	public static String move= "";
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);	
		
		while (true) {
			tembok = new ArrayList<Tembok>();
			bomArr = new ArrayList<Tembok>();
			String input = "";
			int turn = 0;
                                 int jmlPemain = 0;
                                 
			// Read board state
			// Read until "END" is detected
			while (!input.equals("END")) {
				input = scanner.nextLine();
                                            String [] inputarray =input.split(" ");
                                            if (inputarray[0].equals("TURN")){
                                            	turn = Integer.parseInt(inputarray[1]);
                                            }
                                            else if(inputarray[0].equals("PLAYER")){
                                            	jmlPemain = Integer.parseInt(inputarray[1]);
                                            	for(int i = 0 ; i < jmlPemain ; i++){
                                            		input = scanner.nextLine();
                                            		String [] inputarrayC = input.split(" ");
                                            		if(inputarrayC[1].equals(nickname)){
                                            			player.nomorPlayer = inputarrayC[0].substring(1);
                                            		}
                                            	}
                                            }
                                            else if(inputarray[0].equals("BOARD")){
                                            	// masukin peta ke board dari outputnya
                                            	int row = Integer.parseInt(inputarray[1]);
                                            	int col = Integer.parseInt(inputarray[2]);
                                            	board = new String[row][col];
                                            	for (int i = 0 ; i < row ; i++){
                                            		input = scanner.nextLine();
                                            		input = input.replace(" ","").replace("]","").substring(1).replace("["," ") + " ^";
                                            		inputarray = input.split(" ");
                                            		for(int j = 0 ; j < col ; j++){
                                            			input = inputarray[j];
                                            			if(input.equals("")){
                                            				board[i][j] = ".";
                                            			}
                                            			else if(input.substring(0,1).equals("#") || input.substring(0,1).equals("X")){
                                            				if(input.substring(0,1).equals("X")){
                                            					tembok.add(new Tembok(""+i , ""+j));
                                            				}
                                            				board[i][j] = input.substring(0,1);
                                            			}else {
                                            				String [] inputarrayB = input.split(";");
                                            				if(inputarrayB.length > 0){
	                                            				for(int k = 0 ; k < inputarrayB.length ; k++){
	                                            					input = inputarrayB[k];
	                                            					String awalInput = input.substring(0,1);
	                                            					if(awalInput.equals("B")){
	                                            						String powerBom = input.substring(1,input.length());
	                                            						String timeBom = input.substring(input.length());
	                                            						board[i][j] = "b";
	                                            						bomArr.add(new Tembok(""+i,""+j));
	                                            					}else if (awalInput.equals("F")){
	                                            						String timeFlare = input.substring(1);
	                                            						board[i][j] = "f";
	                                            					}else if(awalInput.equals("+")){
	                                            						String powerUP = input.substring(1);
	                                            						board[i][j] = "p";
	                                            					}else {
	                                            						//Player
	                                            						if(input.equals(player.nomorPlayer)){
	                                            							player.x = ""+i;
	                                            							player.y = ""+j;
	                                            						}
	                                            						board[i][j] = input;
	                                            					}
	                                            				}
                                            				}
                                            			}
                                            		}
                                            	}
                                            }
			}
			if(bomArr.size() == 0 ){
				status = "AMAN";
				sementara = nearestTembok(tembok);
				moveToTembok(sementara);
			}else{
				sementara = nearestTembok(bomArr);
				kaburFromBom(sementara);
			}
			System.out.println(sementara.getX() + " " + sementara.getY() + status);
			System.out.println(player.x + " " + player.y + status);
		}
	}
	public static void moveToTembok(Tembok tembok){
		// DLS deep 1 LOL + MD
		int tmpA = 9000;
		int tmpKn = 9000;
		int tmpKr = 9000;
		int tmpB = 9000;
		int xP = Integer.parseInt(player.x);
		int yP = Integer.parseInt(player.y);
		int xT = Integer.parseInt(tembok.x);
		int yT = Integer.parseInt(tembok.y);
		if(validMove(xP-1,yP)){
			tmpA = Math.abs(xP-1-xT) + Math.abs(yP-yT);
			if(move.equals("DOWN")) tmpA +=2;
		}
		if(validMove(xP+1,yP)){
			tmpB = Math.abs(xP+1-xT) + Math.abs(yP-yT);
			if(move.equals("UP")) tmpB +=2;
		}
		if(validMove(xP,yP+1)){
			tmpKn = Math.abs(xP-xT) + Math.abs(yP-yT+1);
			if(move.equals("LEFT")) tmpKn +=2;
		}
		if(validMove(xP,yP-1)){
			tmpKr = Math.abs(xP-xT) + Math.abs(yP-yT-1);
			if(move.equals("RIGHT")) tmpKr +=2;
		}
		if(status.equals("PASANG")){
			move = "DROP";
			System.out.println(">> DROP BOMB");
			return;
		}
		int hasil = Math.min(tmpA,Math.min(tmpB,Math.min(tmpKn,tmpKr)));
    		if(hasil == 9000){
    			move = "STAY";
    			System.out.println(">> STAY");
    			return;
    		}
		if(tmpA == hasil){
			move = "UP";
			System.out.println(">> MOVE UP");
			return;
		}
		if(tmpKr == hasil){
			move = "LEFT";
			System.out.println(">> MOVE LEFT");
			return;
		}
		if(tmpB == hasil){
			move = "DOWN";
			System.out.println(">> MOVE DOWN");
			return;
		}
		if(tmpKn == hasil){
			move = "RIGHT";
			System.out.println(">> MOVE RIGHT");
			return;
		}

	}
	public static void kaburFromBom(Tembok tembok){
		// DLS deep 1 LOL + MD
		int tmpA = -99;
		int tmpKn = -99;
		int tmpKr = -99;
		int tmpB = -99;
		int xP = Integer.parseInt(player.x);
		int yP = Integer.parseInt(player.y);
		int xT = Integer.parseInt(tembok.getX());
		int yT = Integer.parseInt(tembok.getY());
		if(validMove(xP-1,yP)){
			tmpA = Math.abs(xP-1-xT) + Math.abs(yP-yT);
			//if(move.equals("DOWN")) tmpA -=2;
		}
		if(validMove(xP+1,yP)){
			tmpB = Math.abs(xP+1-xT) + Math.abs(yP-yT);
			//if(move.equals("UP")) tmpB -=2;
		}
		if(validMove(xP,yP+1)){
			tmpKn = Math.abs(xP-xT) + Math.abs(yP-yT+1);
			//if(move.equals("LEFT")) tmpKn -=2;
		}
		if(validMove(xP,yP-1)){
			tmpKr = Math.abs(xP-xT) + Math.abs(yP-yT-1);
			//if(move.equals("RIGHT")) tmpKr -=2;
		}
		int hasil = Math.max(tmpA,Math.max(tmpB,Math.max(tmpKn,tmpKr)));
		if(hasil <= 1 && xP != xT && yP != yT || hasil == -99){
			move = "STAY";
			System.out.println(">> STAY");
			return;
		}
		if(tmpA == hasil){
			move = "UP";
			System.out.println(">> MOVE UP");
			return;
		}
		if(tmpKr == hasil){
			move = "LEFT";
			System.out.println(">> MOVE LEFT");
			return;
		}
		if(tmpB == hasil){
			move = "DOWN";
			System.out.println(">> MOVE DOWN");
			return;
		}
		if(tmpKn == hasil){
			move = "RIGHT";
			System.out.println(">> MOVE RIGHT");
			return;
		}

	}
	public static Tembok nearestTembok(ArrayList<Tembok> tembok){
		if(tembok.size() < 1){
			return null;
		}
		Tembok tmp = tembok.get(0);
		int resultTmp = Math.abs(Integer.parseInt(tmp.x) - Integer.parseInt(player.x)) + Math.abs(Integer.parseInt(tmp.y) - Integer.parseInt(player.y));
		for(int i = 1 ; i < tembok.size() ; i++){
			int resultTandingan = Math.abs(Integer.parseInt(tembok.get(i).x) - Integer.parseInt(player.x)) + Math.abs(Integer.parseInt(tembok.get(i).y) - Integer.parseInt(player.y));
			
			if(resultTandingan < resultTmp){
				tmp = tembok.get(i);
				resultTmp = resultTandingan;
			}
		}
		return tmp;
	}
	public static boolean validMove(int x, int y){
		if(x < 0 || x >= board.length || y < 0 || y >= board[0].length){
			return false;
		} else if(board[x][y].equals("#") || board[x][y].equals("f") || board[x][y].equals("b")){
			return false;
		}else if(board[x][y].equals("X")){
			status = "PASANG";
			return false;
		}
		return true;
	}

}