let protectedUrl = "";
let createdForTargetUrl = null;


/* ================= THEME TOGGLE ================= */

(function initTheme() {

    const saved = localStorage.getItem("theme");

    if (saved === "dark") {
        document.documentElement.setAttribute("data-theme", "dark");
    }

})();

document
    .getElementById("themeToggle")
    .addEventListener("click", function () {

        const isDark =
            document.documentElement.getAttribute("data-theme") === "dark";

        if (isDark) {
            document.documentElement.removeAttribute("data-theme");
            localStorage.setItem("theme", "light");
        } else {
            document.documentElement.setAttribute("data-theme", "dark");
            localStorage.setItem("theme", "dark");
        }

    });


/* ================= MOBILE SIDEBAR ================= */

const sidebar = document.getElementById("sidebar");
const sidebarOverlay = document.getElementById("sidebarOverlay");

function openSidebar() {
    sidebar.classList.add("open");
    sidebarOverlay.classList.add("open");
}

function closeSidebar() {
    sidebar.classList.remove("open");
    sidebarOverlay.classList.remove("open");
}

document.getElementById("menuToggle").addEventListener("click", openSidebar);
sidebarOverlay.addEventListener("click", closeSidebar);

document.querySelectorAll(".side-nav-link").forEach(function (link) {
    link.addEventListener("click", closeSidebar);
});


/* ================= DYNAMIC LIMIT EXPLAINER ================= */

function updateLimitExplainer() {

    const limit = document.getElementById("limit").value || 0;
    const windowSeconds = document.getElementById("window").value || 0;

    document.getElementById("limitExplainer").textContent =
        "Your API will allow up to " + limit + " requests every " + windowSeconds + " seconds.";

}

document.getElementById("limit").addEventListener("input", updateLimitExplainer);
document.getElementById("window").addEventListener("input", updateLimitExplainer);


/* ================= COUNT-UP FOR METRIC NUMBERS ================= */

function animateNumber(el, from, to, duration) {

    from = Number(from);
    to = Number(to);

    if (isNaN(from) || isNaN(to)) {
        el.textContent = to;
        return;
    }

    const start = performance.now();

    function step(now) {

        const progress = Math.min((now - start) / duration, 1);
        const value = Math.round(from + (to - from) * progress);

        el.textContent = value;

        if (progress < 1) {
            requestAnimationFrame(step);
        } else {
            el.textContent = to;
        }

    }

    requestAnimationFrame(step);

}

function setMetric(id, value) {

    const el = document.getElementById(id);
    const previous = el.textContent;
    const card = el.closest(".metric");

    if (previous !== "—" && !isNaN(Number(previous)) && !isNaN(Number(value))) {
        animateNumber(el, previous, value, 350);
    } else {
        el.textContent = value;
    }

    if (card) {
        card.classList.add("flash");
        setTimeout(() => card.classList.remove("flash"), 400);
    }

}


/* ================= RESET WHEN TARGET URL CHANGES ================= */

function resetGeneratedState() {

    protectedUrl = "";
    createdForTargetUrl = null;

    document.getElementById("endpointFilled").classList.add("hidden");
    document.getElementById("endpointFilled").classList.remove("reveal");
    document.getElementById("endpointEmpty").classList.remove("hidden");
    document.getElementById("protectedUrl").textContent = "—";

    document.getElementById("limitValue").textContent = "—";
    document.getElementById("remainingValue").textContent = "—";
    document.getElementById("resetValue").textContent = "—";

    document.getElementById("requestStatus").classList.add("hidden");
    document.getElementById("requestStatus").classList.remove("blocked");

    document.getElementById("responseCode").textContent = "—";
    document.getElementById("responseBody").textContent = "No request made yet.";

}

document
    .getElementById("targetUrl")
    .addEventListener("input", function () {

        if (createdForTargetUrl !== null && this.value !== createdForTargetUrl) {
            resetGeneratedState();
        }

    });


/* ================= CREATE PROTECTED API ================= */

document
    .getElementById("rateLimitForm")
    .addEventListener("submit", async function (event) {

        event.preventDefault();

        const targetUrl =
            document.getElementById("targetUrl").value;

        const apiKey =
            document.getElementById("apiKey").value;

        const limit =
            Number(document.getElementById("limit").value);

        const windowSeconds =
            Number(document.getElementById("window").value);

        const createBtn = document.getElementById("createBtn");
        const createBtnLabel = document.getElementById("createBtnLabel");

        createBtn.disabled = true;
        createBtnLabel.textContent = "Creating protected endpoint...";

        try {

            const response = await fetch(
                "/api/protected/create",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        targetUrl: targetUrl,
                        key: apiKey,
                        limit: limit,
                        windowSeconds: windowSeconds
                    })
                }
            );


            if (!response.ok) {

                throw new Error(
                    "Failed to create protected API"
                );
            }


            protectedUrl =
                await response.text();

            createdForTargetUrl = targetUrl;


            document.getElementById(
                "protectedUrl"
            ).textContent = protectedUrl;


            document.getElementById("limitValue").textContent = limit;
            document.getElementById("remainingValue").textContent = limit;
            document.getElementById("resetValue").textContent = windowSeconds;


            document.getElementById("endpointEmpty").classList.add("hidden");

            const endpointFilled = document.getElementById("endpointFilled");
            endpointFilled.classList.remove("hidden");
            endpointFilled.classList.add("reveal");
            setTimeout(() => endpointFilled.classList.remove("reveal"), 400);


            createBtnLabel.textContent = "Protected endpoint created";


            document.getElementById("endpointSection").scrollIntoView({
                behavior: "smooth"
            });


        } catch (error) {

            createBtnLabel.textContent = "Create Protected Endpoint";
            alert(error.message);

        } finally {

            createBtn.disabled = false;

            if (createBtnLabel.textContent === "Protected endpoint created") {

                setTimeout(() => {
                    createBtnLabel.textContent = "Create Protected Endpoint";
                }, 2200);

            }

        }

    });


/* ================= COPY URL ================= */

document
    .getElementById("copyBtn")
    .addEventListener("click", async function () {

        if (!protectedUrl) {

            alert("Create a protected API first.");

            return;
        }


        await navigator.clipboard.writeText(
            protectedUrl
        );


        const originalHTML = this.innerHTML;

        this.classList.add("copied");
        this.innerHTML =
            '<svg viewBox="0 0 16 16" fill="none"><path d="M3 8.5L6.2 11.5L13 4.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg> Copied';


        setTimeout(() => {

            this.classList.remove("copied");
            this.innerHTML = originalHTML;

        }, 1500);

    });


/* ================= TEST API ================= */

document
    .getElementById("testBtn")
    .addEventListener("click", async function () {

        if (!protectedUrl) {

            alert("Create a protected API first.");

            return;
        }


        try {

            const response =
                await fetch(protectedUrl);


            const body =
                await response.text();


            /* Rate limit headers */

            const limit =
                response.headers.get(
                    "X-RateLimit-Limit"
                );

            const remaining =
                response.headers.get(
                    "X-RateLimit-Remaining"
                );

            const reset =
                response.headers.get(
                    "X-RateLimit-Reset"
                );


            if (limit !== null) {
                setMetric("limitValue", limit);
            }


            if (remaining !== null) {
                setMetric("remainingValue", remaining);
            }


            if (reset !== null) {
                setMetric("resetValue", reset);
            }


            /* Response */

            document.getElementById(
                "responseCode"
            ).textContent =
                response.status;


            document.getElementById(
                "responseBody"
            ).textContent =
                formatResponse(body);


            /* Status */

            const status =
                document.getElementById(
                    "requestStatus"
                );

            const title =
                document.getElementById(
                    "statusTitle"
                );

            const message =
                document.getElementById(
                    "statusMessage"
                );


            status.classList.remove(
                "hidden",
                "blocked"
            );


            if (response.status === 429) {

                status.classList.add(
                    "blocked"
                );

                title.textContent =
                    "Rate limit exceeded";

                message.textContent =
                    "Too many requests were sent during the current rate-limit window.";

            } else {

                title.textContent =
                    "Request allowed";

                message.textContent =
                    "The request passed through the rate limiter.";

            }


        } catch (error) {

            document.getElementById(
                "responseBody"
            ).textContent =
                "Request failed:\n\n" +
                error.message;

        }

    });


/* ================= FORMAT RESPONSE ================= */

function formatResponse(body) {

    try {

        const json =
            JSON.parse(body);

        return JSON.stringify(
            json,
            null,
            2
        );

    } catch {

        return body;

    }

}