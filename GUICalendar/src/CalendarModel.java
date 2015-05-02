
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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class CalendarModel{

	private int maxDays;
	private int selectedDay;
	private HashMap<String, ArrayList<Event>> eventMap = new HashMap<>();
	private ArrayList<ChangeListener> listeners = new ArrayList<>();
	private GregorianCalendar cal = new GregorianCalendar();
	private boolean monthChanged = false;
	
	/**
	 * Constructor
	 */
	public CalendarModel() {
		selectedDay = cal.get(Calendar.DATE);
		maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		loadEvents();
	}
	
	/**
	 * Adds ChangeListeners to array.
	 * @param l the ChangeListener
	 */
	public void attach(ChangeListener l) {
		listeners.add(l);
	}
	
	/**
	 * Updates all ChangeListeners in array.
	 */
	public void update() {
		for (ChangeListener l : listeners) {
			l.stateChanged(new ChangeEvent(this));
		}
	}
	
	/**
	 * Sets the user selected day.
	 * @param day the day of the month
	 */
	public void setSelectedDate(int day) {
		selectedDay = day;
	}
	
	/**
	 * Gets the user selected day.
	 * @return the day of the month
	 */
	public int getSelectedDay() {
		return selectedDay;
	}

	/**
	 * Gets the current year the calendar is at.
	 * @return the current year
	 */
	public int getCurrentYear() {
		return cal.get(Calendar.YEAR);
	}
	
	/**
	 * Gets the current month the calendar is at.
	 * @return the current month
	 */
	public int getCurrentMonth() {
		return cal.get(Calendar.MONTH);
	}
	
	/**
	 * Gets the value representing the day of the week
	 * @param i the day of the month
	 * @return the day of the week (1-7)
	 */
	public int getDayOfWeek(int i) {
		cal.set(Calendar.DAY_OF_MONTH, i);
		return cal.get(Calendar.DAY_OF_WEEK);
	}
	
	/**
	 * Gets the max number of days in a month.
	 * @return the max number of days in a month
	 */
	public int getMaxDays() {
		return maxDays;
	}

	/**
	 * Moves the calendar forward by one month.
	 */
	public void nextMonth() {
		cal.add(Calendar.MONTH, 1);
		maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		monthChanged = true;
		update();
	}
	
	/**
	 * Moves the calendar backward by one month.
	 */
	public void prevMonth() {
		cal.add(Calendar.MONTH, -1);
		maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		monthChanged = true;
		update();
	}
	
	/**
	 * Moves the selected day forward by one.
	 */
	public void nextDay() {
		selectedDay++;
		if (selectedDay > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
			selectedDay = 1;
			nextMonth();
		}
		update();
	}
	
	/**
	 * Moves the selected day backward by one.
	 */
	public void prevDay() {
		selectedDay--;
		if (selectedDay < 1) {
			prevMonth();
			selectedDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		}
		update();
	}
	
	/**
	 * Checks if the month has changed as a result of user interaction.
	 * @return
	 */
	public boolean hasMonthChanged() {
		return monthChanged;
	}
	
	/**
	 * Resets monthChanged to false.
	 */
	public void resetHasMonthChanged() {
		monthChanged = false;
	}
	
	/**
	 * Creates an event on the currently selected date.
	 * @param title the title of the event
	 * @param startTime the start time of the event
	 * @param endTime the end time of the event
	 */
	public void createEvent(String title, String startTime, String endTime) {
		String date = (cal.get(Calendar.MONTH) + 1) + "/" + selectedDay + "/" + cal.get(Calendar.YEAR);
		Event e = new Event(title, date, startTime, endTime);
		ArrayList<Event> eventArray = new ArrayList<>();
		if (hasEvent(e.date)) {
			eventArray = eventMap.get(date);
		}
		eventArray.add(e);
		eventMap.put(date, eventArray);
	}
	
	/**
	 * Checks if specified date has any events scheduled.
	 * @param date the selected date in format MM/DD/YYYY
	 * @return if the date has an event
	 */
	public Boolean hasEvent(String date) {
		return eventMap.containsKey(date);
	}

	/**
	 * Checks if a new event has a time conflict with an existing event.
	 * @param timeStart the start time of the new event
	 * @param timeEnd the end time of the new event
	 * @return whether there is a conflict in time
	 */
	public Boolean hasEventConflict(String timeStart, String timeEnd) {
		String date = (getCurrentMonth() + 1) + "/" + selectedDay + "/" + getCurrentYear();
		if (!hasEvent(date)) {
			return false;
		}
		
		ArrayList<Event> eventArray = eventMap.get(date);
		Collections.sort(eventArray, timeComparator());
		
		int timeStartMins = convertHourToMin(timeStart), timeEndMins = convertHourToMin(timeEnd);
		for (Event e : eventArray) {
			int eventStartTime = convertHourToMin(e.startTime), eventEndTime = convertHourToMin(e.endTime);
			if (timeStartMins >= eventStartTime && timeStartMins < eventEndTime) {
				return true;
			} else if (timeStartMins <= eventStartTime && timeEndMins > eventStartTime) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets a string of all events on a particular date.
	 * @param date the date to get events for
	 * @return a string of all events on specified date
	 */
	public String getEvents(String date) {
		ArrayList<Event> eventArray = eventMap.get(date);
		Collections.sort(eventArray, timeComparator());
		String events = "";
		for (Event e : eventArray) {
			events += e.toString() + "\n";
		}
		return events;
	}
	
	/**
	 * Saves all events to "events.ser".
	 */
	public void saveEvents() {
		if (eventMap.isEmpty()) {
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
	private void loadEvents() {
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
		} catch (IOException ioe) {
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
		}
	}
	
	/**
	 * Converts 24:00 time to minutes
	 * @param time the time in 24 hour format
	 * @return the time converted to minutes
	 */
	private int convertHourToMin(String time) {
		int hours = Integer.valueOf(time.substring(0, 2));
		return hours * 60 + Integer.valueOf(time.substring(3));
	}

	/**
	 * Comparator for comparing by time in format XX:XX.
	 * @return The comparator.
	 */
	private static Comparator<Event> timeComparator() {
		return new Comparator<Event>() {
			@Override
			public int compare(Event arg0, Event arg1) {
				if (arg0.startTime.substring(0, 2).equals(arg1.startTime.substring(0, 2))) {
					return Integer.parseInt(arg0.startTime.substring(3, 5)) - Integer.parseInt(arg1.startTime.substring(3, 5));
				}
				return Integer.parseInt(arg0.startTime.substring(0, 2)) - Integer.parseInt(arg1.startTime.substring(0, 2));
			}
		};
	}
	
	/**
	 * Event object containing event title, date, and time.
	 */
	private static class Event implements Serializable {

		private static final long serialVersionUID = -6030371583841330976L;
		private String title;
		private String date;
		private String startTime;
		private String endTime;

		/**
		 * Constructor
		 * @param title the title of the event
		 * @param date the date of the event
		 * @param startTime the start time of the event
		 * @param endTime the end time of the event
		 */
		private Event(String title, String date, String startTime, String endTime) {
			this.title = title;
			this.date = date;
			this.startTime = startTime;
			this.endTime = endTime;
		}
		
		/**
		 * Gets the event entry as a string.
		 * @return the event entry in format "XX:XX - XX:XX: title".
		 */
		public String toString() {
			if (endTime.equals("")) {
				return startTime + ": " + title;
			}
			return startTime + " - " + endTime + ": " + title;
		}
	}
}
