/*
* The Content Variables
 */
window.User = window.User || {};

/**
 * Returns the username of the current user.
 *
 * @return {boolean} true if the user is authenticated, false otherwise
 */
User.isAuthenticated = function () {
    return Utils.defaultIfNotDefinedOrNull(this.getData().authenticated, false);
}

/**
 * Returns the username of the current user.
 *
 * @return {string} the username
 */
User.getUserName = function () {
    return Utils.defaultIfNotDefinedOrNull(this.getData().userName, "anonymous");
}

/**
 * Returns the (display) name of the current user.
 *
 * @return {string} the name
 */
User.getName = function () {
    return Utils.defaultIfNotDefinedOrNull(this.getData().name, "Anonymous");
}

/**
 * Returns the email of the current user.
 *
 * @return {string} the email, null if not available
 */
User.getEmail = function () {
    return this.getData().email;
}

/**
 * Returns the email of the current user.
 *
 * @return {boolean} true if the user has the role, false otherwise
 */
User.hasRole = function (role) {
    Utils.requireNonNull(role);
    if (!this.roles) {
        this.roles = {};
        let existingRoles = this.getData().roles || {};
        for (const existingRole of existingRoles) {
            this.roles[existingRole.toUpperCase()] = 1;
        }
    }
    return this.roles[role.toUpperCase()] === 1;
}

/**
 * Returns an object with user data (info).
 *
 * @return {Object} the data associated with the user
 */
User.getData = function () {
    return APP_USER || {};
}

/**
 * Initializes the user
 */
User.showProfile = function () {
    Application.get("/user/profile", {}, function (data) {
        Application.loadModal("user-profile", data);
    }, {self: false});
}

/**
 * Saves the profile form
 */
User.saveProfile = function () {
    const me = this;
    Application.saveForm('#user-profile-form', "/user/profile", {}, {
        self: false,
        success: function (data) {
            Application.showInfoAlert("Profile", "The profile information has been saved");
            me.updateInfo(data.payload);
            Application.closeModal();
        }
    });
}

/**
 * Initializes the user
 */
User.initialize = function () {
    Logger.debug("Initialize user: " + this.getName() + " (" + this.getUserName() + ")");
}

/**
 * Starts user related functionality
 */
User.start = function () {
    if (this.getData().resetPassword) {
        Application.showWarnAlert("Security", "Your password was reset recently by the system administrator or yourself. Please change your password to a password known only to you, as soon as possible.");
    }
}

/**
 * Updates user info.
 *
 * @param {Object} user the user object containing the updated user information
 */
User.updateInfo = function (user) {
    this.getData().name = user.name;
    this.getData().email = user.email;
    $('.user-display-name').html(user.name);
    $('.user-email').html(user.email);
}

// initialize user
User.initialize();