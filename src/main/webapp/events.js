function initEvents() {
  displayCalendar();
}

function displayCalendar() {
  document.getElementById('events-calendar').innerHTML = '<iframe src="' + getCalendarURL() + '"></iframe>';
}

function getCalendarURL() {
  let URL = "https://calendar.google.com/calendar/embed?src=";

  //Use to obtain the group id from the current page's URL
  const params = new URLSearchParams(location.search);

  fetch('/calendar?groupid=' + params.get('groupid')).then(response => response.text()).then((calendarID) => {
    URL += calendarID;
  });

  // Check the client's timezone and add it to the calendar URL.
  URL += "&ctz=" + Intl.DateTimeFormat().resolvedOptions().timeZone;

  return URL;
}
