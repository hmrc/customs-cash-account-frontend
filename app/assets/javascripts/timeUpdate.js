const timeUpdate = (time) => {
  return `Updated ${moment(time).fromNow()}`
}

document.addEventListener("DOMContentLoaded", (event) => {
  const timeContainer = document.getElementById("update-time")
  // welsh language translation can be added exactly like the one below
  moment.updateLocale("en", {
    relativeTime : {
      past:   "%s",
      s  : "just now",
      m:  "a minute ago",
      mm: "%d minutes ago",
      h:  "an hour ago",
      hh: "%d hours ago"
    }
  });

  if(timeContainer !== null) {
    const timestamp = new Date()
    const interval = timeContainer.getAttribute("data-update-interval")
    const timeout = timeContainer.getAttribute("data-update-timeout")
    timeContainer.setAttribute("style", "display: inline-block")
    timeContainer.setAttribute("aria-hidden", "false")

    const updateDOM = setInterval(() => {
      timeContainer.innerHTML = timeUpdate(timestamp)
    }, interval)

    setTimeout(() => { clearInterval(updateDOM) }, timeout);
  }

});

