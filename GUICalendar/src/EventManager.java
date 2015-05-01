import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Manages events for calendar.
 * 
 * @author Jaylan Tse
 * @version 1.0
 */

public class EventManager {

	private static Scanner sc;
	private static HashMap<String, ArrayList<Event>> eventMap = new HashMap<>();
	private static boolean cleared = false;

	/**
	 * Sets scanner.
	 * @param sc The scanner to set as.
	 */
	public static void setScanner(Scanner sc) {
		EventManager.sc = sc;
	}

	/**
	 * Creates an event from user inputs.
	 */
	public static void createEvent() {
		sc.nextLine();
		System.out.print("Title: ");
		String title = sc.nextLine();
		System.out.print("Date (MM/DD/YYYY): ");
		String date = sc.next();
		while (!date.matches("^(1[0-2]|0[1-9])/(3[01]|[12][0-9]|0[1-9])/[0-9]{4}$")) {
			System.out.println("Invalid date format. Use MM/DD/YYYY.");
			System.out.print("Date (MM/DD/YYYY): ");
			date = sc.next();
		}
		System.out.print("Time frame (00:00-24:00): ");
		String time = sc.next();
		while ((time.length() != 5 && time.length() != 11) || !time.substring(0, 5).matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
			System.out.println("Invalid time format. Use XX:XX.");
			System.out.print("Time frame (00:00-24:00): ");
			time = sc.next();
		}
		sc.nextLine();

		Event e = new Event(title, date, time);
		ArrayList<Event> eventArray = new ArrayList<>();
		if (hasEvent(e.date)) {
			eventArray = eventMap.get(date);
		}
		eventArray.add(e);
		eventMap.put(e.date, eventArray);
		System.out.println();
	}

	/**
	 * Prints all events for specified date.
	 * @param date The date given by user.
	 */
	public static void printEvent(String date) {
		if (hasEvent(date)) {
			ArrayList<Event> eventArray = eventMap.get(date);
			Collections.sort(eventArray, timeComparator());
			for (Event e : eventArray) {
				System.out.println("\t" + e.getEntry());
			}
		}
	}

	/**
	 * Checks if specified date has any events scheduled.
	 * @param date The date given by user.
	 * @return Boolean if the date has an event.
	 */
	public static Boolean hasEvent(String date) {
		return eventMap.containsKey(date);
	}

	/**
	 * Removes all events from specified date or clears all events that are loaded.
	 */
	public static void deleteEvent() {
		System.out.println("[S]elected or [A]ll?");
		String option = sc.next().toLowerCase();
		while (!option.equals("s") && !option.equals("a")) {
			System.out.println("Invalid option. Please enter \'S\' or \'A\'.");
			option = sc.next().toLowerCase();
		}
		if (option.equals("s")) {
			System.out.print("Date (MM/DD/YYYY): ");
			String date = sc.next();
			while (!date.matches("^(1[0-2]|0[1-9])/(3[01]|[12][0-9]|0[1-9])/[0-9]{4}$")) {
				System.out.println("Invalid date format. Use MM/DD/YYYY.");
				System.out.print("Date (MM/DD/YYYY): ");
				date = sc.next();
			}
			eventMap.remove(date);
		} else {
			eventMap.clear();
		}
		sc.nextLine();
		cleared = true;
		System.out.println();
	}

	/**
	 * Prints out all events sorted by date and starting time.
	 */
	public static void listEvents() {
		System.out.println();
		List<String> sortedDates = new ArrayList<>(eventMap.keySet());

		Collections.sort(sortedDates, new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				if (arg0.substring(6).equals(arg1.substring(6))) {
					if (arg0.substring(0, 2).equals(arg1.substring(0, 2))) {
						return Integer.parseInt(arg0.substring(3, 5)) - Integer.parseInt(arg1.substring(3, 5));
					} else {
						return Integer.parseInt(arg0.substring(0, 2)) - Integer.parseInt(arg1.substring(0, 2));
					}
				}
				return Integer.parseInt(arg0.substring(6)) - Integer.parseInt(arg1.substring(6));
			}
		});

		for (String date : sortedDates) {
			ArrayList<Event> eventArray = eventMap.get(date);
			List<Event> sortedEvents = new ArrayList<>(eventArray);

			Collections.sort(sortedEvents, timeComparator());

			Boolean printDate = true;
			for (Event e : sortedEvents) {
				if (printDate) {
					System.out.println(e.getFormattedDate());
					printDate = false;
				}
				System.out.println("\t" + e.getEntry());
			}
		}
		System.out.println();
	}

	/**
	 * Saves all events to "events.ser".
	 */
	public static void saveEvents() {
		if (eventMap.isEmpty() && !cleared) {
			return;
		}
		try {
			FileOutputStream fOut = new FileOutputStream("events.ser");
			ObjectOutputStream oOut = new ObjectOutputStream(fOut);
			oOut.writeObject(eventMap);
			oOut.close();
			fOut.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Loads all events from "events.ser".
	 */
	@SuppressWarnings("unchecked")
	public static void loadEvents() {
		try {
			FileInputStream fIn = new FileInputStream("events.ser");
			ObjectInputStream oIn = new ObjectInputStream(fIn);
			HashMap<String, ArrayList<Event>> temp = (HashMap<String, ArrayList<Event>>) oIn.readObject();
			for (String date : temp.keySet()) {
				if (hasEvent(date)) {
					ArrayList<Event> eventArray = eventMap.get(date);
					eventArray.addAll(temp.get(date));
				} else {
					eventMap.put(date, temp.get(date));
				}
			}
			oIn.close();
			fIn.close();
			System.out.println("\nEvents successfully loaded.\n");
		} catch (IOException ioe) {
			System.out.println("First run. No events found.\n");
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
		}
	}

	/**
	 * Comparator for comparing by time in format XX:XX.
	 * @return The comparator.
	 */
	private static Comparator<Event> timeComparator() {
		return new Comparator<Event>() {
			@Override
			public int compare(Event arg0, Event arg1) {
				if (arg0.time.substring(0, 2).equals(arg1.time.substring(0, 2))) {
					return Integer.parseInt(arg0.time.substring(3, 5)) - Integer.parseInt(arg1.time.substring(3, 5));
				}
				return Integer.parseInt(arg0.time.substring(0, 2)) - Integer.parseInt(arg1.time.substring(0, 2));
			}
		};
	}

	/**
	 * Event class containing event title, date, and time.
	 * 
	 * @author Jaylan Tse
	 */
	private static class Event implements Serializable {

		/**
         * For serialization.
         */
		private static final long serialVersionUID = -8867677570991913141L;
		private String title;
		private String date;
		private String time;

		/**
		 * Constructor for Event.
		 * @param title The title of the event.
		 * @param date The date of the event.
		 * @param time The time of the event.
		 */
		private Event(String title, String date, String time) {
			this.title = title;
			this.date = date;
			this.time = time;
		}

		/**
		 * Gets the event entry.
		 * @return The event entry in format "XX:XX event title".
		 */
		private String getEntry() {
			return String.format("%11s", time) + "   " + title;
		}

		/**
		 * Gets a date String in the format "Day, Month DayOfMonth, Year".
		 * @return The formatted date String.
		 */
		private String getFormattedDate() {
			MONTHS[] arrayOfMonths = MONTHS.values();
			DAYS[] arrayOfDays = DAYS.values();

			int month = Integer.valueOf(date.substring(0, 2)) - 1;
			int day = Integer.valueOf(date.substring(3, 5));
			int year = Integer.valueOf(date.substring(6));

			GregorianCalendar cal = new GregorianCalendar();
			cal.set(year, month, day);

			return arrayOfDays[cal.get(Calendar.DAY_OF_WEEK) - 1] + ", " + arrayOfMonths[month] + " " + day + ", " + year;
		}
	}
}