
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
	private HashMap<String, ArrayList<Event>> eventMap = new HashMap<>();
	private ArrayList<ChangeListener> listeners = new ArrayList<>();
	private GregorianCalendar cal = new GregorianCalendar();
	private int selectedDay = 0;
	private boolean monthChanged = false;
	
	public CalendarModel() {
		cal.set(Calendar.DAY_OF_MONTH, 1);
		maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		loadEvents();
	}
	
	public void attach(ChangeListener l) {
		listeners.add(l);
	}
	
	public void update() {
		for (ChangeListener l : listeners) {
			l.stateChanged(new ChangeEvent(this));
		}
	}
	
	public void setSelectedDate(int day) {
		selectedDay = day;
		cal.set(Calendar.DAY_OF_MONTH, selectedDay);
	}
	
	public int getSelectedDay() {
		return selectedDay;
	}

	public int getCurrentYear() {
		return cal.get(Calendar.YEAR);
	}
	
	public int getCurrentMonth() {
		return cal.get(Calendar.MONTH);
	}
	
	public int getDayOfWeek(int i) {
		cal.set(Calendar.DAY_OF_MONTH, i);
		return cal.get(Calendar.DAY_OF_WEEK);
	}
	
	public int getMaxDays() {
		return maxDays;
	}

	public void nextMonth() {
		cal.add(Calendar.MONTH, 1);
		maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		monthChanged = true;
		update();
	}
	
	public void prevMonth() {
		cal.add(Calendar.MONTH, -1);
		maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		monthChanged = true;
		update();
	}
	
	public void nextDay() {
		selectedDay++;
		if (selectedDay > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
			selectedDay = 1;
			nextMonth();
		}
		update();
	}
	
	public void prevDay() {
		selectedDay--;
		if (selectedDay < 1) {
			prevMonth();
			selectedDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		}
		update();
	}
	
	/**
	 * @return
	 */
	public boolean hasMonthChanged() {
		return monthChanged;
	}
	
	/**
	 * 
	 */
	public void resetHasMonthChanged() {
		monthChanged = false;
	}
	
	/**
	 * @param title
	 * @param startTime
	 * @param endTime
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
	 * @param date The date given by user.
	 * @return Boolean if the date has an event.
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
	 * Converts 24:00 time to minutes
	 * @param time the time in 24 hour format
	 * @return the time converted to minutes
	 */
	private int convertHourToMin(String time) {
		int hours = Integer.valueOf(time.substring(0, 2));
		return hours * 60 + Integer.valueOf(time.substring(3));
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
			System.out.println("First run. No events found.");
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
		 * Constructor for Event.
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
