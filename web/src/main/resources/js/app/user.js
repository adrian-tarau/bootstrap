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
    if (!this.roles) {
        this.roles = {};
        let existingRoles = this.getData().roles || {};
        for (const existingRole of existingRoles) {
            this.roles[existingRole] = 1;
        }
    }
    return this.roles[role] === 1;
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
User.initialize = function () {
    Logger.debug("Initialize user: " + this.getName() + " (" + this.getUserName() + ")");
}

// initialize user
User.initialize();