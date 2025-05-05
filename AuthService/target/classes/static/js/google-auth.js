
// Initialize Google Sign-In
window.onload = function () {
    google.accounts.id.initialize({
        client_id: "730255015972-m08dadflh0eatde4ij0tgacnb556939c.apps.googleusercontent.com",
        callback: handleCredentialResponse,
        ux_mode: "popup",
    });

    // Render the Google Sign-In button
    google.accounts.id.renderButton(
        document.getElementById("google-login-btn"),
        {
            theme: "outline",
            size: "large",
            text: "signin_with",
            shape: "rectangular",
            logo_alignment: "left"
        }
    );

};

// Callback when Google token is received
async function handleCredentialResponse(response) {
    const idToken = response.credential;

    try {
        const res = await fetch("/api/google-login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ idToken }),
            credentials: "include",
        });

        const result = await res.json();

        if (res.ok) {
            console.log(result.message);
            if (result.message === "User requires profile data") {
                window.location.href = "/profile-setup.html";
            } else {
                alert("Google login successful!");
                window.location.href = "/index.html";
            }
        } else {
            alert("Google login failed: " + result.error);
        }
    } catch (error) {
        alert("An error occurred: " + error.message);
    }
}

