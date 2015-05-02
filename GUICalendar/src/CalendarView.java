import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CalendarView implements ChangeListener {

	private CalendarModel model;
	private DAYS[] arrayOfDays = DAYS.values();
	private MONTHS[] arrayOfMonths = MONTHS.values();
	private int prevHighlight = -1;
	private int maxDays;

	private JFrame frame = new JFrame("Calendar");
	private JPanel monthViewPanel = new JPanel();
	private JLabel monthLabel = new JLabel();
	private JButton create = new JButton("Create");
	private JButton nextDay = new JButton("Next");
	private JButton prevDay = new JButton("Prev");
	private JTextPane dayTextPane = new JTextPane();
	private ArrayList<JButton> dayBtns = new ArrayList<JButton>();

	/**
	 * Constructs the calendar.
	 * @param model the  model that stores and manipulates calendar data
	 */
	public CalendarView(CalendarModel model) {
		this.model = model;
		maxDays = model.getMaxDays();
		monthViewPanel.setLayout(new GridLayout(0, 7));
		dayTextPane.setPreferredSize(new Dimension(300, 150));
		dayTextPane.setEditable(false);
		JScrollPane dayScrollPane = new JScrollPane(dayTextPane);
		dayScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		createDayBtns();
		addBlankBtns();
		addDayBtns();
		highlightEvents();

		create.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				createEventDialog();
			}
		});
		JButton prevMonth = new JButton("<");
		prevMonth.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.prevMonth();
				create.setEnabled(false);
				nextDay.setEnabled(false);
				prevDay.setEnabled(false);
				dayTextPane.setText("");
			}
		});
		JButton nextMonth = new JButton(">");
		nextMonth.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.nextMonth();
				create.setEnabled(false);
				nextDay.setEnabled(false);
				prevDay.setEnabled(false);
				dayTextPane.setText("");
			}
		});
		
		JPanel monthContainer = new JPanel();
		monthContainer.setLayout(new BorderLayout());
		monthLabel.setText(arrayOfMonths[model.getCurrentMonth()] + " " + model.getCurrentYear());
		monthContainer.add(monthLabel, BorderLayout.NORTH);
		monthContainer.add(new JLabel("       S             M             T             W             T              F             S"), BorderLayout.CENTER);
		monthContainer.add(monthViewPanel, BorderLayout.SOUTH);
		
		JPanel dayViewPanel = new JPanel();
		dayViewPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		dayViewPanel.add(dayScrollPane, c);
		JPanel btnsPanel = new JPanel();
		nextDay.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.nextDay();
			}
		});
		prevDay.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.prevDay();
			}
		});
		btnsPanel.add(prevDay);
		btnsPanel.add(create);
		btnsPanel.add(nextDay);
		c.gridx = 0;
		c.gridy = 1;
		dayViewPanel.add(btnsPanel, c);

		JButton quit = new JButton("Quit");
		quit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.saveEvents();
				System.exit(0);
			}
		});
		
		showDate(model.getSelectedDay());
		highlightSelectedDate(model.getSelectedDay() - 1);

		frame.add(prevMonth);
		frame.add(monthContainer);
		frame.add(nextMonth);
		frame.add(dayViewPanel);
		frame.add(quit);
		frame.setLayout(new FlowLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (model.hasMonthChanged()) {
			maxDays = model.getMaxDays();
			dayBtns.clear();
			monthViewPanel.removeAll();
			monthLabel.setText(arrayOfMonths[model.getCurrentMonth()] + " " + model.getCurrentYear());
			createDayBtns();
			addBlankBtns();
			addDayBtns();
			highlightEvents();
			prevHighlight = -1;
			model.resetHasMonthChanged();
			frame.pack();
			frame.repaint();
		} else {
			showDate(model.getSelectedDay());
			highlightSelectedDate(model.getSelectedDay() - 1);
		}
	}

	/**
	 * Creates an event on the selected date through user input.
	 */
	private void createEventDialog() {
		final JDialog eventDialog = new JDialog();
		eventDialog.setTitle("Create event");
		eventDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		final JTextField eventText = new JTextField(30);
		final JTextField timeStart = new JTextField(10);
		final JTextField timeEnd = new JTextField(10);
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (eventText.getText().isEmpty()) {
					return;
				}
				if ((!eventText.getText().isEmpty() && (timeStart.getText().isEmpty() || timeEnd.getText().isEmpty()))
						|| timeStart.getText().length() != 5
						|| timeEnd.getText().length() != 5
						|| !timeStart.getText().matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")
						|| !timeEnd.getText().matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
					JDialog timeErrorDialog = new JDialog();
					timeErrorDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
					timeErrorDialog.setLayout(new GridLayout(2, 0));
					timeErrorDialog.add(new JLabel("Please enter start and end time in format XX:XX."));
					JButton ok = new JButton("Okay");
					ok.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							timeErrorDialog.dispose();
						}
					});
					timeErrorDialog.add(ok);
					timeErrorDialog.pack();
					timeErrorDialog.setVisible(true);
				} else if (!eventText.getText().equals("")) {
					if (model.hasEventConflict(timeStart.getText(), timeEnd.getText())) {
						JDialog conflictDialog = new JDialog();
						conflictDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
						conflictDialog.setLayout(new GridLayout(2, 0));
						conflictDialog.add(new JLabel("Time conflict."));
						JButton ok = new JButton("Okay");
						ok.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								conflictDialog.dispose();
							}
						});
						conflictDialog.add(ok);
						conflictDialog.pack();
						conflictDialog.setVisible(true);
					} else {
						eventDialog.dispose();
						model.createEvent(eventText.getText(), timeStart.getText(), timeEnd.getText());
						showDate(model.getSelectedDay());
						highlightEvents();
					}
				}
			}
		});
		eventDialog.setLayout(new GridBagLayout());
		JLabel date = new JLabel();
		date.setText(model.getCurrentMonth() + 1 + "/" + model.getSelectedDay() + "/" + model.getCurrentYear());
		date.setBorder(BorderFactory.createEmptyBorder());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.gridx = 0;
		c.gridy = 0;
		eventDialog.add(date, c);
		c.gridy = 1;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_START;
		eventDialog.add(new JLabel("Event"), c);
		c.gridy = 2;
		eventDialog.add(eventText, c);
		c.gridy = 3;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.LINE_START;
		eventDialog.add(new JLabel("Time Start (00:00)"), c);
		c.anchor = GridBagConstraints.CENTER;
		eventDialog.add(new JLabel("Time End (24:00)"), c);
		c.gridy = 4;
		c.anchor = GridBagConstraints.LINE_START;
		eventDialog.add(timeStart, c);
		c.anchor = GridBagConstraints.CENTER;
		eventDialog.add(timeEnd, c);
		c.anchor = GridBagConstraints.LINE_END;
		eventDialog.add(save, c);
		eventDialog.pack();
		eventDialog.setVisible(true);
	}

	/**
	 * Shows the selected date and events on that date.
	 * @param d the selected date
	 */
	private void showDate(final int d) {
		model.setSelectedDate(d);
		String dayOfWeek = arrayOfDays[model.getDayOfWeek(d) - 1] + "";
		String date = (model.getCurrentMonth() + 1) + "/" + d + "/" + model.getCurrentYear();
		String events = "";
		if (model.hasEvent(date)) {
			events += model.getEvents(date);
		}
		dayTextPane.setText(dayOfWeek + " " + date + "\n" + events);
	}

	/**
	 * Highlights the currently selected date.
	 * @param d the currently selected date
	 */
	private void highlightSelectedDate(int d) {
		Border border = new LineBorder(Color.ORANGE, 2);
		dayBtns.get(d).setBorder(border);
		if (prevHighlight != -1) {
			dayBtns.get(prevHighlight).setBorder(new JButton().getBorder());
		}
		prevHighlight = d;
	}

	/**
	 * Highlights days containing events.
	 */
	private void highlightEvents() {
		for (int i = 1; i <= maxDays; i++) {
			if (model.hasEvent((model.getCurrentMonth() + 1) + "/" + i + "/" + model.getCurrentYear())) {
				dayBtns.get(i - 1).setBackground(Color.decode("0xE4EFF8"));
			}
		}
	}

	/**
	 * Creates buttons representing days of the current month and adds them to an array list.
	 */
	private void createDayBtns() {
		for (int i = 1; i <= maxDays; i++) {
			final int d = i;
			JButton day = new JButton(Integer.toString(d));
			day.setBackground(Color.WHITE);
	
			day.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					showDate(d);
					highlightSelectedDate(d - 1);
					create.setEnabled(true);
					nextDay.setEnabled(true);
					prevDay.setEnabled(true);
				}
			});
			dayBtns.add(day);
		}
	}

	/**
	 * Adds the buttons representing the days of the month to the panel.
	 */
	private void addDayBtns() {
		for (JButton d : dayBtns) {
			monthViewPanel.add(d);
		}
	}

	/**
	 * Adds filler buttons before the start of the month to align calendar.
	 */
	private void addBlankBtns() {
		for (int j = 1; j < model.getDayOfWeek(1); j++) {
			JButton blank = new JButton();
			blank.setEnabled(false);
			monthViewPanel.add(blank);
		}
	}
}
