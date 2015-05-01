
public class SimpleCalendar {

	public static void main(String[] args) {
		CalendarModel cm = new CalendarModel();
		CalendarView cv = new CalendarView(cm);
		cm.attach(cv);
	}

}
