goog.require('goog.dom');
goog.require('goog.dom.TagName');
goog.require('templates.eventsoy');

function initEvents() {
  displayCalendar();
}

function displayCalendar() {
  //Use Google Calendar's calendar display
  const calendarFrame = goog.dom.createDom(goog.dom.TagName.IFRAME, {
      'className': 'calendar-iframe', //check if className
      'src': getCalendarURL(),
  });

  goog.dom.appendChild(goog.dom.getElementById('events-calendar'), calendarFrame);
}

function getCalendarURL() {
  let calendarID = 0;
  // Use to obtain the group id from the current page's URL
  const params = new URLSearchParams(location.search);
  fetch('/calendar?groupid=' + params.get('groupid')).then(response => response.text()).then((requestedID) => {
    calendarID = requestedID;
  });

  return templates.eventsoy.obtainURL({
      'calendarID': calendarID, 
      'timezone': Intl.DateTimeFormat().resolvedOptions().timeZone
  }).toString;
}
