(function () {
    const cookieName = "BidinlyAuth";

    function setCookie(value) {
        if (!value) {
            document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/; SameSite=Lax`;
            return;
        }

        document.cookie = `${cookieName}=${value}; path=/; SameSite=Lax`;
    }

    function getCookie() {
        const cookies = document.cookie ? document.cookie.split("; ") : [];
        for (const cookie of cookies) {
            if (cookie.startsWith(`${cookieName}=`)) {
                return cookie.substring(cookieName.length + 1);
            }
        }
        return null;
    }

    window.authStorage = {
        saveUser: function (userJson) {
            if (!userJson) {
                this.clearUser();
                return;
            }

            sessionStorage.setItem("currentUser", userJson);
            const encoded = btoa(unescape(encodeURIComponent(userJson)));
            setCookie(encoded);
        },
        getUser: function () {
            const fromSession = sessionStorage.getItem("currentUser");
            if (fromSession && fromSession.length > 0) {
                return fromSession;
            }

            const cookieValue = getCookie();
            if (!cookieValue) {
                return null;
            }

            try {
                const decoded = decodeURIComponent(escape(atob(cookieValue)));
                sessionStorage.setItem("currentUser", decoded);
                return decoded;
            } catch {
                this.clearUser();
                return null;
            }
        },

        getPublicKey: function () {
            const userJson = this.getUser();
            if (!userJson) return null;

            try {
                const user = JSON.parse(userJson);
                return user.publicKey || null;
            } catch {
                return null;
            }
        },
        
        clearUser: function () {
            sessionStorage.removeItem("currentUser");
            setCookie("");
        }
    };
})();

