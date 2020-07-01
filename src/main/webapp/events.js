goog.require('goog.dom');
goog.require('goog.dom.TagName');
goog.require('templates.eventsoy');

function initEvents() {
  displayCalendar();
}

function displayCalendar() {
  // Use Google Calendar's calendar display
  const calendarFrame = goog.dom.createDom(goog.dom.TagName.IFRAME, {
      'class': 'calendar-iframe',
      'src': getCalendarUrl(),
  });

  goog.dom.appendChild(goog.dom.getElement('events-calendar'), calendarFrame);
}

function getCalendarUrl() {
  let calendarId = '';
  // Use params to obtain the group id from the current page's URL
  const params = new URLSearchParams(location.search);
  fetch('/events?groupid=' + params.get('groupid')).then(response => response.text()).then((requestedId) => {
    calendarId = requestedId;
  });

  return templates.eventsoy.obtainUrl({
      'calendarId': calendarId, 
      'timezone': Intl.DateTimeFormat().resolvedOptions().timeZone
  }).toString();
}
