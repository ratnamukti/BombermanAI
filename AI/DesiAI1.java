
/*
 * TUGAS 1 SC - BOMBERMAN AI	
 * Author: 
 * Desi Ratna Mukti Umpuan 1306397904
 *
 * Description:
 * Goal utama AI ini adalah memburu AI lain untuk dibunuh.
 * Untuk mencapai goal, AI ini membutuhkan bomb dan powerup sebanyak-banyaknya.
 * Oleh karena itu, ia akan membaca peta board dan mendeteksi bomb/powerup terdekat
 * untuk di-spawn sebelum AI lain memilikinya. Kemudian, bomb yang ia miliki
 * akan digunakan untuk segera membunuh AI lainnya.
 * 
 * P.S:
 * susah banget ga boong pak :(
 * 
 * */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;

public class DesiAI1 {

	public static ArrayList<Tembok> tembok = new ArrayList<Tembok>();
	public static ArrayList<Tembok> bomArr = new ArrayList<Tembok>();
	public static ArrayList<Player> otherplayer = new ArrayList<Player>();
	
	public static ArrayList<Integer> poX = new ArrayList<Integer>();
	public static ArrayList<Integer> poY = new ArrayList<Integer>();
	public static ArrayList<String> poType = new ArrayList<String>();
	public static ArrayList<Boolean> poIsSpawn = new ArrayList<Boolean>();
	
	public static PriorityQueue<Powerup> pq = new PriorityQueue<Powerup>();
	public static String[][] board = new String[2][2];
	public static String[][] koorplayer = new String[1][1];
	public static Player player;
	public static String status = "AMAN";
	public static Tembok sementara = new Tembok("0", "0");
	public static Player target;
	public static String move = "";
	public static String nickname = (new Object() {
	}).getClass().getEnclosingClass().getName();

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		Random random = new Random();
		random.setSeed(System.currentTimeMillis());
		int jmlplayer;
		int boardCol;
		int boardRow;
		int bomb;
		String x = "";
		String y = "";
		String[] splited;
		int b = 0;
		int m = 0;

		while (true) {

			// arraylist yang di perbaharui setiap looping
			// reset publik tiap looping
			tembok = new ArrayList<Tembok>();
			bomArr = new ArrayList<Tembok>();
			pq = new PriorityQueue<Powerup>();
			otherplayer = new ArrayList<Player>();
			String input = "";
			int turn = 0;
			int jmlPemain = 0;

			// Read board state
			// Read until "END" is detected
			while (!input.equals("END")) {

				// baca turn
				input = scanner.nextLine();
				splited = input.split(" ");

				if (splited[0].equals("TURN")) {
					turn = Integer.parseInt(splited[1]);
					//System.out.println(turn);

				} else if (splited[0].equals("PLAYER")) {
					// baca jumlah player
					jmlplayer = Integer.parseInt(splited[1]);
					//input = scanner.nextLine();
					//splited = input.split(" ");
					//System.out.println(jmlplayer);
					for (int i = 0; i < jmlplayer; i++) {
						input = scanner.nextLine();
						splited = input.split(" ");

						String name = splited[0].substring(1);
						String npm = splited[1];

						String[] bombdata = splited[2].split(":");
						String[] bombdata2 = bombdata[1].split("/");

						b = Integer.parseInt(bombdata2[0]);
						m = Integer.parseInt(bombdata2[1]);
						String range = splited[3];
						String[] parserange = range.split(":");

						int bombrange = Integer.parseInt(parserange[1]);

						String playerstatus = splited[4];
						int score = Integer.parseInt(splited[5]);

						// create player
						if (splited[1].equals(nickname)) {
							System.out.println("<< setting player: " + splited[0].substring(1));
							player = new Player(name, npm, b, m, bombrange, playerstatus, score, x, y);
							System.out.println("player created!!!!");
							player.setName(splited[0].substring(1));
						} else {
							otherplayer.add(new Player(name, npm, b, m, bombrange, playerstatus, score, x, y));
						}
					}

				} else if (splited[0].equals("BOARD")) {
					// baca ukuran board
					//input = scanner.nextLine();
					//splited = input.split(" ");
					boardRow = Integer.parseInt(splited[1]);
					boardCol = Integer.parseInt(splited[2]);
					board = new String[boardRow][boardCol];
					System.out.println("<< player " + player);
					
					// FOR LAMA
					for (int i = 0; i < boardRow; i++) {
						// baca peta
						input = scanner.nextLine();

						// membuat inputan menjadi lebih mudah di olah
						input = input.replace("     ", ".")
								.replace(" ", "").replace("]", "")
								.substring(1).replace("[", " ") + " ^";
						splited = input.split(" ");
						System.out.println("<< line:" + input);
						System.out.println(Arrays.toString(splited));

						for (int j = 0; j < boardCol; j++) {
							input = splited[j];
							
							// cek semua kemungkinan entity dalam peta
							if (input.equals(".")) {
								board[i][j] = ".";
							}

							else if (input.substring(0, 1).equals("#") || input.substring(0, 1).equals("X")) {
								if (input.substring(0, 1).equals("X")) {
									
									tembok.add(new Tembok("" + i, "" + j));
									System.out.println("ini pq: " + pq);
									

									if (input.substring(1, 2).equals("B")) {
										board[i][j] = "h";
										// kalo ketemu XBX, masukin ke priorityqueue
										poX.add(i);
										poY.add(j);
										poType.add("Bom");
										poIsSpawn.add(false);
										
									} else if (input.substring(1, 2).equals("P")) {
										board[i][j] = "h";
										// kalo ketemu XPX, masukin ke priorityqueue
										poX.add(i);
										poY.add(j);
										poType.add("PowerUp");
										poIsSpawn.add(false);
									
									}
								}
								//System.out.println("<< Yg masuk ke board: " + input.substring(1, 2));
								board[i][j] = input.substring(1, 2);

							} else {
								// bentuk lain kayak number, hurup, dan plus
								// TODO
								String[] parsed = input.split(";");

								for (int k = 0; k < parsed.length; k++) {
									input = parsed[k];
									System.out.println("<< Lagi iterasi baca: " + input);
									String awalInput = input.substring(0, 1);

									// setting untuk bom, ada power dan juga waktu ledakannya
									// dan masukkan kedalam array bom
									if (awalInput.equals("B")) {
										int powerBom = Integer.parseInt(input.substring(1, input.length()-1));
										int timeBom = Integer.parseInt(input.substring(input.length()-1));
										board[i][j] = "b";
										bomArr.add(new Tembok("" + i, "" + j));

										System.out.println("disini " + i + j);
										
									} else if (awalInput.equals("F")) {

										// cek apakah itu flare
										String timeFlare = input.substring(1);
										board[i][j] = "f";
										String flareRange = input.substring(1, 2);

									} else if (awalInput.equals("+")) {
										// cek apakah itu power up
										String powerUP = input.substring(1);
										board[i][j] = "p";

										String tipePower = input.substring(1, 2);
										// bila ketemu petak dengan +B
										if (tipePower.equals("B")) {
											poX.add(i);
											poY.add(j);
											poType.add("Bom");
											poIsSpawn.add(true);
										} else if (tipePower.equals("P")) {
											poX.add(i);
											poY.add(j);
											poType.add("PowerUp");
											poIsSpawn.add(true);
										}

									} else {
										// Player
										System.out.println("<< player input: " + input);
										System.out.println("<< player: " + player);
										if (input.equals(player.getName())) {
											// dapetin angka player di dalam board lalu set koor i dan j
											System.out.println("<< ini punya gw");
											System.out.println("<< i dan j: " + i + ", " + j);
											player.setX("" + i);
											player.setY("" + j);
										} else {
											for (int n = 0; n < otherplayer.size(); n++) {
												String otherPlayerName = "" + otherplayer.get(n).getName();
												if (input.equals(otherPlayerName)) {
													System.out.println("<< ini punya player " + otherPlayerName);
													otherplayer.get(n).setX("" + i);
													otherplayer.get(n).setY("" + j);
												}
											}
										}
										board[i][j] = input;
									}
								}
								

							}
						} // punya board col
					} // punya board row
				} //board
			}// !END
			
			for(int w = 0; w < poY.size(); w++){
				int col = poX.get(w);
				int row = poY.get(w);
				boolean iss = poIsSpawn.get(w);
				Powerup newPowerup = new Powerup(col, row, iss);
			}

			// kalau bomb yang saya miliki >= 2, maka cari player lain buat dibunuh
			if (b >= 2) {
				target = nearestPlayer(otherplayer);
				moveToPlayer(target);

			} else if (bomArr.size() == 0) {
				status = "AMAN";
				sementara = nearestTembok(tembok);
				moveToTembok(sementara);

			} else {
				sementara = nearestTembok(bomArr);
				kaburFromBom(sementara);
			}
			System.out.println(sementara.getX() + " " + sementara.getY() + status);
			System.out.println(player.getX() + " " + player.getY() + status);

		} // while true

	} // main method

	// ini bekerja dengan cek dari 2 method dibawahnya
	// di cek move mana yang terbaik dari atas - kiri - bawah - kanan
	// menggunakan MD untuk menentukan jarak terdekat atau terjauh
	public static void moveToTembok(Tembok tembok) {
		// DLS deep 1 LOL + MD
		int tmpA = 9000;
		int tmpKn = 9000;
		int tmpKr = 9000;
		int tmpB = 9000;
		int xP = Integer.parseInt(player.getX());
		int yP = Integer.parseInt(player.getY());
		int xT = Integer.parseInt(tembok.getX());
		int yT = Integer.parseInt(tembok.getY());

		if (validMove(xP - 1, yP)) {
			tmpA = Math.abs(xP - 1 - xT) + Math.abs(yP - yT);
			if (move.equals("DOWN"))
				tmpA += 2;
		}
		if (validMove(xP + 1, yP)) {
			tmpB = Math.abs(xP + 1 - xT) + Math.abs(yP - yT);
			if (move.equals("UP"))
				tmpB += 2;
		}
		if (validMove(xP, yP + 1)) {
			tmpKn = Math.abs(xP - xT) + Math.abs(yP - yT + 1);
			if (move.equals("LEFT"))
				tmpKn += 2;
		}
		if (validMove(xP, yP - 1)) {
			tmpKr = Math.abs(xP - xT) + Math.abs(yP - yT - 1);
			if (move.equals("RIGHT"))
				tmpKr += 2;
		}
		if (status.equals("PASANG")) {
			move = "DROP";
			System.out.println(">> DROP BOMB");
			return;
		}

		int hasil = Math.min(tmpA, Math.min(tmpB, Math.min(tmpKn, tmpKr)));
		if (hasil == 9000) {
			move = "STAY";
			System.out.println(">> STAY");
			return;
		}
		if (tmpA == hasil) {
			move = "UP";
			System.out.println(">> MOVE UP");
			return;
		}
		if (tmpKr == hasil) {
			move = "LEFT";
			System.out.println(">> MOVE LEFT");
			return;
		}
		if (tmpB == hasil) {
			move = "DOWN";
			System.out.println(">> MOVE DOWN");
			return;
		}
		if (tmpKn == hasil) {
			move = "RIGHT";
			System.out.println(">> MOVE RIGHT");
			return;
		}

	}

	public static void moveToPlayer(Player otherplayer) {
		// DLS deep 1 LOL + MD
		int tmpA = 9000;
		int tmpKn = 9000;
		int tmpKr = 9000;
		int tmpB = 9000;
		int xP = Integer.parseInt(player.getX());
		int yP = Integer.parseInt(player.getY());
		int xO = Integer.parseInt(otherplayer.getX());
		int yO = Integer.parseInt(otherplayer.getY());

		if ((validMove(xP - 1, yP))) {
			tmpA = Math.abs(xP - 1 - xO) + Math.abs(yP - yO);
			if (move.equals("DOWN"))
				tmpA += 2;
		}

		if (validMove(xP + 1, yP)) {
			tmpB = Math.abs(xP + 1 - xO) + Math.abs(yP - yO);
			if (move.equals("UP"))
				tmpB += 2;
		}

		if (validMove(xP, yP + 1)) {
			tmpKn = Math.abs(xP - xO) + Math.abs(yP - yO + 1);
			if (move.equals("LEFT"))
				tmpKn += 2;
		}

		if (validMove(xP, yP - 1)) {
			tmpKr = Math.abs(xP - xO) + Math.abs(yP - yO - 1);
			if (move.equals("RIGHT"))
				tmpKr += 2;
		}

		if (status.equals("PASANG")) {
			move = "DROP";
			System.out.println(">> DROP BOMB");
			return;
		}

		int hasil = Math.min(tmpA, Math.min(tmpB, Math.min(tmpKn, tmpKr)));
		if (hasil == 9000) {
			move = "STAY";
			System.out.println(">> STAY");
			return;
		}
		if (tmpA == hasil) {
			move = "UP";
			System.out.println(">> MOVE UP");
			return;
		}
		if (tmpKr == hasil) {
			move = "LEFT";
			System.out.println(">> MOVE LEFT");
			return;
		}
		if (tmpB == hasil) {
			move = "DOWN";
			System.out.println(">> MOVE DOWN");
			return;
		}
		if (tmpKn == hasil) {
			move = "RIGHT";
			System.out.println(">> MOVE RIGHT");
			return;
		}

	}

	public static void kaburFromBom(Tembok tembok) {
		// DLS deep 1 LOL + MD
		int tmpA = -1;
		int tmpKn = -1;
		int tmpKr = -1;
		int tmpB = -1;
		int xP = Integer.parseInt(player.getX());
		int yP = Integer.parseInt(player.getY());
		int xT = Integer.parseInt(tembok.getX());
		int yT = Integer.parseInt(tembok.getY());
		if (validMove(xP - 1, yP)) {
			tmpA = Math.abs(xP - 1 - xT) + Math.abs(yP - yT);
			// if(move.equals("DOWN")) tmpA -=2;
		}
		if (validMove(xP + 1, yP)) {
			tmpB = Math.abs(xP + 1 - xT) + Math.abs(yP - yT);
			// if(move.equals("UP")) tmpB -=2;
		}
		if (validMove(xP, yP + 1)) {
			tmpKn = Math.abs(xP - xT) + Math.abs(yP - yT + 1);
			// if(move.equals("LEFT")) tmpKn -=2;
		}
		if (validMove(xP, yP - 1)) {
			tmpKr = Math.abs(xP - xT) + Math.abs(yP - yT - 1);
			// if(move.equals("RIGHT")) tmpKr -=2;
		}
		int hasil = Math.max(tmpA, Math.max(tmpB, Math.max(tmpKn, tmpKr)));
		if (hasil <= 1 && xP != xT && yP != yT) {
			move = "STAY";
			System.out.println(">> STAY");
			return;
		}
		if (tmpA == hasil) {
			move = "UP";
			System.out.println(">> MOVE UP");
			return;
		}
		if (tmpKr == hasil) {
			move = "LEFT";
			System.out.println(">> MOVE LEFT");
			return;
		}
		if (tmpB == hasil) {
			move = "DOWN";
			System.out.println(">> MOVE DOWN");
			return;
		}
		if (tmpKn == hasil) {
			move = "RIGHT";
			System.out.println(">> MOVE RIGHT");
			return;
		}
	}

	// cek siapakah yang paling dekat dari arraylist yang diberikan
	// dibandingkan dengan metode MD sehingga diketahui jarak terdekatnya
	public static Tembok nearestTembok(ArrayList<Tembok> tembok) {
		if (tembok.size() < 1) {
			return null;
		}
		Tembok tmp = tembok.get(0);
		System.out.println("<< Get player x dan y: " + player.getX() + " " + player.getY());
		System.out.println("<< Get tembok x dan y: " + tmp.getX() + " " + tmp.getY());
		System.out.println("<< ----------");
		int resultTmp = Math.abs(Integer.parseInt(tmp.getX()) - Integer.parseInt(player.getX()))
				+ Math.abs(Integer.parseInt(tmp.getY()) - Integer.parseInt(player.getY()));
		for (int i = 1; i < tembok.size(); i++) {
			int resultTandingan = Math.abs(Integer.parseInt(tembok.get(i).getX()) - Integer.parseInt(player.getX()))
					+ Math.abs(Integer.parseInt(tembok.get(i).getY()) - Integer.parseInt(player.getY()));

			if (resultTandingan < resultTmp) {
				tmp = tembok.get(i);
				resultTmp = resultTandingan;
			}
		}
		return tmp;
	}

	// cari player terdekat dari kita untuk di bunuh hohoho
	public static Player nearestPlayer(ArrayList<Player> otherplayer) {
		if (otherplayer.size() < 1) {
			return null;
		}
		Player tmp = otherplayer.get(0);
		int resultTmp = Math.abs(Integer.parseInt(tmp.getX()) - Integer.parseInt(player.getX()))
				+ Math.abs(Integer.parseInt(tmp.getY()) - Integer.parseInt(player.getY()));
		for (int i = 1; i < otherplayer.size(); i++) {
			int resultTandingan = Math
					.abs(Integer.parseInt(otherplayer.get(i).getX()) - Integer.parseInt(player.getX()))
					+ Math.abs(Integer.parseInt(otherplayer.get(i).getY()) - Integer.parseInt(player.getY()));

			if (resultTandingan < resultTmp) {
				tmp = otherplayer.get(i);
				resultTmp = resultTandingan;
			}
		}
		return tmp;
	}

	// pada method ini cek apakah koordinat tersebeut bisa dilewati atau tidak
	// terserah kalian yg menentukan valid seperti apa validnya
	public static boolean validMove(int x, int y) {
		if (x < 0 || x >= board.length || y < 0 || y >= board[0].length) {
			return false;
		} else if (board[x][y].equals("#") || board[x][y].equals("f") || board[x][y].equals("b")) {
			return false;
		} else if (board[x][y].equals("X") || board[x][y].equals("h")) {
			status = "PASANG";
			return false;
		} else if (board[x][y].equals("f")) {
			status = "STAY";
			return false;
		}
		return true;
	}

	static class Player {
		private String name;
		private String npm;
		private int b, m;
		private int bombrange;
		private String playerstatus;
		private int score;
		private String x;
		private String y;

		public Player(String name, String npm, int b, int m, int bombrange, String playerstatus, int score, String x,
				String y) {
			this.name = name;
			this.npm = npm;
			this.b = b;
			this.m = m;
			this.bombrange = bombrange;
			this.playerstatus = playerstatus;
			this.score = score;
			this.x = x;
			this.y = y;
		}

		public String getName() {
			return this.name;
		}

		public String getNpm() {
			return this.npm;
		}

		public int getB() {
			return this.b;
		}

		public int getM() {
			return this.m;
		}

		public int getBombrange() {
			return this.bombrange;
		}

		public String getPlayerstatus() {
			return this.playerstatus;
		}

		public int getScore() {
			return this.score;
		}

		public String getX() {
			return this.x;
		}

		public String getY() {
			return this.y;
		}

		public void setName(String newName) {
			name = newName;
		}

		public void setNpm(String newNpm) {
			npm = newNpm;
		}

		public void setB(int b) {
			this.b = b;
		}

		public void setM(int m) {
			this.m = m;
		}

		public void setBombrange(int newBombrange) {
			bombrange = newBombrange;
		}

		public void setPlayerstatus(String newPlayerstatus) {
			playerstatus = newPlayerstatus;
		}

		public void setScore(int newScore) {
			score = newScore;
		}

		public void setX(String newX) {
			x = newX;
		}

		public void setY(String newY) {
			y = newY;
		}

	} // class player

	static class Tembok {
		public String x;
		public String y;

		public Tembok(String x, String y) {
			this.x = x;
			this.y = y;
		}

		public String getX() {
			return this.x;
		}

		public String getY() {
			return this.y;
		}
	} // class tembok

	static class Powerup implements Comparable<Powerup> {
		private int x;
		private int y;
		private boolean isSpawn;

		public Powerup(int x, int y, boolean isSpawn) {
			this.x = x;
			this.y = y;
			this.isSpawn = isSpawn;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}
		
		public boolean getIsSpawn() {
			return this.isSpawn;
		}

		public void setX(int newX) {
			this.x = newX;
		}

		public void setY(int newY) {
			this.y = newY;
		}
		
		public void setIsSpawn(boolean newIsSpawn) {
			this.isSpawn = newIsSpawn;
		}

		// Semua powerup dan bomb yang terdeteksi bakal masuk ke priority queue
		// untuk menentukan mana yang posisinya paling dekat
		@Override
		public int compareTo(Powerup o) {
			// TODO Auto-generated method stub 
			return (Math.abs(this.x - Integer.parseInt(player.getX()))
					+ Math.abs(this.y - Integer.parseInt(player.getY())))
					- (Math.abs(o.getX() - Integer.parseInt(player.getX()))
							+ Math.abs(o.getY() - Integer.parseInt(player.getY())));

		}
	} // class bomb
} // main class
