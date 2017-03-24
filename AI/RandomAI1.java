import java.util.Random;
import java.util.Scanner;

public class RandomAI1
{
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);	
		Random random = new Random();
		random.setSeed(System.currentTimeMillis());;
		
		while (true) {
			String input = "";
			
			// Read board state
			// Read until "END" is detected
			while (!input.equals("END")) {
				input = scanner.nextLine();
			}
			
			// Print a move
			switch (random.nextInt(6)) {
				case 0: System.out.println(">> MOVE RIGHT"); break;
				case 1: System.out.println(">> MOVE LEFT"); break;
				case 2: System.out.println(">> MOVE UP"); break;
				case 3: System.out.println(">> MOVE DOWN"); break;
				case 4: System.out.println(">> DROP BOMB"); break;
				default: System.out.println(">> STAY"); break;
			}
		}
	}
}
