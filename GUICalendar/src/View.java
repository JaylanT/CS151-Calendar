import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Scanner;

/**
 * Prints out dates and months of calendar.
 * 
 * @author Jaylan Tse
 * @version 1.0
 */

public class View {

	private static Scanner sc;
	private static Boolean printToday = true;

	/**
	 * Sets scanner.
	 * @param sc The scanner to set as.
	 */
	public static void setScanner(Scanner sc) {
		View.sc = sc;
	}

	/**
	 * UI prompt for viewing by day or month.
	 */
	public static void prompt() {
		System.out.println("[D]ay view or [M]onth view?");
		String option = sc.next().toLowerCase();
		while (!option.equals("d") && !option.equals("m")) {
			System.out.println("Invalid option. Please enter \'D\' or \'M\'.");
			option = sc.next().toLowerCase();
		}
		if (option.equals("d")) {
			viewByDay();
		} else {
			viewByMonth();
		}
	}

	/**
	 * Goes to user specified date in day view.
	 */
	public static void goToDate() {
		System.out.print("Date (MM/DD/YYYY): ");
		String date = sc.next();
		while (!date.matches("^(1[0-2]|0[1-9])/(3[01]|[12][0-9]|0[1-9])/[0-9]{4}$")) {
			System.out.println("Invalid date format. Use MM/DD/YYYY.");
			System.out.print("Date (MM/DD/YYYY): ");
			date = sc.next();
		}
		int month = Integer.valueOf(date.substring(0, 2)) - 1;
		int day = Integer.valueOf(date.substring(3, 5));
		int year = Integer.valueOf(date.substring(6));

		GregorianCalendar cal = new GregorianCalendar();
		cal.set(year, month, day);
		printDate(cal, 0);
		viewPrompt(cal, "day");
	}

	/**
	 * Prints a formatted calendar with today or events highlighted.
	 * @param cal The object containing date information to be displayed.
	 * @param i The month offset from current month.
	 */
	public static void printMonth(GregorianCalendar cal, int i) {
		int today = cal.get(Calendar.DAY_OF_MONTH);
		cal.add(Calendar.MONTH, i);
		cal.set(Calendar.DAY_OF_MONTH, 1);

		System.out.println("\n" + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " " + cal.get(Calendar.YEAR));
		System.out.println(" Su  Mo  Tu  We  Th  Fr  Sa");

		int dayNum = 0;
		for (int j = 1; j < cal.get(Calendar.DAY_OF_WEEK); j++) {
			System.out.print("    ");
			dayNum++;
		}
		for (int k = 1; k <= cal.getActualMaximum(Calendar.DAY_OF_MONTH); k++) {
			if (EventManager.hasEvent(getFormattedDate(cal.get(Calendar.MONTH) + 1, k, cal.get(Calendar.YEAR)))
					|| (k == today && printToday)) {
				System.out.printf("[%2s]", k);
				printToday = false;
			} else {
				System.out.printf("%3s ", Integer.toString(k));
			}
			dayNum++;
			if (dayNum == 7 && k != cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
				dayNum = 0;
				System.out.println();
			}
		}
		System.out.println();
	}

	/**
	 * Calls printDate method and starts prompt for traversing calendar by day.
	 */
	private static void viewByDay() {
		GregorianCalendar cal = new GregorianCalendar();
		printDate(cal, 0);
		viewPrompt(cal, "day");
	}

	/**
	 * Calls printMonth method and starts prompt for traversing calendar by month.
	 */
	private static void viewByMonth() {
		GregorianCalendar cal = new GregorianCalendar();
		printMonth(cal, 0);
		viewPrompt(cal, "month");
	}

	/**
	 * Prompt for traversing calendar by day or month.
	 * @param cal The object containing date information to be displayed.
	 * @param type View by day or month.
	 */
	private static void viewPrompt(GregorianCalendar cal, String type) {
		System.out.println("\n[P]revious, [N]ext, or [M]ain menu?");
		String option = sc.next().toLowerCase();
		while (!option.equals("m")) {
			while (!option.equals("p") && !option.equals("n") && !option.equals("m")) {
				System.out.println("Invalid option. Please enter \'P\', \'N\', or \'M\'.");
				option = sc.next().toLowerCase();
			}
			switch (option) {
			case "m":
				return;
			case "p":
				if (type.equals("day")) {
					printDate(cal, -1);
				} else {
					printMonth(cal, -1);
				}
				break;
			case "n":
				if (type.equals("day")) {
					printDate(cal, 1);
				} else {
					printMonth(cal, 1);
				}
				break;
			}
			System.out.println("\n[P]revious, [N]ext, or [M]ain menu?");
			option = sc.next().toLowerCase();
		}
	}

	/**
	 * Prints date of a single day.
	 * @param cal The object containing date information to be displayed.
	 * @param i The day offset from current day.
	 */
	private static void printDate(GregorianCalendar cal, int i) {
		MONTHS[] arrayOfMonths = MONTHS.values();
		DAYS[] arrayOfDays = DAYS.values();
		cal.add(Calendar.DAY_OF_WEEK, i);

		System.out.println();
		System.out.print(arrayOfDays[cal.get(Calendar.DAY_OF_WEEK) - 1]);
		System.out.print(", ");
		System.out.print(arrayOfMonths[cal.get(Calendar.MONTH)]);
		System.out.print(" ");
		System.out.print(cal.get(Calendar.DAY_OF_MONTH));
		System.out.print(", ");
		System.out.print(cal.get(Calendar.YEAR) + "\n");

		EventManager.printEvent(getFormattedDate(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
				cal.get(Calendar.YEAR)));
	}

	/**
	 * Creates a formatted date String of form MM/DD/YYYY.
	 * @param monthNum The month.
	 * @param dayNum The day.
	 * @param year The year.
	 * @return The formatted date String.
	 */
	private static String getFormattedDate(int monthNum, int dayNum, int year) {
		String month = String.valueOf(monthNum);
		if (monthNum < 10) {
			month = "0" + monthNum;
		}
		String day = String.valueOf(dayNum);
		if (dayNum < 10) {
			day = "0" + dayNum;
		}
		return month + "/" + day + "/" + year;
	}
}