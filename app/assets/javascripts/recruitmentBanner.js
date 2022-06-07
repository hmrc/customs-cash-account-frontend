document.addEventListener("DOMContentLoaded", () => {
  const banner = new RecruitmentBanner()
  banner.updateDOM()
})


const isBannerVisible = () => localStorage.getItem("recruitment-banner") !== "disabled"
const disableBanner = () => localStorage.setItem("recruitment-banner", "disabled")

class RecruitmentBanner {

  constructor() {
    this.state = { visible: isBannerVisible() }

    this.banner = document.getElementById("recruitment-banner")
    this.banner !== null && this.banner.addEventListener("click", (e) => this.hideBanner(e))
  }

  hideBanner(event) {
    const id = event.target.getAttribute("id")
    if (id === "close-banner") {
      event.preventDefault()
      this.banner.setAttribute("style", "display: none")
      disableBanner()
    }
  }

  updateDOM() {
    this.state.visible && this.banner !== null && this.banner.setAttribute("style", "display: block")
  }

}

