import java.util.GregorianCalendar;
import java.util.Scanner;

/**
 * Terminal/CMD calendar and scheduler program.
 * 
 * @author Jaylan Tse
 * @version 1.0
 */

public class MyCalendarTester {

	private static final Scanner sc = new Scanner(System.in);

	public static void main(String[] args) {
		GregorianCalendar cal = new GregorianCalendar();
		View.printMonth(cal, 0);
		System.out.println();
		EventManager.setScanner(sc);
		View.setScanner(sc);
		prompt();
	}

	/**
	 * Menu to interact with calendar through keyboard inputs.
	 */
	private static void prompt() {
		while (true) {
			System.out.println("Select one of the following options: \n"
					+ "[L]oad,  [V]iew by,  [C]reate,  [G]o to,  [E]vent list,  [D]elete,  [Q]uit");
			switch (sc.next().toLowerCase()) {
			case "l":
				EventManager.loadEvents();
				break;
			case "v":
				View.prompt();
				break;
			case "c":
				EventManager.createEvent();
				break;
			case "g":
				View.goToDate();
				break;
			case "e":
				EventManager.listEvents();
				break;
			case "d":
				EventManager.deleteEvent();
				break;
			case "q":
				EventManager.saveEvents();
				System.exit(0);
				break;
			default:
				System.out.println("Invalid option.");
			}
		}
	}
}