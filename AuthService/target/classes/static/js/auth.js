document.addEventListener("DOMContentLoaded", function () { 
    checkAuthStatus(); // Check session on page load

    document.getElementById("register-form")?.addEventListener("submit", registerUser);
    document.getElementById("login-form")?.addEventListener("submit", loginUser);
    document.getElementById("logout-btn")?.addEventListener("click", logoutUser);
    document.getElementById("reactivate-btn")?.addEventListener("click", reactivateAccount);
    document.getElementById("verify-btn")?.addEventListener("click", verifyCredentials);
    document.getElementById("forgot-password-link")?.addEventListener("click", showResetPasswordForm);
    document.getElementById("send-otp-btn")?.addEventListener("click", requestPasswordReset); // Trigger send OTP
    document.getElementById("reset-password-btn")?.addEventListener("click", resetPassword); // Trigger reset password with OTP
});


/* Verify Credentials (OTP) */
async function verifyCredentials(event) {
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const response = await fetch("/api/verify", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
    });

    const data = await response.json();

    if (response.ok) {
        document.getElementById("otp-section").style.display = "block"; // Show OTP section
    } else {
        document.getElementById("error-msg").innerText = data.error || "Verification failed.";
    }
}

/* Login User */
async function loginUser(event) {
    event.preventDefault();
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const otp = document.getElementById("otp").value;

    try {
        const response = await fetch("/api/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password, otp }),
            credentials: "include",
        });

        // Check if the response is valid JSON
        let data = {};
        if (response.ok) {
            if (response.headers.get("Content-Type")?.includes("application/json")) {
                data = await response.json();
            } else {
                const text = await response.text();
                throw new Error("Server returned a non-JSON response: " + text);
            }

            if (data.actionRequired === "reactivate") {
                // Show reactivation prompt if account is soft-deleted and within reactivation period
                showReactivationPrompt();
            } else {
                alert("Login successful!");
                window.location.href = "/index.html"; // Redirect to home page
            }
        } else {
            // Handle errors in response
            const errorMessage = data.error || "Invalid login credentials or OTP.";
            document.getElementById("error-msg").innerText = errorMessage;
        }
    } catch (error) {
        // If there was an error in the fetch request or in processing the response
        document.getElementById("error-msg").innerText = error.message || "An unexpected error occurred.";
    }
}

/* Show Reactivation Prompt */
function showReactivationPrompt() {
    const reactivationModal = document.getElementById("reactivation-modal");
    
    // Check if the modal element exists
    if (reactivationModal) {
        reactivationModal.style.display = "block"; // Show the reactivation modal
    } else {
        console.error("Reactivation modal not found!");
    }
}

/* Reactivate Account */
async function reactivateAccount() {
    const response = await fetch("/api/reactivate-account", {
        method: "POST",
        credentials: "include", // Ensure session is cleared on the backend
    });

    const data = await response.json();
    if (response.ok) {
        document.getElementById("reactivation-modal").style.display = "none";
        window.location.href = "/index.html"; // Redirect on reactivation success
    } else {
        document.getElementById("error-msg").innerText = data.error || "Failed to reactivate account.";
    }
}

/* Logout User */
async function logoutUser() {
    await fetch("/api/logout", {
        method: "POST",
        credentials: "include",
    });

    alert("Logged out successfully.");
    window.location.href = "/index.html";
}

/* Check Authentication Status */
async function checkAuthStatus() {
    const response = await fetch("/api/auth-status", { credentials: "include" });

    if (response.ok) {
        const data = await response.json();
        const authSection = document.getElementById("auth-section");
        const userSection = document.getElementById("user-section");

        if (data.loggedIn) {
            authSection?.classList.add("hidden");
            userSection?.classList.remove("hidden");
        } else {
            authSection?.classList.remove("hidden");
            userSection?.classList.add("hidden");
        }
    }
}

/* Register User */
async function registerUser(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const avatar = formData.get("avatar");

    let avatarLink = null; // Default to null if no avatar is uploaded

    if (avatar && avatar.size > 0) {
        const uploadFormData = new FormData();
        uploadFormData.append("avatar", avatar);

        const uploadResp = await fetch("http://localhost:6998/api/avatar/upload", {
            method: "POST",
            body: uploadFormData,
        });

        if (uploadResp.ok) {
            const uploadResult = await uploadResp.json();
            avatarLink = uploadResult.avatarUrl;
        } else {
            alert("Avatar upload failed. Error status: " + uploadResp.status);
            return;
        }
    }

    const userData = {
        email: formData.get("email"),
        password: formData.get("password"),
        userName: formData.get("userName"),
        bio: formData.get("bio") || "",
        avatarLink: avatarLink, // Can be null if no avatar is provided
    };

    const response = await fetch("/api/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(userData),
        credentials: "include",
    });

    const result = await response.json();
    if (response.ok) {
        alert("Registration successful! Please log in.");
        window.location.href = "/login.html";
    } else {
        alert("Registration failed: " + result.error);
    }
}

/* Show Reset Password Form */
function showResetPasswordForm(event) {
    event.preventDefault(); // Prevent any form submission or unwanted redirection
    document.getElementById("reset-password-section").style.display = "block"; // Show the reset password form
    document.getElementById("login-form").style.display = "none"; // Hide the login form
}

/* Request Password Reset (Send OTP to email) */
async function requestPasswordReset() {
    const email = document.getElementById("reset-email").value;
    const response = await fetch("/api/forgot-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email })
    });

    const data = await response.json();

    if (response.ok) {
        alert("OTP sent to your email.");
        document.getElementById("reset-password-section").style.display = "none"; // Hide reset password section after success
        document.getElementById("otp-input-section").style.display = "block"; // Show OTP input section
    } else {
        document.getElementById("reset-error-msg").innerText = data.error || "Failed to send OTP.";
    }
}

/* Reset Password */
async function resetPassword() {
    const email = document.getElementById("reset-email").value;
    const otp = document.getElementById("otp-input").value; // Changed to match OTP input field
    const newPassword = document.getElementById("new-password").value;

    const response = await fetch("/api/reset-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, otp, newPassword })
    });

    const data = await response.json();

    if (response.ok) {
        alert("Password reset successful. You can now log in.");
        document.getElementById("otp-input-section").style.display = "none"; // Hide OTP section after success
        document.getElementById("login-form").style.display = "block"; // Show login form again
    } else {
        document.getElementById("otp-error-msg").innerText = data.error || "Password reset failed. Check OTP or email.";
    }
}
