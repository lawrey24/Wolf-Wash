const equipmentGrid = document.querySelector(".equipment-grid");

if (equipmentGrid) {
    function updateReasonValidity(form) {
        const reason = form.elements.reason;
        reason.required = form.elements.status.value === "OUT_OF_SERVICE";
        reason.setCustomValidity(reason.required && reason.value && !reason.value.trim()
            ? "Enter an out-of-service reason." : "");
    }

    for (const form of equipmentGrid.querySelectorAll("form")) {
        updateReasonValidity(form);
    }

    for (const eventName of ["input", "change"]) {
        equipmentGrid.addEventListener(eventName, (event) => {
            if (event.target.form) updateReasonValidity(event.target.form);
        });
    }

    equipmentGrid.addEventListener("submit", async (event) => {
        const form = event.target;
        updateReasonValidity(form);
        event.preventDefault();
        if (!form.reportValidity()) return;

        const card = form.closest(".equipment-card");
        const button = form.querySelector("button[type=submit]");
        const data = new URLSearchParams(new FormData(form));
        button.disabled = true;
        form.querySelector("[data-request-error]")?.remove();

        try {
            const response = await fetch(form.action, { method: "POST", body: data });
            if (!response.ok) throw new Error("Status request failed");
            if (new URL(response.url).pathname !== new URL(form.action).pathname) {
                window.location.assign(response.url);
                return;
            }

            const page = new DOMParser().parseFromString(await response.text(), "text/html");
            const updatedCard = page.getElementById(card.id);
            if (!updatedCard || page.querySelector(".admin-alert")) {
                throw new Error("Status response unavailable");
            }

            const scrollX = window.scrollX;
            const scrollY = window.scrollY;
            card.replaceWith(updatedCard);
            updateReasonValidity(updatedCard.querySelector("form"));
            updatedCard.querySelector('[aria-invalid="true"], button[type="submit"]')?.focus({ preventScroll: true });
            window.scrollTo(scrollX, scrollY);
        } catch {
            const error = document.createElement("p");
            error.className = "equipment-error";
            error.dataset.requestError = "";
            error.setAttribute("role", "alert");
            error.textContent = "Unable to confirm the update. Try again.";
            button.before(error);
        } finally {
            button.disabled = false;
        }
    });
}
