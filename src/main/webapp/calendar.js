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
  // Use URLSearchParams to obtain the group id from the current page's URL
  const groupId = (new URLSearchParams(location.search)).get('groupid');
  
  if (groupId !== null && groupId !== '') {
    fetch('/events?groupid=' + groupId).then(response => response.text()).then((requestedId) => {
      calendarId = requestedId;
    });
  }
  
  return templates.eventsoy.obtainUrl({
      'calendarId': calendarId, 
      'timezone': Intl.DateTimeFormat().resolvedOptions().timeZone,
  }).toString();
}
